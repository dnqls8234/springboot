package com.mindshift.ums.api.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ErrorResponse {
    private String timestamp;
    private String error;
    private String message;
    private String path;
    private Map<String, Object> details;

    public ErrorResponse(String error, String message, String path) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(String error, String message, LocalDateTime timestamp, String path) {
        this.timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(String error, String message, String path, Map<String, Object> details) {
        this(error, message, path);
        this.details = details;
    }

    public ErrorResponse(String error, String message, LocalDateTime timestamp, String path, Map<String, Object> details) {
        this(error, message, timestamp, path);
        this.details = details;
    }

    // Getters and setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}