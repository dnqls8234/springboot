package com.mindshift.ums.service.adapter.provider.impl;

import com.mindshift.ums.service.adapter.provider.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Solapi (CoolSMS) provider implementation.
 * Activated when ums.sms.provider=SOLAPI in configuration.
 */
@Component
@ConditionalOnProperty(name = "ums.sms.provider", havingValue = "SOLAPI", matchIfMissing = true)
public class SolapiSmsProvider implements SmsProvider {

    private static final Logger logger = LoggerFactory.getLogger(SolapiSmsProvider.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String apiSecret;
    private final String fromNumber;
    private final boolean enabled;

    public SolapiSmsProvider(
            @Value("${ums.sms.solapi.api-key:${ums.sms.api-key:}}") String apiKey,
            @Value("${ums.sms.solapi.api-secret:${ums.sms.api-secret:}}") String apiSecret,
            @Value("${ums.sms.solapi.from-number:${ums.sms.from-number:}}") String fromNumber,
            @Value("${ums.sms.solapi.api-url:https://api.solapi.com}") String apiUrl,
            @Value("${ums.sms.solapi.enabled:true}") boolean enabled) {

        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.fromNumber = fromNumber;
        this.enabled = enabled && !apiKey.isEmpty() && !apiSecret.isEmpty();

        this.webClient = WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();

        if (this.enabled) {
            logger.info("Solapi SMS provider initialized");
        } else {
            logger.warn("Solapi SMS provider is disabled or not properly configured");
        }
    }

    @Override
    public String getProviderName() {
        return "SOLAPI";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return 5; // Default priority
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String message, Map<String, Object> metadata) {
        return sendSms(phoneNumber, null, message, metadata);
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String title, String message, Map<String, Object> metadata) {
        if (!enabled) {
            return SmsResult.failure("PROVIDER_DISABLED", "Solapi provider is not enabled");
        }

        logger.info("Sending SMS via Solapi to: {}", phoneNumber);

        try {
            Map<String, Object> requestBody = buildRequestBody(phoneNumber, title, message);
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            String signature = generateSignature(timestamp);

            SolapiResponse response = webClient.post()
                .uri("/messages/v4/send")
                .header("Authorization", "HMAC-SHA256 apiKey=" + apiKey + ", date=" + timestamp + ", signature=" + signature)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() >= 400, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error("Solapi API error: {} - {}", clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException("Solapi API error: " + body));
                        })
                )
                .bodyToMono(SolapiResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response != null && response.groupId != null) {
                logger.info("SMS sent successfully via Solapi. Group ID: {}", response.groupId);

                Map<String, Object> resultMetadata = new HashMap<>();
                resultMetadata.put("provider", "SOLAPI");
                resultMetadata.put("groupId", response.groupId);
                resultMetadata.put("messageId", response.messageId);
                resultMetadata.put("statusCode", response.statusCode);

                return SmsResult.success(response.groupId, resultMetadata);
            } else {
                return SmsResult.failure("SEND_FAILED", "Invalid response from Solapi");
            }

        } catch (Exception e) {
            logger.error("Failed to send SMS via Solapi", e);
            return SmsResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(String phoneNumber, String title, String message) {
        Map<String, Object> requestBody = new HashMap<>();

        // Message info
        Map<String, Object> messageInfo = new HashMap<>();
        messageInfo.put("to", normalizePhoneNumber(phoneNumber));
        messageInfo.put("from", fromNumber);
        messageInfo.put("text", message);

        // Determine message type based on length
        String type = determineMessageType(message);
        messageInfo.put("type", type);

        // Add subject for LMS/MMS if title is provided
        if (title != null && !title.isEmpty() && !"SMS".equals(type)) {
            messageInfo.put("subject", title);
        }

        // Add message to array
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(messageInfo);

        requestBody.put("messages", messages);

        return requestBody;
    }

    private String determineMessageType(String message) {
        int length = message.length();
        if (length <= 90) {
            return "SMS";
        } else if (length <= 2000) {
            return "LMS";
        } else {
            return "MMS";
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non-digit characters except +
        String normalized = phoneNumber.replaceAll("[^+\\d]", "");

        // Convert to Korean format if needed
        if (!normalized.startsWith("+")) {
            if (normalized.startsWith("010") || normalized.startsWith("011") ||
                normalized.startsWith("016") || normalized.startsWith("017") ||
                normalized.startsWith("018") || normalized.startsWith("019")) {
                // Remove leading 0 for Korean numbers
                normalized = normalized.substring(1);
            }
        } else if (normalized.startsWith("+82")) {
            // Remove +82 country code
            normalized = normalized.substring(3);
        }

        return normalized;
    }

    private String generateSignature(String timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String data = timestamp + apiKey;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Solapi API response structure
     */
    private static class SolapiResponse {
        public String groupId;
        public String messageId;
        public String statusCode;
        public String statusMessage;
        public String accountId;
        public String type;
        public Map<String, Object> results;
    }
}