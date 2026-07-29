package com.guvaren.gms.master.auth.controller;

import com.guvaren.gms.base.Response;
import com.guvaren.gms.master.auth.dto.req.AuthenticationReq;
import com.guvaren.gms.master.auth.dto.req.RegistrationReq;
import com.guvaren.gms.master.auth.dto.res.AuthenticationRes;
import com.guvaren.gms.master.auth.dto.res.AuthenticationResult;
import com.guvaren.gms.master.auth.dto.res.TokenRes;
import com.guvaren.gms.master.auth.service.AuthService;
import com.guvaren.gms.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class ApiAuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<Response<AuthenticationRes>> register(@RequestBody RegistrationReq req, HttpServletResponse response) {
        AuthenticationResult result = this.authService.register(req);
        this.cookieUtil.addRefreshTokenCookie(response, result.getRefreshToken());
        AuthenticationRes res = AuthenticationRes.builder()
                .accessToken(result.getAccessToken())
                .accessTokenExpiration(result.getAccessTokenExpiration())
                .refreshTokenExpiration(result.getRefreshTokenExpiration())
                .build();
        return ResponseEntity.ok(Response.created(res));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<AuthenticationRes>> login(@RequestBody AuthenticationReq req, HttpServletResponse response) {
        AuthenticationResult result = this.authService.login(req);
        this.cookieUtil.addRefreshTokenCookie(response, result.getRefreshToken());
        AuthenticationRes res = AuthenticationRes.builder()
                .accessToken(result.getAccessToken())
                .accessTokenExpiration(result.getAccessTokenExpiration())
                .refreshTokenExpiration(result.getRefreshTokenExpiration())
                .build();
        return ResponseEntity.ok(Response.success(res));
    }

    @PostMapping("/login-logout")
    public ResponseEntity<Response<AuthenticationRes>> loginLogout(@RequestBody AuthenticationReq req, HttpServletResponse response) {
        AuthenticationResult result = this.authService.loginAndLogoutForAllDevices(req);
        this.cookieUtil.addRefreshTokenCookie(response, result.getRefreshToken());
        AuthenticationRes res = AuthenticationRes.builder()
                .accessToken(result.getAccessToken())
                .accessTokenExpiration(result.getAccessTokenExpiration())
                .refreshTokenExpiration(result.getRefreshTokenExpiration())
                .build();
        return ResponseEntity.ok(Response.success(res));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Response<TokenRes>> refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        TokenRes res = this.authService.getNewAccessToken(refreshToken);
        return ResponseEntity.ok(Response.created(res));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<String>> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        String res = this.authService.logoutThisDevice(refreshToken);
        this.cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(Response.success(res));
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<Response<String>> logoutAllDevices(@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        String res = this.authService.logoutAllDevices(refreshToken);
        this.cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(Response.success(res));
    }
}
