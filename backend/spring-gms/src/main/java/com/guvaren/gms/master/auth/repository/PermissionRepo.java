package com.guvaren.gms.master.auth.repository;

import com.guvaren.gms.master.auth.entity.PermissionEntity;
import com.guvaren.gms.master.auth.enums.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepo extends JpaRepository<PermissionEntity, String> {
    Optional<PermissionEntity> findByName(Permissions name);
}
