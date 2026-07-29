package com.guvaren.gms.master.auth.dto.res;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResult {
    private String accessToken;
    private Long accessTokenExpiration;
    private String refreshToken;
    private Long refreshTokenExpiration;
}
