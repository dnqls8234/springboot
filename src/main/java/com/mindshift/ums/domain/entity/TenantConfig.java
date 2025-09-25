package com.mindshift.ums.domain.entity;

import com.mindshift.ums.domain.enums.TenantStatus;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tenant_configs")
@EntityListeners(AuditingEntityListener.class)
public class TenantConfig {

    @Id
    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "tenant_name", nullable = false, length = 200)
    private String tenantName;

    @Column(name = "api_key", unique = true, nullable = false, length = 100)
    private String apiKey;

    @Column(name = "api_secret", nullable = false, length = 500)
    private String apiSecret;

    @Column(name = "api_key_status", nullable = false, length = 20)
    private String apiKeyStatus = "ACTIVE";

    @Type(JsonType.class)
    @Column(name = "rate_limits", columnDefinition = "jsonb", nullable = false)
    private Map<String, Integer> rateLimits;

    @Type(JsonType.class)
    @Column(name = "allowed_channels", columnDefinition = "jsonb", nullable = false)
    private List<String> allowedChannels;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;

    @Type(JsonType.class)
    @Column(name = "webhook_events", columnDefinition = "jsonb")
    private List<String> webhookEvents;

    @Type(JsonType.class)
    @Column(name = "provider_configs", columnDefinition = "jsonb")
    private Map<String, Object> providerConfigs;

    @Column(name = "billing_plan", length = 50)
    private String billingPlan = "STANDARD";

    @Column(name = "credits_remaining")
    private Integer creditsRemaining;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> features;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private TenantStatus status = TenantStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public TenantConfig() {}

    public TenantConfig(String tenantId, String tenantName, String apiKey, String apiSecret) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    // Business methods
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public boolean isChannelAllowed(String channel) {
        return allowedChannels != null && allowedChannels.contains(channel);
    }

    public Integer getRateLimit(String key) {
        return rateLimits != null ? rateLimits.get(key) : null;
    }

    public boolean isSandboxMode() {
        if (features == null) return false;
        Object sandbox = features.get("sandbox");
        return Boolean.TRUE.equals(sandbox);
    }

    public boolean hasPriorityQueue() {
        if (features == null) return false;
        Object priorityQueue = features.get("priority_queue");
        return Boolean.TRUE.equals(priorityQueue);
    }

    public boolean consumeCredit() {
        if (creditsRemaining == null) {
            return true; // Unlimited credits
        }
        if (creditsRemaining > 0) {
            creditsRemaining--;
            return true;
        }
        return false;
    }

    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }

    public String getApiKeyStatus() { return apiKeyStatus; }
    public void setApiKeyStatus(String apiKeyStatus) { this.apiKeyStatus = apiKeyStatus; }

    public Map<String, Integer> getRateLimits() { return rateLimits; }
    public void setRateLimits(Map<String, Integer> rateLimits) { this.rateLimits = rateLimits; }

    public List<String> getAllowedChannels() { return allowedChannels; }
    public void setAllowedChannels(List<String> allowedChannels) { this.allowedChannels = allowedChannels; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public List<String> getWebhookEvents() { return webhookEvents; }
    public void setWebhookEvents(List<String> webhookEvents) { this.webhookEvents = webhookEvents; }

    public Map<String, Object> getProviderConfigs() { return providerConfigs; }
    public void setProviderConfigs(Map<String, Object> providerConfigs) { this.providerConfigs = providerConfigs; }

    public String getBillingPlan() { return billingPlan; }
    public void setBillingPlan(String billingPlan) { this.billingPlan = billingPlan; }

    public Integer getCreditsRemaining() { return creditsRemaining; }
    public void setCreditsRemaining(Integer creditsRemaining) { this.creditsRemaining = creditsRemaining; }

    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }

    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}