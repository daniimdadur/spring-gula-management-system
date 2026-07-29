package com.guvaren.gms.master.auth.service;

import com.guvaren.gms.exception.DuplicateException;
import com.guvaren.gms.exception.NotFoundException;
import com.guvaren.gms.master.auth.dto.req.AuthenticationReq;
import com.guvaren.gms.master.auth.dto.req.RegistrationReq;
import com.guvaren.gms.master.auth.dto.res.AuthenticationResult;
import com.guvaren.gms.master.auth.dto.res.TokenRes;
import com.guvaren.gms.master.auth.entity.RefreshTokenEntity;
import com.guvaren.gms.master.auth.entity.RoleEntity;
import com.guvaren.gms.master.auth.entity.UserEntity;
import com.guvaren.gms.master.auth.enums.Roles;
import com.guvaren.gms.master.auth.repository.RoleRepo;
import com.guvaren.gms.master.auth.repository.UserRepo;
import com.guvaren.gms.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final AccessJwtService accessJwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthenticationResult register(RegistrationReq req) {
        this.userRepo.findByEmail(req.getEmail()).ifPresent(duplicate -> {
            throw new DuplicateException("Email already exists");
        });
        UserEntity user = UserEntity.builder()
                .id(CommonUtil.getUUID())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        RoleEntity userRole = this.roleRepo.findByRole(Roles.USER)
                .orElseGet(() -> this.roleRepo.save(
                        RoleEntity.builder()
                                .id(CommonUtil.getUUID())
                                .role(Roles.USER)
                                .build()
                ));

        user.setRoles(Set.of(userRole));
        return generateAuthenticationRes(this.userRepo.save(user));
    }

    @Override
    public AuthenticationResult login(AuthenticationReq req) {
        this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        UserEntity user = this.userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + req.getEmail()));
        return generateAuthenticationRes(user);
    }

    @Override
    public AuthenticationResult loginAndLogoutForAllDevices(AuthenticationReq req) {
        this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        UserEntity user = this.userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + req.getEmail()));
        return generateAuthenticationAndLogoutRes(user);
    }

    @Override
    public TokenRes getNewAccessToken(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = this.refreshTokenService.getValidRefreshToken(refreshToken);
        String accessToken = this.accessJwtService.generateAccessToken(refreshTokenEntity.getUser().getEmail());
        return TokenRes.builder()
                .accessToken(accessToken)
                .accessTokenExpiration(this.accessJwtService.getRemainingMinutes(accessToken))
                .build();
    }

    @Override
    @Transactional
    public String logoutAllDevices(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = this.refreshTokenService.getValidRefreshToken(refreshToken);
        this.refreshTokenService.revokeAllUserTokens(refreshTokenEntity.getUser());
        return "Logout successful from all devices";
    }

    @Override
    @Transactional
    public String logoutThisDevice(String refreshToken) {
        this.refreshTokenService.revokeRefreshToken(refreshToken);
        return "Logout successful from this device";
    }

    private AuthenticationResult generateAuthenticationRes(UserEntity user){
        String accessToken = this.accessJwtService.generateAccessToken(user.getEmail());
        String refreshToken = this.refreshTokenService.generateAdditionalRefreshToken(user);
        return AuthenticationResult.builder()
                .accessToken(accessToken)
                .accessTokenExpiration(this.accessJwtService.getRemainingMinutes(accessToken))
                .refreshToken(refreshToken)
                .refreshTokenExpiration(this.refreshTokenService.getRemainingMinutes(refreshToken))
                .build();

    }

    private AuthenticationResult generateAuthenticationAndLogoutRes(UserEntity user){
        String accessToken = this.accessJwtService.generateAccessToken(user.getEmail());
        String refreshToken = this.refreshTokenService.generateRefreshToken(user);
        return AuthenticationResult.builder()
                .accessToken(accessToken)
                .accessTokenExpiration(this.accessJwtService.getRemainingMinutes(accessToken))
                .refreshToken(refreshToken)
                .refreshTokenExpiration(this.refreshTokenService.getRemainingMinutes(refreshToken))
                .build();

    }
}
