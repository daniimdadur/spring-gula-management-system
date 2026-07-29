package com.guvaren.gms.master.auth.dto.req;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationReq {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
