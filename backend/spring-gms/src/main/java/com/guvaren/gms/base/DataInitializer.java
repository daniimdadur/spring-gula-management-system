package com.guvaren.gms.base;

import com.guvaren.gms.master.auth.entity.PermissionEntity;
import com.guvaren.gms.master.auth.entity.RoleEntity;
import com.guvaren.gms.master.auth.entity.UserEntity;
import com.guvaren.gms.master.auth.enums.Permissions;
import com.guvaren.gms.master.auth.enums.Roles;
import com.guvaren.gms.master.auth.repository.PermissionRepo;
import com.guvaren.gms.master.auth.repository.RoleRepo;
import com.guvaren.gms.master.auth.repository.UserRepo;
import com.guvaren.gms.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PermissionRepo permissionRepo;
    private final PasswordEncoder passwordEncoder;

    private static final Map<Roles, Set<Permissions>> ROLE_PERMISSIONS = Map.of(
            Roles.USER, Set.of(
                    Permissions.FAKULTAS_READ
            ),

            Roles.ADMIN, Set.of(
                    Permissions.FAKULTAS_READ,
                    Permissions.FAKULTAS_CREATE,
                    Permissions.FAKULTAS_UPDATE,
                    Permissions.FAKULTAS_DELETE,
                    Permissions.USER_READ,
                    Permissions.USER_ASSIGN_ROLE
            ),

            Roles.SUPER_ADMIN, EnumSet.allOf(Permissions.class)
    );

    @Override
    public void run(String @NonNull ... args) throws Exception {
        initializer();
    }

    private void initializer() {
        ROLE_PERMISSIONS.forEach((roleName, permissionNames) -> {

            Set<PermissionEntity> permissions = permissionNames.stream()
                    .map(this::findOrCreatePermission)
                    .collect(Collectors.toSet());

            RoleEntity role = this.roleRepo.findByRole(roleName)
                    .orElseGet(() -> RoleEntity.builder()
                            .id(CommonUtil.getUUID())
                            .role(roleName)
                            .build());

            role.setPermissions(permissions);

            this.roleRepo.save(role);

            log.info("Role {} seeded.", roleName);
        });

        createUser(
                "superadmin@guvaren.com",
                "super",
                "admin",
                "superadmin",
                Set.of(getRole(Roles.SUPER_ADMIN))
        );

        createUser(
                "admin@guvaren.com",
                "admin",
                "admin",
                "admin",
                Set.of(getRole(Roles.ADMIN))
        );

        createUser(
                "user@guvaren.com",
                "user",
                "user",
                "user",
                Set.of(getRole(Roles.USER))
        );
    }

    private PermissionEntity findOrCreatePermission(Permissions name) {

        return this.permissionRepo.findByName(name)
                .orElseGet(() -> {

                    log.info("Creating permission {}", name.getValue());

                    return this.permissionRepo.save(
                            PermissionEntity.builder()
                                    .id(CommonUtil.getUUID())
                                    .name(name)
                                    .build()
                    );
                });
    }

    private RoleEntity getRole(Roles role) {
        return this.roleRepo.findByRole(role)
                .orElseThrow(() -> new IllegalStateException("Role not found : " + role));
    }

    private void createUser(String email, String firstName, String lastName, String password, Set<RoleEntity> roles) {

        if (this.userRepo.existsByEmail(email)) {
            return;
        }

        UserEntity user = UserEntity.builder()
                .id(CommonUtil.getUUID())
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(this.passwordEncoder.encode(password))
                .roles(roles)
                .build();

        this.userRepo.save(user);

        log.info("Default user {} created.", email);
    }
}
