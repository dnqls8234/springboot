package com.mindshift.ums.api.exception;

import org.springframework.http.HttpStatus;

public class MessageNotFoundException extends UmsException {

    public MessageNotFoundException(String requestId) {
        super("Message not found with requestId: " + requestId, "MESSAGE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}