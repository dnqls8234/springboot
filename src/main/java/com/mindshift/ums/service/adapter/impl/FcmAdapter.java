package com.mindshift.ums.service.adapter.impl;

import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.service.adapter.ChannelAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Firebase Cloud Messaging (FCM) channel adapter for push notifications.
 */
@Component
public class FcmAdapter implements ChannelAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FcmAdapter.class);

    private final WebClient webClient;
    private final String serverKey;
    private final String projectId;

    public FcmAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${ums.fcm.server-key}") String serverKey,
            @Value("${ums.fcm.project-id}") String projectId) {

        this.webClient = webClientBuilder
            .baseUrl("https://fcm.googleapis.com")
            .defaultHeader("Authorization", "key=" + serverKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();

        this.serverKey = serverKey;
        this.projectId = projectId;
    }

    @Override
    public SendResult send(Message message) {
        logger.info("Sending FCM push notification: {}", message.getRequestId());

        try {
            Map<String, Object> recipient = message.getToJson();
            String pushToken = (String) recipient.get("pushToken");

            if (pushToken == null || pushToken.trim().isEmpty()) {
                return SendResult.failure("INVALID_PUSH_TOKEN", "Push token not found in recipient data");
            }

            Map<String, Object> requestBody = buildRequestBody(message, pushToken);

            String providerMessageId = webClient.post()
                .uri("/fcm/send")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    logger.error("FCM API error: {} for message {}", response.statusCode(), message.getRequestId());
                    return response.bodyToMono(String.class)
                        .doOnNext(body -> logger.error("FCM API error body: {}", body))
                        .then(Mono.error(new RuntimeException("FCM API error: " + response.statusCode())));
                })
                .bodyToMono(FcmResponse.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> {
                    if (response.success == 1) {
                        return String.valueOf(response.multicastId);
                    } else {
                        throw new RuntimeException("FCM send failed: " + response.results);
                    }
                })
                .block();

            logger.info("FCM push notification sent successfully: {} -> {}", message.getRequestId(), providerMessageId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("channel", "FCM_PUSH");
            metadata.put("pushToken", pushToken);
            metadata.put("projectId", projectId);

            return SendResult.success(providerMessageId, metadata);

        } catch (Exception e) {
            logger.error("Failed to send FCM push notification: {}", message.getRequestId(), e);
            return SendResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(Message message, String pushToken) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("to", pushToken);

        // Build notification payload
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", message.getRenderedTitle() != null ? message.getRenderedTitle() : "Notification");
        notification.put("body", message.getRenderedBody());

        // Add icon and sound if available in meta
        if (message.getMeta() != null) {
            Map<String, Object> meta = message.getMeta();
            if (meta.containsKey("icon")) {
                notification.put("icon", meta.get("icon"));
            }
            if (meta.containsKey("sound")) {
                notification.put("sound", meta.get("sound"));
            }
            if (meta.containsKey("click_action")) {
                notification.put("click_action", meta.get("click_action"));
            }
        }

        requestBody.put("notification", notification);

        // Build data payload for custom handling
        Map<String, Object> data = new HashMap<>();
        data.put("ums_request_id", message.getRequestId());
        data.put("ums_template_code", message.getTemplateCode());
        data.put("ums_channel", "FCM_PUSH");

        // Add custom data from meta
        if (message.getMeta() != null) {
            message.getMeta().forEach((key, value) -> {
                if (!key.equals("icon") && !key.equals("sound") && !key.equals("click_action")) {
                    data.put("custom_" + key, value.toString());
                }
            });
        }

        requestBody.put("data", data);

        // Set priority based on message priority
        switch (message.getPriority()) {
            case HIGH:
                requestBody.put("priority", "high");
                break;
            case NORMAL:
                requestBody.put("priority", "normal");
                break;
            case LOW:
                requestBody.put("priority", "normal");
                break;
        }

        // Set TTL if specified
        if (message.getTtlExpiresAt() != null) {
            long ttlSeconds = Duration.between(message.getCreatedAt(), message.getTtlExpiresAt()).getSeconds();
            if (ttlSeconds > 0) {
                requestBody.put("time_to_live", (int) ttlSeconds);
            }
        }

        return requestBody;
    }

    @Override
    public String getChannelType() {
        return ChannelType.FCM_PUSH.name();
    }

    @Override
    public boolean canHandle(Message message) {
        if (message.getChannel() != ChannelType.FCM_PUSH) {
            return false;
        }

        Map<String, Object> recipient = message.getToJson();
        return recipient != null && recipient.containsKey("pushToken");
    }

    /**
     * Response structure from FCM API
     */
    private static class FcmResponse {
        public long multicastId;
        public int success;
        public int failure;
        public int canonicalIds;
        public Object results; // Can be array or object depending on response
    }
}