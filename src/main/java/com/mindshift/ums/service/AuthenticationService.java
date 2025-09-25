package com.mindshift.ums.service;

import com.mindshift.ums.api.exception.AuthenticationException;
import com.mindshift.ums.api.exception.TenantNotFoundException;
import com.mindshift.ums.api.exception.TenantSuspendedException;
import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.enums.TenantStatus;
import com.mindshift.ums.repository.TenantConfigRepository;
import com.mindshift.ums.security.HmacAuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for handling tenant authentication and authorization.
 */
@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final TenantConfigRepository tenantConfigRepository;
    private final HmacAuthenticationHelper hmacHelper;

    @Autowired
    public AuthenticationService(TenantConfigRepository tenantConfigRepository,
                               HmacAuthenticationHelper hmacHelper) {
        this.tenantConfigRepository = tenantConfigRepository;
        this.hmacHelper = hmacHelper;
    }

    /**
     * Authenticate and authorize a tenant from authorization header.
     *
     * @param authorization Authorization header
     * @return Authenticated tenant config
     * @throws AuthenticationException if authentication fails
     * @throws TenantNotFoundException if tenant not found
     * @throws TenantSuspendedException if tenant is suspended
     */
    @Cacheable(value = "tenants", key = "#authorization")
    public TenantConfig authenticateTenant(String authorization) {
        logger.debug("Authenticating tenant");

        if (authorization == null || authorization.trim().isEmpty()) {
            throw new AuthenticationException("Authorization header is required");
        }

        String apiKey = hmacHelper.extractApiKey(authorization);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AuthenticationException("Invalid authorization format");
        }

        TenantConfig tenant = tenantConfigRepository.findActiveByApiKey(apiKey)
            .orElseThrow(() -> new TenantNotFoundException(apiKey));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new TenantSuspendedException(tenant.getTenantId());
        }

        logger.debug("Tenant authenticated successfully: {}", tenant.getTenantId());
        return tenant;
    }

    /**
     * Validate HMAC signature for request authentication.
     *
     * @param method HTTP method
     * @param uri Request URI
     * @param body Request body
     * @param timestamp Request timestamp
     * @param signature Request signature
     * @param tenant Tenant config containing secret
     * @return true if signature is valid
     */
    public boolean validateHmacSignature(String method, String uri, String body,
                                       long timestamp, String signature, TenantConfig tenant) {

        if (tenant.getApiSecret() == null) {
            logger.warn("No API secret configured for tenant: {}", tenant.getTenantId());
            return false;
        }

        return hmacHelper.validateSignature(method, uri, body, timestamp, signature, tenant.getApiSecret());
    }

    /**
     * Extract API key from authorization header.
     *
     * @param authorization Authorization header
     * @return API key or null if invalid format
     */
    public String extractApiKey(String authorization) {
        return hmacHelper.extractApiKey(authorization);
    }
}