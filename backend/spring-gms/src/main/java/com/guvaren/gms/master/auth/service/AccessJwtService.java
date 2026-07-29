package com.guvaren.gms.master.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AccessJwtService {
    private final CustomUserDetailsService userDetailsService;
    private final String accessSecret;
    private final SecretKey accessSecretKey;
    private final long jwtAccessExpiration;

    public AccessJwtService(
            CustomUserDetailsService userDetailsService,
            @Value("${app.jwt.access-secret}") String accessSecret,
            @Value("${app.jwt.access-expiration:60}") long accessExpirationMinutes) {

        this.userDetailsService = userDetailsService;
        this.accessSecret = accessSecret;
        this.accessSecretKey = generateAccessSigningKey();
        this.jwtAccessExpiration = TimeUnit.MINUTES.toMillis(accessExpirationMinutes);
    }

    //auth service
    public String generateAccessToken(String email) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
        Map<String, Object> claims = Map.of("type", "access");
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(this.jwtAccessExpiration)))
                .signWith(this.accessSecretKey)
                .compact();
    }

    //filter
    public String extractAccessUsername(String jwt) {
        Claims claims = getClaims(jwt);
        return claims != null ? claims.getSubject() : null;
    }

    //auth service
    public long getRemainingMinutes(String jwt) {
        try {
            Claims claims = getClaims(jwt);
            if (claims == null || claims.getExpiration() == null) {
                return 0;
            }

            Instant now = Instant.now();
            Instant expiration = claims.getExpiration().toInstant();

            // Jika waktu kadaluarsa sudah lewat dari waktu sekarang
            if (expiration.isBefore(now)) {
                return 0;
            }

            // Menghitung selisih menit dengan aman menggunakan ChronoUnit
            return ChronoUnit.MINUTES.between(now, expiration);
        } catch (JwtException | IllegalArgumentException e) {
            // Menangani jika token rusak, tanda tangan salah, atau formatnya tidak valid
            return 0;
        }
    }

    //filter
    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey generateAccessSigningKey() {
        byte[] decodedKey = this.accessSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.accessSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}