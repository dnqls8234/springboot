package com.mindshift.ums.api.exception;

import org.springframework.http.HttpStatus;

public class TenantSuspendedException extends UmsException {

    public TenantSuspendedException(String tenantId) {
        super("Tenant is suspended: " + tenantId, "TENANT_SUSPENDED", HttpStatus.FORBIDDEN);
    }

    public TenantSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }
}