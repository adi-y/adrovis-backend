package com.adrovis.adrovis_backend.common.exception;// Replace the import section at the top:
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Replace the enum constant list with:
public enum ErrorCode {

    // --- 400 Bad Request ---
    VALIDATION_FAILED("ADV-400", "Validation failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("ADV-400-1", "Malformed request", HttpStatus.BAD_REQUEST),

    // --- 401 / 403 (future admin JWT) ---
    UNAUTHORIZED("ADV-401", "Authentication required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ADV-403", "You do not have permission to perform this action", HttpStatus.FORBIDDEN),

    // --- 404 Not Found ---
    RESOURCE_NOT_FOUND("ADV-404", "Requested resource not found", HttpStatus.NOT_FOUND),

    // --- 409 Conflict ---
    INVALID_STATE("ADV-409", "Invalid resource state", HttpStatus.CONFLICT),
    DUPLICATE_RESOURCE("ADV-409-1", "Resource already exists", HttpStatus.CONFLICT),

    // --- 413 / 415 (file upload — resume storage) ---
    FILE_TOO_LARGE("ADV-413", "Uploaded file exceeds maximum allowed size", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_VALIDATION_FAILED("ADV-415", "Invalid file type or format", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // --- 500 Internal Server Errors ---
    INTERNAL_SERVER_ERROR(
            "ADV-500",
            "Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    FILE_STORAGE_FAILED(
            "ADV-500-1",
            "Failed to store or retrieve file",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    EMAIL_DISPATCH_FAILED(
            "ADV-500-2",
            "Failed to send email notification",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    // --- 502 External Dependencies ---
    EXTERNAL_SERVICE_FAILURE(
            "ADV-502",
            "An external service is unavailable",
            HttpStatus.BAD_GATEWAY
    );


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Resolves an ErrorCode from its string code (e.g. "ADV-404").
     * Falls back to INTERNAL_SERVER_ERROR if no match is found,
     * so a bad/unknown code never throws during error handling itself.
     */
    private static final Map<String, ErrorCode> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            ErrorCode::getCode,
                            Function.identity()
                    ));
    public static ErrorCode fromCode(String code) {
        return LOOKUP.getOrDefault(code, INTERNAL_SERVER_ERROR);
    }
}