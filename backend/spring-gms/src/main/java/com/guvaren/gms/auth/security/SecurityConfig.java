package com.guvaren.gms.auth.security;

import com.guvaren.gms.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAnyRole("OWNER", "ADMIN_GUDANG", "ADMIN_PENJUALAN")
                .requestMatchers("/api/v1/products/**").hasRole("OWNER")

                .requestMatchers(HttpMethod.GET, "/api/v1/inventories/**").hasAnyRole("OWNER", "ADMIN_GUDANG")
                .requestMatchers("/api/v1/inventories/*/in", "/api/v1/inventories/*/out").hasRole("ADMIN_GUDANG")
                .requestMatchers("/api/v1/inventories/*/adjust").hasAnyRole("OWNER", "ADMIN_GUDANG")

                .requestMatchers(HttpMethod.POST, "/api/v1/productions").hasRole("ADMIN_GUDANG")
                .requestMatchers(HttpMethod.GET, "/api/v1/productions/**").hasAnyRole("OWNER", "ADMIN_GUDANG")

                .requestMatchers("/api/v1/customers/**").hasAnyRole("OWNER", "ADMIN_PENJUALAN")

                .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasRole("ADMIN_PENJUALAN")
                .requestMatchers(HttpMethod.GET, "/api/v1/orders/**").hasAnyRole("OWNER", "ADMIN_PENJUALAN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/*/status").hasAnyRole("OWNER", "ADMIN_PENJUALAN")

                .requestMatchers("/api/v1/payments/**").hasAnyRole("OWNER", "ADMIN_PENJUALAN")

                .requestMatchers(HttpMethod.GET, "/api/v1/notifications/**").hasRole("OWNER")
                .requestMatchers(HttpMethod.POST, "/api/v1/notifications/test").hasRole("OWNER")

                .requestMatchers("/api/v1/dashboard/**").hasRole("OWNER")

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
