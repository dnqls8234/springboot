package com.mindshift.ums.service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for producing Kafka events related to UMS operations.
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topic names
    private static final String TOPIC_MESSAGE_REQUESTED = "ums.message.requested.v1";
    private static final String TOPIC_MESSAGE_DELIVERY = "ums.message.delivery.v1";
    private static final String TOPIC_MESSAGE_STATUS = "ums.message.status.v1";
    private static final String TOPIC_TENANT_ACTIVITY = "ums.tenant.activity.v1";

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a message requested event.
     *
     * @param requestId    The message request ID
     * @param tenantId     The tenant ID
     * @param channel      The channel type
     * @param templateCode The template code
     * @param priority     The message priority
     */
    public void publishMessageRequested(String requestId, String tenantId, String channel,
                                      String templateCode, String priority) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "MESSAGE_REQUESTED");
        event.put("requestId", requestId);
        event.put("tenantId", tenantId);
        event.put("channel", channel);
        event.put("templateCode", templateCode);
        event.put("priority", priority);
        event.put("timestamp", LocalDateTime.now().toString());

        publishEvent(TOPIC_MESSAGE_REQUESTED, requestId, event);
    }

    /**
     * Publish a delivery status update event.
     *
     * @param providerMessageId The provider's message ID
     * @param status           The delivery status
     * @param errorCode        Error code if failed
     * @param errorMessage     Error message if failed
     */
    public void publishDeliveryStatus(String providerMessageId, String status,
                                    String errorCode, String errorMessage) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "DELIVERY_STATUS");
        event.put("providerMessageId", providerMessageId);
        event.put("status", status);
        event.put("timestamp", LocalDateTime.now().toString());

        if (errorCode != null) {
            event.put("errorCode", errorCode);
        }
        if (errorMessage != null) {
            event.put("errorMessage", errorMessage);
        }

        publishEvent(TOPIC_MESSAGE_DELIVERY, providerMessageId, event);
    }

    /**
     * Publish a message status change event.
     *
     * @param requestId   The message request ID
     * @param oldStatus   Previous status
     * @param newStatus   New status
     * @param tenantId    The tenant ID
     */
    public void publishMessageStatusChange(String requestId, String oldStatus, String newStatus, String tenantId) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "MESSAGE_STATUS_CHANGE");
        event.put("requestId", requestId);
        event.put("oldStatus", oldStatus);
        event.put("newStatus", newStatus);
        event.put("tenantId", tenantId);
        event.put("timestamp", LocalDateTime.now().toString());

        publishEvent(TOPIC_MESSAGE_STATUS, requestId, event);
    }

    /**
     * Publish tenant activity event for monitoring and analytics.
     *
     * @param tenantId     The tenant ID
     * @param activityType Type of activity
     * @param details      Activity details
     */
    public void publishTenantActivity(String tenantId, String activityType, Map<String, Object> details) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "TENANT_ACTIVITY");
        event.put("tenantId", tenantId);
        event.put("activityType", activityType);
        event.put("timestamp", LocalDateTime.now().toString());

        if (details != null) {
            event.put("details", details);
        }

        publishEvent(TOPIC_TENANT_ACTIVITY, tenantId, event);
    }

    /**
     * Generic method to publish events to Kafka.
     *
     * @param topic The topic to publish to
     * @param key   The message key
     * @param event The event data
     */
    private void publishEvent(String topic, String key, Map<String, Object> event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    logger.error("Failed to publish event to topic {} with key {}: {}",
                        topic, key, event, exception);
                } else {
                    logger.debug("Event published successfully to topic {} with key {}: partition={}, offset={}",
                        topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            logger.error("Failed to send event to topic {} with key {}: {}", topic, key, event, e);
        }
    }

    /**
     * Publish events synchronously (for critical events where we need to ensure delivery).
     *
     * @param topic The topic to publish to
     * @param key   The message key
     * @param event The event data
     * @throws Exception if publishing fails
     */
    public void publishEventSync(String topic, String key, Map<String, Object> event) throws Exception {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, key, event).get();
            logger.debug("Event published synchronously to topic {} with key {}: partition={}, offset={}",
                topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());

        } catch (Exception e) {
            logger.error("Failed to send event synchronously to topic {} with key {}: {}", topic, key, event, e);
            throw e;
        }
    }
}