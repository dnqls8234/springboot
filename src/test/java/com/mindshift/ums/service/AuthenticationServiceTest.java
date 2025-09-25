package com.mindshift.ums.service;

import com.mindshift.ums.api.exception.AuthenticationException;
import com.mindshift.ums.api.exception.TenantNotFoundException;
import com.mindshift.ums.api.exception.TenantSuspendedException;
import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.enums.TenantStatus;
import com.mindshift.ums.repository.TenantConfigRepository;
import com.mindshift.ums.security.HmacAuthenticationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private TenantConfigRepository tenantConfigRepository;

    @Mock
    private HmacAuthenticationHelper hmacHelper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private TenantConfig testTenant;

    @BeforeEach
    void setUp() {
        testTenant = new TenantConfig();
        testTenant.setTenantId("test-tenant");
        testTenant.setApiKey("test-api-key");
        testTenant.setApiSecret("test-secret");
        testTenant.setStatus(TenantStatus.ACTIVE);
    }

    @Test
    void authenticateTenant_Success() {
        // Given
        String authorization = "Bearer test-api-key";
        when(hmacHelper.extractApiKey(authorization)).thenReturn("test-api-key");
        when(tenantConfigRepository.findActiveByApiKey("test-api-key"))
            .thenReturn(Optional.of(testTenant));

        // When
        TenantConfig result = authenticationService.authenticateTenant(authorization);

        // Then
        assertNotNull(result);
        assertEquals("test-tenant", result.getTenantId());
        assertEquals("test-api-key", result.getApiKey());
        assertEquals(TenantStatus.ACTIVE, result.getStatus());

        verify(hmacHelper).extractApiKey(authorization);
        verify(tenantConfigRepository).findActiveByApiKey("test-api-key");
    }

    @Test
    void authenticateTenant_NullAuthorization() {
        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateTenant(null);
        });

        verify(hmacHelper, never()).extractApiKey(anyString());
        verify(tenantConfigRepository, never()).findActiveByApiKey(anyString());
    }

    @Test
    void authenticateTenant_EmptyAuthorization() {
        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateTenant("");
        });

        verify(hmacHelper, never()).extractApiKey(anyString());
        verify(tenantConfigRepository, never()).findActiveByApiKey(anyString());
    }

    @Test
    void authenticateTenant_InvalidApiKey() {
        // Given
        String authorization = "Bearer invalid-key";
        when(hmacHelper.extractApiKey(authorization)).thenReturn("invalid-key");
        when(tenantConfigRepository.findActiveByApiKey("invalid-key"))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> {
            authenticationService.authenticateTenant(authorization);
        });

        verify(hmacHelper).extractApiKey(authorization);
        verify(tenantConfigRepository).findActiveByApiKey("invalid-key");
    }

    @Test
    void authenticateTenant_SuspendedTenant() {
        // Given
        testTenant.setStatus(TenantStatus.SUSPENDED);
        String authorization = "Bearer test-api-key";
        when(hmacHelper.extractApiKey(authorization)).thenReturn("test-api-key");
        when(tenantConfigRepository.findActiveByApiKey("test-api-key"))
            .thenReturn(Optional.of(testTenant));

        // When & Then
        assertThrows(TenantSuspendedException.class, () -> {
            authenticationService.authenticateTenant(authorization);
        });

        verify(hmacHelper).extractApiKey(authorization);
        verify(tenantConfigRepository).findActiveByApiKey("test-api-key");
    }

    @Test
    void validateHmacSignature_Success() {
        // Given
        String method = "POST";
        String uri = "/v1/messages";
        String body = "{\"test\":\"data\"}";
        long timestamp = System.currentTimeMillis() / 1000;
        String signature = "valid-signature";

        when(hmacHelper.validateSignature(method, uri, body, timestamp, signature, testTenant.getApiSecret()))
            .thenReturn(true);

        // When
        boolean result = authenticationService.validateHmacSignature(
            method, uri, body, timestamp, signature, testTenant);

        // Then
        assertTrue(result);

        verify(hmacHelper).validateSignature(method, uri, body, timestamp, signature, testTenant.getApiSecret());
    }

    @Test
    void validateHmacSignature_NoSecret() {
        // Given
        testTenant.setApiSecret(null);
        String method = "POST";
        String uri = "/v1/messages";
        String body = "{\"test\":\"data\"}";
        long timestamp = System.currentTimeMillis() / 1000;
        String signature = "any-signature";

        // When
        boolean result = authenticationService.validateHmacSignature(
            method, uri, body, timestamp, signature, testTenant);

        // Then
        assertFalse(result);

        verify(hmacHelper, never()).validateSignature(anyString(), anyString(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void extractApiKey() {
        // Given
        String authorization = "Bearer test-api-key";
        when(hmacHelper.extractApiKey(authorization)).thenReturn("test-api-key");

        // When
        String result = authenticationService.extractApiKey(authorization);

        // Then
        assertEquals("test-api-key", result);

        verify(hmacHelper).extractApiKey(authorization);
    }
}