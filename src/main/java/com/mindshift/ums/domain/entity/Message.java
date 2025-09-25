package com.mindshift.ums.domain.entity;

import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.MessagePriority;
import com.mindshift.ums.domain.enums.MessageStatus;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "messages")
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true, nullable = false, length = 50)
    private String requestId;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private ChannelType channel;

    @Column(nullable = false, length = 10)
    private String locale = "ko-KR";

    @Type(JsonType.class)
    @Column(name = "to_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> toJson;

    @Column(name = "rendered_title", columnDefinition = "TEXT")
    private String renderedTitle;

    @Column(name = "rendered_body", columnDefinition = "TEXT", nullable = false)
    private String renderedBody;

    @Type(JsonType.class)
    @Column(name = "rendered_buttons", columnDefinition = "jsonb")
    private Map<String, Object> renderedButtons;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> routing;

    @Column(name = "ttl_expires_at")
    private LocalDateTime ttlExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(10)")
    private MessagePriority priority = MessagePriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "provider_status_code", length = 50)
    private String providerStatusCode;

    @Column(nullable = false)
    private Integer retries = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonType.class)
    @Column(name = "error_details", columnDefinition = "jsonb")
    private Map<String, Object> errorDetails;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> attachments;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> meta;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageEvent> events = new ArrayList<>();

    // Constructors
    public Message() {}

    public Message(String requestId, String tenantId, ChannelType channel, String templateCode) {
        this.requestId = requestId;
        this.tenantId = tenantId;
        this.channel = channel;
        this.templateCode = templateCode;
    }

    // Business methods
    public void markAsSent(String providerMessageId) {
        this.status = MessageStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.status = MessageStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorCode, String errorMessage, Map<String, Object> details) {
        this.status = MessageStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetails = details;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = MessageStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementRetry() {
        this.retries++;
        this.lastRetryAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRetryable() {
        return status == MessageStatus.FAILED && retries < 3;
    }

    public boolean isExpired() {
        return ttlExpiresAt != null && ttlExpiresAt.isBefore(LocalDateTime.now());
    }

    public String getPhone() {
        return toJson != null ? (String) toJson.get("phone") : null;
    }

    public String getEmail() {
        return toJson != null ? (String) toJson.get("email") : null;
    }

    public String getPushToken() {
        return toJson != null ? (String) toJson.get("pushToken") : null;
    }

    @SuppressWarnings("unchecked")
    public String getKakaoUserId() {
        if (toJson != null && toJson.get("kakao") instanceof Map) {
            Map<String, Object> kakao = (Map<String, Object>) toJson.get("kakao");
            return (String) kakao.get("userId");
        }
        return null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public ChannelType getChannel() { return channel; }
    public void setChannel(ChannelType channel) { this.channel = channel; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public Map<String, Object> getToJson() { return toJson; }
    public void setToJson(Map<String, Object> toJson) { this.toJson = toJson; }

    public String getRenderedTitle() { return renderedTitle; }
    public void setRenderedTitle(String renderedTitle) { this.renderedTitle = renderedTitle; }

    public String getRenderedBody() { return renderedBody; }
    public void setRenderedBody(String renderedBody) { this.renderedBody = renderedBody; }

    public Map<String, Object> getRenderedButtons() { return renderedButtons; }
    public void setRenderedButtons(Map<String, Object> renderedButtons) { this.renderedButtons = renderedButtons; }

    public Map<String, Object> getRouting() { return routing; }
    public void setRouting(Map<String, Object> routing) { this.routing = routing; }

    public LocalDateTime getTtlExpiresAt() { return ttlExpiresAt; }
    public void setTtlExpiresAt(LocalDateTime ttlExpiresAt) { this.ttlExpiresAt = ttlExpiresAt; }

    public MessagePriority getPriority() { return priority; }
    public void setPriority(MessagePriority priority) { this.priority = priority; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }

    public String getProviderStatusCode() { return providerStatusCode; }
    public void setProviderStatusCode(String providerStatusCode) { this.providerStatusCode = providerStatusCode; }

    public Integer getRetries() { return retries; }
    public void setRetries(Integer retries) { this.retries = retries; }

    public LocalDateTime getLastRetryAt() { return lastRetryAt; }
    public void setLastRetryAt(LocalDateTime lastRetryAt) { this.lastRetryAt = lastRetryAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, Object> getErrorDetails() { return errorDetails; }
    public void setErrorDetails(Map<String, Object> errorDetails) { this.errorDetails = errorDetails; }

    public List<Map<String, Object>> getAttachments() { return attachments; }
    public void setAttachments(List<Map<String, Object>> attachments) { this.attachments = attachments; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }

    public List<MessageEvent> getEvents() { return events; }
    public void setEvents(List<MessageEvent> events) { this.events = events; }
}