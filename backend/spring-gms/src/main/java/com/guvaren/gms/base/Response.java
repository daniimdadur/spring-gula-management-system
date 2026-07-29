package com.guvaren.gms.base;

import com.guvaren.gms.enums.CustomStatus;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

@Builder
public record Response<T>(int status, String message, T data) {
    public static <T> Response<T> success(T data) {
        return Response.of(HttpStatus.OK, CustomStatus.SUCCESS.getMessage(), data);
    }

    public static <T> Response<PageResponse<T>> successPage(Page<T> page) {
        return Response.of(
                HttpStatus.OK,
                CustomStatus.SUCCESS.getMessage(),
                PageResponse.from(page)
        );
    }

    public static <T> Response<T> created(T data) {
        return Response.of(HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase(), data);
    }

    public static <T> Response<T> updated(T data) {
        return Response.of(HttpStatus.OK, CustomStatus.UPDATED.getMessage(), data);
    }

    public static <T> Response<T> deleted(T data) {
        return Response.of(HttpStatus.OK, CustomStatus.DELETED.getMessage(), data);
    }

    public static <T> Response<T> custom(HttpStatus status, String message, T data) {
        return Response.of(status, message, data);
    }

    //helper method
    private static <T> Response<T> of(
            HttpStatus status,
            String message,
            T data) {

        return Response.<T>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .build();
    }
}
