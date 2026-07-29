package com.guvaren.gms.master.auth.repository;

import com.guvaren.gms.master.auth.entity.RoleEntity;
import com.guvaren.gms.master.auth.enums.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepo extends JpaRepository<RoleEntity, String> {
    Optional<RoleEntity> findByRole(Roles role);
    Set<RoleEntity> findByRoleIn(Set<Roles> roles);
}
