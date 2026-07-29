package com.guvaren.gms.master.auth.dto.res;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRes {
    private String accessToken;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
}
