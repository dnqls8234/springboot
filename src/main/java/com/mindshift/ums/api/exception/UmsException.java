package com.mindshift.ums.api.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class UmsException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> details;

    public UmsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.details = null;
    }

    public UmsException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public UmsException(String message, String errorCode, HttpStatus httpStatus, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public UmsException(String message, String errorCode, HttpStatus httpStatus, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    // Simple constructors for backward compatibility
    public UmsException(String message) {
        this(message, "GENERIC_ERROR");
    }

    public UmsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERIC_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.details = null;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }
    public Map<String, Object> getDetails() { return details; }
}