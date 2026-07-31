package com.adrovis.adrovis_backend.common.exception;

import java.io.Serial;

public class FileStorageException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FileStorageException() {
        super(ErrorCode.FILE_STORAGE_FAILED);
    }

    public FileStorageException(String message) {
        super(ErrorCode.FILE_STORAGE_FAILED, message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(ErrorCode.FILE_STORAGE_FAILED, message, cause);
    }
}