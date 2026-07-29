package com.adrovis.adrovis_backend.common.exception;

import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(
            AppException ex,
            HttpServletRequest request
    ) {

        ErrorCode errorCode = ex.getErrorCode();

        log.warn(
                "[{}] {}",
                getTraceId(request),
                ex.getMessage()
        );

        if (ex.getFieldErrors() != null) {

            return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(
                            ApiResponse.validationFailure(
                                    ex.getMessage(),
                                    ex.getFieldErrors(),
                                    getTraceId(request)
                            )
                    );
        }

        return buildResponse(
                errorCode,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        log.warn("[{}] Validation failed", getTraceId(request));

        return ResponseEntity.badRequest().body(
                ApiResponse.validationFailure(
                        "Validation failed",
                        errors,
                        getTraceId(request)
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            );
        }

        log.warn("[{}] Constraint violation", getTraceId(request));

        return ResponseEntity.badRequest().body(
                ApiResponse.validationFailure(
                        "Validation failed",
                        errors,
                        getTraceId(request)
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJson(
            HttpServletRequest request
    ) {

        log.warn("[{}] Malformed JSON request", getTraceId(request));

        return buildResponse(
                ErrorCode.BAD_REQUEST,
                "Malformed JSON request.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMedia(
            HttpServletRequest request
    ) {

        log.warn("[{}] Unsupported media type", getTraceId(request));

        return buildResponse(
                ErrorCode.FILE_VALIDATION_FAILED,
                "Unsupported media type.",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileTooLarge(
            HttpServletRequest request
    ) {

        log.warn("[{}] Uploaded file exceeds maximum allowed size", getTraceId(request));

        return buildResponse(
                ErrorCode.FILE_TOO_LARGE,
                ErrorCode.FILE_TOO_LARGE.getMessage(),
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            HttpServletRequest request
    ) {

        log.warn("[{}] Endpoint not found", getTraceId(request));

        return buildResponse(
                ErrorCode.RESOURCE_NOT_FOUND,
                "The requested endpoint does not exist.",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpServletRequest request
    ) {

        log.warn("[{}] HTTP method not supported", getTraceId(request));

        return buildResponse(
                ErrorCode.BAD_REQUEST,
                "HTTP method not supported for this endpoint.",
                request
        );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequestParameters(
            Exception ex,
            HttpServletRequest request
    ) {

        log.warn(
                "[{}] Bad request parameter: {}",
                getTraceId(request),
                ex.getMessage()
        );

        return buildResponse(
                ErrorCode.BAD_REQUEST,
                "A required parameter is missing or invalid.",
                request
        );
    }

    /**
     * Active once Spring Security is introduced.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            HttpServletRequest request
    ) {

        log.warn("[{}] Access denied", getTraceId(request));

        return buildResponse(
                ErrorCode.FORBIDDEN,
                ErrorCode.FORBIDDEN.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "[{}] Unhandled exception",
                getTraceId(request),
                ex
        );

        return buildResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {

        ApiResponse<Void> response = ApiResponse.failure(
                errorCode.getHttpStatus().value(),
                message,
                errorCode.getCode(),
                getTraceId(request)
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    private String getTraceId(HttpServletRequest request) {

        Object traceId = request.getAttribute(TRACE_ID_KEY);

        return traceId == null
                ? null
                : traceId.toString();
    }
}