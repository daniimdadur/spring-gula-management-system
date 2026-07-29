package com.guvaren.gms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomStatus {
    SUCCESS(200, "Success"),
    UPDATED(200, "Updated"),
    DELETED(200, "Deleted");

    private final int code;
    private final String message;
}
