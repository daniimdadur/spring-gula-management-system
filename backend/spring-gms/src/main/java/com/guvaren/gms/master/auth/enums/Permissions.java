package com.guvaren.gms.master.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permissions {
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    USER_ASSIGN_ROLE("user:assign-role"),

    ROLE_READ("role:read"),
    ROLE_CREATE("role:create"),
    ROLE_UPDATE("role:update"),
    ROLE_DELETE("role:delete"),

    FAKULTAS_READ("fakultas:read"),
    FAKULTAS_CREATE("fakultas:create"),
    FAKULTAS_UPDATE("fakultas:update"),
    FAKULTAS_DELETE("fakultas:delete");

    private final String value;
}
