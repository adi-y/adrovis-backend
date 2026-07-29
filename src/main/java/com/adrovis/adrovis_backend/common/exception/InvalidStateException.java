package com.adrovis.adrovis_backend.common.exception;

import java.io.Serial;

public class InvalidStateException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidStateException() {
        super(ErrorCode.INVALID_STATE);
    }

    public InvalidStateException(String message) {
        super(ErrorCode.INVALID_STATE, message);
    }

    public InvalidStateException(String message, Throwable cause) {
        super(ErrorCode.INVALID_STATE, message, cause);
    }
}