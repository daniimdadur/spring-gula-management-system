package com.guvaren.gms.master.auth.repository;

import com.guvaren.gms.master.auth.entity.RefreshTokenEntity;
import com.guvaren.gms.master.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByToken(String token);
    @Modifying
    @Query("update RefreshTokenEntity r set r.revoked = true where r.user.email = :email")
    void revokeAllUserTokens(@Param("email") String email);
    List<RefreshTokenEntity> findAllByUserAndRevokedFalse(UserEntity user);
    int deleteByExpiredBefore(LocalDateTime date);
}
