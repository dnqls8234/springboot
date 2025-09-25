package com.mindshift.ums.api.exception;

import com.mindshift.ums.api.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for UMS API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotFoundException(MessageNotFoundException ex, WebRequest request) {
        logger.warn("Message not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "MESSAGE_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTenantNotFoundException(TenantNotFoundException ex, WebRequest request) {
        logger.warn("Tenant not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "TENANT_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTemplateNotFoundException(TemplateNotFoundException ex, WebRequest request) {
        logger.warn("Template not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "TEMPLATE_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidTemplateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTemplateException(InvalidTemplateException ex, WebRequest request) {
        logger.warn("Invalid template: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_TEMPLATE",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        logger.warn("Rate limit exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        // Add rate limit headers
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header("X-RateLimit-Remaining", String.valueOf(ex.getRemainingTokens()))
            .header("X-RateLimit-Reset", String.valueOf(ex.getResetTimeSeconds()))
            .body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        if (ex.getValidationErrors() != null) {
            errorResponse.setDetails(ex.getValidationErrors());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "AUTHENTICATION_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyException(IdempotencyException ex, WebRequest request) {
        logger.warn("Idempotency error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "IDEMPOTENCY_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Method argument validation error: {}", ex.getMessage());

        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            LocalDateTime.now(),
            request.getDescription(false)
        );
        errorResponse.setDetails(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        logger.warn("Binding error: {}", ex.getMessage());

        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request binding failed",
            LocalDateTime.now(),
            request.getDescription(false)
        );
        errorResponse.setDetails(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_REQUEST",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}