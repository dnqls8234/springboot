package com.mindshift.ums.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorDto {

    public static class ErrorResponse {
        private ApiError error;

        public ErrorResponse() {}

        public ErrorResponse(ApiError error) {
            this.error = error;
        }

        public ApiError getError() { return error; }
        public void setError(ApiError error) { this.error = error; }
    }

    public static class ApiError {
        private String code;
        private String message;
        private Map<String, Object> details;
        private String timestamp;

        public ApiError() {
            this.timestamp = LocalDateTime.now().toString();
        }

        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = LocalDateTime.now().toString();
        }

        public ApiError(String code, String message, Map<String, Object> details) {
            this.code = code;
            this.message = message;
            this.details = details;
            this.timestamp = LocalDateTime.now().toString();
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class ErrorCodes {
        public static final String INVALID_CHANNEL = "INVALID_CHANNEL";
        public static final String INVALID_TEMPLATE = "INVALID_TEMPLATE";
        public static final String MISSING_VARIABLES = "MISSING_VARIABLES";
        public static final String INVALID_RECIPIENT = "INVALID_RECIPIENT";
        public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
        public static final String DUPLICATE_REQUEST = "DUPLICATE_REQUEST";
        public static final String QUIET_HOURS = "QUIET_HOURS";
        public static final String RECIPIENT_OPTED_OUT = "RECIPIENT_OPTED_OUT";
        public static final String INVALID_SIGNATURE = "INVALID_SIGNATURE";
        public static final String EXPIRED_REQUEST = "EXPIRED_REQUEST";
        public static final String TENANT_NOT_FOUND = "TENANT_NOT_FOUND";
        public static final String TENANT_SUSPENDED = "TENANT_SUSPENDED";
        public static final String INSUFFICIENT_CREDITS = "INSUFFICIENT_CREDITS";
        public static final String MESSAGE_NOT_FOUND = "MESSAGE_NOT_FOUND";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    }
}