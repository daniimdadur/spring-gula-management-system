package com.guvaren.gms.master.auth.dto.req;

import com.guvaren.gms.master.auth.enums.Roles;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolesReq {
    private Set<Roles> roles;
}
