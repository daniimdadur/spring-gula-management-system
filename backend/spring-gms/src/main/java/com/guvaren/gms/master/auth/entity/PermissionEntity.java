package com.guvaren.gms.master.auth.entity;

import com.guvaren.gms.master.auth.enums.Permissions;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "t_permissions")
public class PermissionEntity {

    @Id
    @Column(name = "pid", length = 36)
    private String id;

    @Column(name = "name", length = 64, nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private Permissions name;
}
