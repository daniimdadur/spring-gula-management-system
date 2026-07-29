package com.guvaren.gms.master.auth.entity;

import com.guvaren.gms.master.auth.enums.Roles;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "t_roles")
public class RoleEntity {

    @Id
    @Column(name = "rid", length = 36)
    private String id;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "t_role_permissions",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "rid"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "pid"))
    private Set<PermissionEntity> permissions = new HashSet<>();
}
