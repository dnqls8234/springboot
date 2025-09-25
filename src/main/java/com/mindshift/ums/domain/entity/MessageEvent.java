package com.mindshift.ums.domain.entity;

import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.EventType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "message_events")
public class MessageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "request_id", nullable = false, length = 50)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private ChannelType channel;

    @Column(length = 50)
    private String provider;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    // Constructors
    public MessageEvent() {}

    public MessageEvent(Message message, String requestId, EventType type, ChannelType channel) {
        this.message = message;
        this.requestId = requestId;
        this.type = type;
        this.channel = channel;
        this.occurredAt = LocalDateTime.now();
    }

    // Static factory methods
    public static MessageEvent createRequestedEvent(Message message) {
        return new MessageEvent(message, message.getRequestId(), EventType.REQUESTED, message.getChannel());
    }

    public static MessageEvent createSentEvent(Message message, String providerMessageId) {
        MessageEvent event = new MessageEvent(message, message.getRequestId(), EventType.SENT, message.getChannel());
        if (providerMessageId != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("providerMessageId", providerMessageId);
            event.setPayload(payload);
        }
        return event;
    }

    public static MessageEvent createDeliveredEvent(Message message) {
        return new MessageEvent(message, message.getRequestId(), EventType.DELIVERED, message.getChannel());
    }

    public static MessageEvent createFailedEvent(Message message, String errorCode, String errorMessage) {
        MessageEvent event = new MessageEvent(message, message.getRequestId(), EventType.FAILED, message.getChannel());
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        return event;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public ChannelType getChannel() { return channel; }
    public void setChannel(ChannelType channel) { this.channel = channel; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}