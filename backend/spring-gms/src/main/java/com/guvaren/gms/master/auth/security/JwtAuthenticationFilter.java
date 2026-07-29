package com.guvaren.gms.master.auth.security;

import com.guvaren.gms.exception.JwtAuthenticationException;
import com.guvaren.gms.master.auth.service.AccessJwtService;
import com.guvaren.gms.master.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final CustomUserDetailsService userDetailsService;
    private final AccessJwtService accessJwtService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isAuthEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = extractJwtFromRequest(request);
        try {
            if (jwt == null) {
                throw new JwtException("JWT cannot be null or empty");
            }

            if (!isAlreadyAuthenticated()) {
                if (!this.accessJwtService.isAccessTokenValid(jwt)) {
                    throw new JwtException("access token has expired or is invalid");
                }

                String username = this.accessJwtService.extractAccessUsername(jwt);

                UserDetails userDetails =
                        this.userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT authentication failed: {}", e.getMessage());
            this.jwtAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new JwtAuthenticationException(e.getMessage())
            );
        }
    }

    private boolean isAuthEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.contains("/api/v1/auth/register") ||
                path.contains("/api/v1/auth/login") ||
                path.contains("/api/v1/auth/login-logout") ||
                path.contains("/api/v1/auth/logout") ||
                path.contains("/api/v1/auth/logout-all-devices") ||
                path.contains("/swagger-ui/***") ||
                path.contains("/api/v1/auth/refresh-token");
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private boolean isAlreadyAuthenticated() {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        return existingAuth != null && existingAuth.isAuthenticated();
    }
}