package com.adrovis.adrovis_backend.common.exception;

import java.io.Serial;

public class ResourceNotFoundException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, cause);
    }
}