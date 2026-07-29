package com.guvaren.gms.base;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ResponseError(LocalDateTime timestamp, int status, String message, Object error) {
    public ResponseError(int status, String message, Object error) {
        this(LocalDateTime.now(), status, message, error);
    }

    public static ResponseError of(HttpStatus status, String message, Object errors) {
        return new ResponseError(
                status.value(),
                message,
                errors
        );
    }
}
