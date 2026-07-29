package com.adrovis.adrovis_backend.common.exception;

import com.adrovis.adrovis_backend.common.exception.ErrorCode;

import java.io.Serial;
import java.util.Map;

public class AppException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final Map<String, String> fieldErrors;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }

    public AppException(
            ErrorCode errorCode,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldErrors = null;
    }

    /**
     * Used when a business rule is tied to a specific field.
     * Example:
     * email -> already subscribed
     */
    public AppException(
            ErrorCode errorCode,
            String message,
            Map<String, String> fieldErrors
    ) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    // ---------- Convenience factories ----------

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static AppException conflict(String message) {
        return new AppException(ErrorCode.INVALID_STATE, message);
    }

    public static AppException duplicate(String message) {
        return new AppException(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public static AppException fileValidationFailed(String message) {
        return new AppException(ErrorCode.FILE_VALIDATION_FAILED, message);
    }

    public static AppException fileTooLarge(String message) {
        return new AppException(ErrorCode.FILE_TOO_LARGE, message);
    }

    public static AppException externalServiceFailure(
            String message,
            Throwable cause
    ) {
        return new AppException(
                ErrorCode.EXTERNAL_SERVICE_FAILURE,
                message,
                cause
        );
    }
}