package com.adrovis.adrovis_backend.common.exception;

import java.io.Serial;
import java.util.Map;

public class FileValidationException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FileValidationException() {
        super(ErrorCode.FILE_VALIDATION_FAILED);
    }

    public FileValidationException(String message) {
        super(ErrorCode.FILE_VALIDATION_FAILED, message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(ErrorCode.FILE_VALIDATION_FAILED, message, cause);
    }

    /**
     * Use when a specific request field caused the validation failure.
     * Example:     * resume -> Only PDF or DOCX files are allowed.
     */
    public FileValidationException(
            String message,
            Map<String, String> fieldErrors
    ) {
        super(
                ErrorCode.FILE_VALIDATION_FAILED,
                message,
                fieldErrors
        );
    }
}
