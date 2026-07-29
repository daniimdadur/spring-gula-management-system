package com.guvaren.gms.master.auth.service;

import com.guvaren.gms.exception.BadRequestException;
import com.guvaren.gms.exception.JwtAuthenticationException;
import com.guvaren.gms.master.auth.entity.RefreshTokenEntity;
import com.guvaren.gms.master.auth.entity.UserEntity;
import com.guvaren.gms.master.auth.repository.RefreshTokenRepo;
import com.guvaren.gms.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RefreshTokenService {
    private final long refreshTokenExpirationMillis;
    private final RefreshTokenRepo refreshTokenRepo;
    private final TokenHashingService tokenHashingService;

    public RefreshTokenService(@Value("${app.jwt.refresh-expiration:1440}") long refreshTokenExpirationMillis, RefreshTokenRepo refreshTokenRepo, TokenHashingService tokenHashingService) {
        this.refreshTokenExpirationMillis = TimeUnit.MINUTES.toMillis(refreshTokenExpirationMillis);
        this.refreshTokenRepo = refreshTokenRepo;
        this.tokenHashingService = tokenHashingService;
    }

    /**
     * Generate new refresh token and revoke all previous tokens for the user
     * This implements token rotation for better security
     */
    @Transactional
    public String generateRefreshToken(UserEntity user) {
        try {
            // Revoke all existing valid tokens for this user (token rotation)
            revokeAllUserTokens(user);

            String rawToken = UUID.randomUUID().toString();
            RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                    .id(CommonUtil.getUUID())
                    .token(this.tokenHashingService.hashToken(rawToken))
                    .expired(LocalDateTime.now().plus(this.refreshTokenExpirationMillis, ChronoUnit.MILLIS))
                    .revoked(false)
                    .user(user)
                    .build();

            this.refreshTokenRepo.save(refreshTokenEntity);
            log.debug("Generated new refresh token for user: {}", user.getId());
            return rawToken;

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while generating refresh token", e);
            throw new JwtAuthenticationException("Failed to generate refresh token due to data conflict" + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating refresh token", e);
            throw new JwtAuthenticationException("Failed to generate refresh token" + e.getMessage());
        }
    }

    /**
     * Generate refresh token without revoking existing ones
     * Use this if you want to allow multiple valid refresh tokens
     */
    @Transactional
    public String generateAdditionalRefreshToken(UserEntity user) {
        try {
            String rawToken = UUID.randomUUID().toString();
            RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                    .id(CommonUtil.getUUID())
                    .token(this.tokenHashingService.hashToken(rawToken))
                    .expired(LocalDateTime.now().plus(this.refreshTokenExpirationMillis, ChronoUnit.MILLIS))
                    .revoked(false)
                    .user(user)
                    .build();

            this.refreshTokenRepo.save(refreshTokenEntity);
            return rawToken;
        } catch (Exception e) {
            log.error("Failed to generate additional refresh token", e);
            throw new JwtAuthenticationException("Failed to generate refresh token" + e.getMessage());
        }
    }

    /**
     * Validate refresh token - returns true if token exists, not revoked, and not expired
     */
    @Transactional
    public boolean isRefreshTokenValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        try {
            String hashedToken = this.tokenHashingService.hashToken(rawToken);
            Optional<RefreshTokenEntity> tokenEntity = this.refreshTokenRepo.findByToken(hashedToken);

            return tokenEntity.map(entity -> {
                boolean isValid = !entity.isRevoked() &&
                        entity.getExpired().isAfter(LocalDateTime.now());

                if (!isValid) {
                    log.debug("Token validation failed. Revoked: {}, Expired: {}",
                            entity.isRevoked(), entity.getExpired());
                }
                return isValid;
            }).orElse(false);

        } catch (Exception e) {
            log.error("Error validating refresh token", e);
            return false;
        }
    }

    /**
     * Validate refresh token and return the token entity if valid
     */
    @Transactional
    public RefreshTokenEntity getValidRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Token cannot be null or empty");
        }

        String hashedToken = this.tokenHashingService.hashToken(rawToken);
        return this.refreshTokenRepo.findByToken(hashedToken)
                .filter(entity -> !entity.isRevoked())
                .filter(entity -> entity.getExpired().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new JwtAuthenticationException("Invalid or expired refresh token"));
    }

    /**
     * Get remaining validity in minutes
     */
    @Transactional
    public long getRemainingMinutes(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return 0;
        }

        try {
            String hashedToken = this.tokenHashingService.hashToken(rawToken);
            Optional<RefreshTokenEntity> tokenEntity = this.refreshTokenRepo.findByToken(hashedToken);

            return tokenEntity.map(entity -> {
                if (entity.isRevoked()) {
                    return 0L;
                }

                long remainingMillis = ChronoUnit.MILLIS.between(
                        LocalDateTime.now(),
                        entity.getExpired()
                );

                return Math.max(0, TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
            }).orElse(0L);

        } catch (Exception e) {
            log.error("Error calculating remaining time for token", e);
            return 0;
        }
    }

    /**
     * Revoke a specific refresh token
     */
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Token cannot be null or empty");
        }

        String hashedToken = this.tokenHashingService.hashToken(rawToken);
        this.refreshTokenRepo.findByToken(hashedToken).ifPresent(entity -> {
            entity.setRevoked(true);
            this.refreshTokenRepo.save(entity);
            log.debug("Revoked refresh token for user: {}", entity.getUser().getId());
        });
    }

    /**
     * Revoke all tokens for a specific user
     */
    @Transactional
    public void revokeAllUserTokens(UserEntity user) {
        List<RefreshTokenEntity> validTokens = this.refreshTokenRepo.findAllByUserAndRevokedFalse(user);
        if (!validTokens.isEmpty()) {
            validTokens.forEach(token -> token.setRevoked(true));
            this.refreshTokenRepo.saveAll(validTokens);
            log.debug("Revoked {} tokens for user: {}", validTokens.size(), user.getId());
        }
    }

    /**
     * Clean up expired tokens (should be called periodically by a scheduler)
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanUpExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = this.refreshTokenRepo.deleteByExpiredBefore(now);
        log.info("Cleaned up {} expired refresh tokens", deletedCount);
    }

    /**
     * Rotate refresh token - revoke old one and generate new one
     * This is the recommended approach for refresh token rotation
     */
//    @Transactional
//    public RefreshTokenResponse rotateRefreshToken(String oldRawToken) {
//        return getValidRefreshToken(oldRawToken)
//                .map(oldToken -> {
//                    // Revoke the old token
//                    oldToken.setRevoked(true);
//                    refreshTokenRepo.save(oldToken);
//
//                    // Generate new token
//                    String newRawToken = generateRefreshToken(oldToken.getUser());
//
//                    return RefreshTokenResponse.builder()
//                            .refreshToken(newRawToken)
//                            .expiresIn(TimeUnit.MILLISECONDS.toMinutes(jwtRefreshExpirationMs))
//                            .build();
//                })
//                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid or expired refresh token"));
//    }
}
