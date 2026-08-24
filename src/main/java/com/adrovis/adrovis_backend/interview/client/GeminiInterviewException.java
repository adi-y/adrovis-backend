package com.adrovis.adrovis_backend.interview.client;

public class GeminiInterviewException extends RuntimeException {

    public GeminiInterviewException(String message) {
        super(message);
    }

    public GeminiInterviewException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}