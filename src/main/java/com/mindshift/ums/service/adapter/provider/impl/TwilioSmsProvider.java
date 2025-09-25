package com.mindshift.ums.service.adapter.provider.impl;

import com.mindshift.ums.service.adapter.provider.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Twilio SMS provider implementation.
 * Activated when ums.sms.provider=TWILIO in configuration.
 */
@Component
@ConditionalOnProperty(name = "ums.sms.provider", havingValue = "TWILIO")
public class TwilioSmsProvider implements SmsProvider {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsProvider.class);

    private final WebClient webClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final boolean enabled;

    public TwilioSmsProvider(
            @Value("${ums.sms.twilio.account-sid:}") String accountSid,
            @Value("${ums.sms.twilio.auth-token:}") String authToken,
            @Value("${ums.sms.twilio.from-number:}") String fromNumber,
            @Value("${ums.sms.twilio.api-url:https://api.twilio.com}") String apiUrl,
            @Value("${ums.sms.twilio.enabled:false}") boolean enabled) {

        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.enabled = enabled && !accountSid.isEmpty() && !authToken.isEmpty();

        String credentials = accountSid + ":" + authToken;
        String base64Credentials = Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        this.webClient = WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Authorization", "Basic " + base64Credentials)
            .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

        if (this.enabled) {
            logger.info("Twilio SMS provider initialized with account: {}", accountSid);
        } else {
            logger.warn("Twilio SMS provider is disabled or not properly configured");
        }
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return 10; // Higher priority
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String message, Map<String, Object> metadata) {
        return sendSms(phoneNumber, null, message, metadata);
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String title, String message, Map<String, Object> metadata) {
        if (!enabled) {
            return SmsResult.failure("PROVIDER_DISABLED", "Twilio provider is not enabled");
        }

        logger.info("Sending SMS via Twilio to: {}", phoneNumber);

        try {
            // Build form data for Twilio API
            String formData = buildFormData(phoneNumber, message, metadata);

            TwilioResponse response = webClient.post()
                .uri("/2010-04-01/Accounts/" + accountSid + "/Messages.json")
                .bodyValue(formData)
                .retrieve()
                .onStatus(status -> status.value() >= 400, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error("Twilio API error: {} - {}", clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException("Twilio API error: " + body));
                        })
                )
                .bodyToMono(TwilioResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response != null && response.sid != null) {
                logger.info("SMS sent successfully via Twilio. SID: {}", response.sid);

                Map<String, Object> resultMetadata = new HashMap<>();
                resultMetadata.put("provider", "TWILIO");
                resultMetadata.put("sid", response.sid);
                resultMetadata.put("status", response.status);
                resultMetadata.put("to", response.to);
                resultMetadata.put("from", response.from);

                return SmsResult.success(response.sid, resultMetadata);
            } else {
                return SmsResult.failure("SEND_FAILED", "Invalid response from Twilio");
            }

        } catch (Exception e) {
            logger.error("Failed to send SMS via Twilio", e);
            return SmsResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    @Override
    public DeliveryStatus getDeliveryStatus(String messageId) {
        if (!enabled) {
            return DeliveryStatus.UNKNOWN;
        }

        try {
            TwilioResponse response = webClient.get()
                .uri("/2010-04-01/Accounts/" + accountSid + "/Messages/" + messageId + ".json")
                .retrieve()
                .bodyToMono(TwilioResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            if (response != null && response.status != null) {
                switch (response.status.toLowerCase()) {
                    case "delivered":
                        return DeliveryStatus.DELIVERED;
                    case "sent":
                    case "sending":
                        return DeliveryStatus.SENT;
                    case "failed":
                    case "undelivered":
                        return DeliveryStatus.FAILED;
                    case "queued":
                    case "accepted":
                        return DeliveryStatus.PENDING;
                    default:
                        return DeliveryStatus.UNKNOWN;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get delivery status from Twilio", e);
        }

        return DeliveryStatus.UNKNOWN;
    }

    private String buildFormData(String phoneNumber, String message, Map<String, Object> metadata) {
        StringBuilder formData = new StringBuilder();
        formData.append("To=").append(phoneNumber);
        formData.append("&From=").append(fromNumber);
        formData.append("&Body=").append(urlEncode(message));

        // Add webhook URL if provided in metadata
        if (metadata != null && metadata.containsKey("statusCallback")) {
            formData.append("&StatusCallback=").append(metadata.get("statusCallback"));
        }

        return formData.toString();
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Twilio API response structure
     */
    private static class TwilioResponse {
        public String sid;
        public String status;
        public String to;
        public String from;
        public String body;
        public String errorCode;
        public String errorMessage;
    }
}