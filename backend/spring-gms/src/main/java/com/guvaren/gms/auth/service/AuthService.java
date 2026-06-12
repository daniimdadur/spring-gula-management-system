package com.guvaren.gms.auth.service;

import com.guvaren.gms.auth.dto.request.LoginRequest;
import com.guvaren.gms.auth.dto.request.RegisterRequest;
import com.guvaren.gms.auth.dto.response.LoginResponse;
import com.guvaren.gms.auth.dto.response.RegisterResponse;
import com.guvaren.gms.auth.entity.User;
import com.guvaren.gms.auth.repository.UserRepository;
import com.guvaren.gms.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public RegisterResponse register(RegisterRequest request) {
        // 1. Check bootstrap: if users exist, enforce that registration requires OWNER role
        if (userRepository.count() > 0) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required to register new users");
            }
            boolean isOwner = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_OWNER"));
            if (!isOwner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only OWNER can register new users");
            }
        }

        // 2. Validate availability
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        // 3. Create User
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        return RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .message("User registered successfully")
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found after authentication"));

        return LoginResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInMs() / 1000)
                .role(user.getRole())
                .build();
    }
}
