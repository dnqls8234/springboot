package com.mindshift.ums.api.exception;

import java.util.Map;

/**
 * Exception for validation errors with detailed field information.
 */
public class ValidationException extends RuntimeException {

    private final Map<String, Object> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }

    public ValidationException(String message, Map<String, Object> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, Object> getValidationErrors() {
        return validationErrors;
    }
}