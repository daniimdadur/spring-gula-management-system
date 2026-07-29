package com.guvaren.gms.master.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_token")
public class RefreshTokenEntity {

    @Id
    @Column(name = "tid", length = 36)
    private String id;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "expired")
    private LocalDateTime expired;

    @Column(name = "revoked")
    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
