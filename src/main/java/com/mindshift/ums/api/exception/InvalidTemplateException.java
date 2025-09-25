package com.mindshift.ums.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidTemplateException extends UmsException {

    public InvalidTemplateException(String message) {
        super(message, "INVALID_TEMPLATE", HttpStatus.BAD_REQUEST);
    }

    public InvalidTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}