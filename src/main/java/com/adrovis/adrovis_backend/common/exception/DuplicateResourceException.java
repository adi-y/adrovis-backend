package com.adrovis.adrovis_backend.common.exception;

import java.io.Serial;
import java.util.Map;

public class DuplicateResourceException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateResourceException() {
        super(ErrorCode.DUPLICATE_RESOURCE);
    }

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(ErrorCode.DUPLICATE_RESOURCE, message, cause);
    }

    public DuplicateResourceException(
            String message,
            Map<String, String> fieldErrors
    ) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                message,
                fieldErrors
        );
    }
}