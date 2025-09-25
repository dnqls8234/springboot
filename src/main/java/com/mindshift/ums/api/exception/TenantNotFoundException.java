package com.mindshift.ums.api.exception;

import org.springframework.http.HttpStatus;

public class TenantNotFoundException extends UmsException {

    public TenantNotFoundException(String apiKey) {
        super("Tenant not found with API key: " + apiKey, "TENANT_NOT_FOUND", HttpStatus.UNAUTHORIZED);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}