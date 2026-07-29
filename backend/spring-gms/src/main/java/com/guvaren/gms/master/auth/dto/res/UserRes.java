package com.guvaren.gms.master.auth.dto.res;

import com.guvaren.gms.master.auth.enums.Roles;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRes {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<Roles> roles;
}
