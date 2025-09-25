package com.mindshift.ums.domain.entity;

import com.mindshift.ums.domain.enums.RecipientType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "recipient_prefs")
@EntityListeners(AuditingEntityListener.class)
@IdClass(RecipientPref.RecipientPrefId.class)
public class RecipientPref {

    @Id
    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Id
    @Column(name = "recipient_key", length = 255)
    private String recipientKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    private RecipientType recipientType;

    @Type(JsonType.class)
    @Column(name = "channel_prefs", columnDefinition = "jsonb", nullable = false)
    private Map<String, Boolean> channelPrefs;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(length = 50)
    private String timezone = "Asia/Seoul";

    @Column(name = "opted_out", nullable = false)
    private Boolean optedOut = false;

    @Column(name = "opted_out_at")
    private LocalDateTime optedOutAt;

    @Column(name = "opted_out_reason", columnDefinition = "TEXT")
    private String optedOutReason;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Boolean> subscriptions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "max_daily_messages")
    private Integer maxDailyMessages;

    @Column(name = "recipient_id", length = 255)
    private String recipientId;

    // Constructors
    public RecipientPref() {}

    public RecipientPref(String tenantId, String recipientKey, RecipientType recipientType) {
        this.tenantId = tenantId;
        this.recipientKey = recipientKey;
        this.recipientType = recipientType;
    }

    // Business methods
    public boolean isChannelAllowed(String channel) {
        if (channelPrefs == null) return true;
        return channelPrefs.getOrDefault(channel, true);
    }

    public boolean isInQuietHours() {
        return isInQuietHours(LocalTime.now());
    }

    public boolean isInQuietHours(LocalTime time) {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }

        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return !time.isBefore(quietHoursStart) && !time.isAfter(quietHoursEnd);
        } else {
            return !time.isBefore(quietHoursStart) || !time.isAfter(quietHoursEnd);
        }
    }

    public void optOut(String reason) {
        this.optedOut = true;
        this.optedOutAt = LocalDateTime.now();
        this.optedOutReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void optIn() {
        this.optedOut = false;
        this.optedOutAt = null;
        this.optedOutReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getRecipientKey() { return recipientKey; }
    public void setRecipientKey(String recipientKey) { this.recipientKey = recipientKey; }

    public RecipientType getRecipientType() { return recipientType; }
    public void setRecipientType(RecipientType recipientType) { this.recipientType = recipientType; }

    public Map<String, Boolean> getChannelPrefs() { return channelPrefs; }
    public void setChannelPrefs(Map<String, Boolean> channelPrefs) { this.channelPrefs = channelPrefs; }

    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Boolean getOptedOut() { return optedOut; }
    public void setOptedOut(Boolean optedOut) { this.optedOut = optedOut; }

    public LocalDateTime getOptedOutAt() { return optedOutAt; }
    public void setOptedOutAt(LocalDateTime optedOutAt) { this.optedOutAt = optedOutAt; }

    public String getOptedOutReason() { return optedOutReason; }
    public void setOptedOutReason(String optedOutReason) { this.optedOutReason = optedOutReason; }

    public Map<String, Boolean> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(Map<String, Boolean> subscriptions) { this.subscriptions = subscriptions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Integer getMaxDailyMessages() { return maxDailyMessages; }
    public void setMaxDailyMessages(Integer maxDailyMessages) { this.maxDailyMessages = maxDailyMessages; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public boolean isOptedOut() { return optedOut != null && optedOut; }

    // Composite Key Class
    public static class RecipientPrefId implements Serializable {
        private String tenantId;
        private String recipientKey;

        public RecipientPrefId() {}

        public RecipientPrefId(String tenantId, String recipientKey) {
            this.tenantId = tenantId;
            this.recipientKey = recipientKey;
        }

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }

        public String getRecipientKey() { return recipientKey; }
        public void setRecipientKey(String recipientKey) { this.recipientKey = recipientKey; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecipientPrefId that = (RecipientPrefId) o;
            return Objects.equals(tenantId, that.tenantId) &&
                   Objects.equals(recipientKey, that.recipientKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, recipientKey);
        }
    }
}