package com.adrovis.adrovis_backend.common.dto;

import com.adrovis.adrovis_backend.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        int status,
        String message,
        String errorCode,
        T data,
        Map<String, String> fieldErrors,
        String traceId,
        Instant timestamp
) {

    // ---------- Success ----------

    public static <T> ApiResponse<T> success(
            HttpStatus status,
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                status.value(),
                message,
                null,
                data,
                null,
                null,
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(
                HttpStatus.OK,
                "Request completed successfully",
                data
        );
    }

    // ---------- Failure ----------

    public static <T> ApiResponse<T> failure(
            int status,
            String message,
            String errorCode,
            String traceId
    ) {
        return new ApiResponse<>(
                false,
                status,
                message,
                errorCode,
                null,
                null,
                traceId,
                Instant.now()
        );
    }

    // Optional overload (useful elsewhere in the project)
    public static <T> ApiResponse<T> failure(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            String traceId
    ) {
        return failure(
                status.value(),
                message,
                errorCode.getCode(),
                traceId
        );
    }

    // ---------- Validation Failure ----------

    public static <T> ApiResponse<T> validationFailure(
            String message,
            Map<String, String> fieldErrors,
            String traceId
    ) {
        return new ApiResponse<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                message,
                ErrorCode.VALIDATION_FAILED.getCode(),
                null,
                fieldErrors,
                traceId,
                Instant.now()
        );
    }
}