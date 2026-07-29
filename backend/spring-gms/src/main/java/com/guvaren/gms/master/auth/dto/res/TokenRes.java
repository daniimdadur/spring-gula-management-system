package com.guvaren.gms.master.auth.dto.res;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRes {
    private String accessToken;
    private Long accessTokenExpiration;
}
