package com.mindshift.ums.service.adapter.provider.impl;

import com.mindshift.ums.service.adapter.provider.EmailProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * SendGrid email provider implementation.
 * Activated when ums.email.provider=SENDGRID in configuration.
 */
@Component
@ConditionalOnProperty(name = "ums.email.provider", havingValue = "SENDGRID")
public class SendGridEmailProvider implements EmailProvider {

    private static final Logger logger = LoggerFactory.getLogger(SendGridEmailProvider.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;
    private final boolean enabled;

    public SendGridEmailProvider(
            @Value("${ums.email.sendgrid.api-key:}") String apiKey,
            @Value("${ums.email.sendgrid.from-email:${ums.email.from-email:}}") String fromEmail,
            @Value("${ums.email.sendgrid.from-name:${ums.email.from-name:}}") String fromName,
            @Value("${ums.email.sendgrid.api-url:https://api.sendgrid.com}") String apiUrl,
            @Value("${ums.email.sendgrid.enabled:false}") boolean enabled) {

        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.enabled = enabled && !apiKey.isEmpty();

        this.webClient = WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();

        if (this.enabled) {
            logger.info("SendGrid email provider initialized");
        } else {
            logger.warn("SendGrid email provider is disabled or not properly configured");
        }
    }

    @Override
    public String getProviderName() {
        return "SENDGRID";
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
    public EmailResult sendEmail(String to, String subject, String body, boolean isHtml, Map<String, Object> metadata) {
        return sendEmail(to, subject, body, isHtml, null, metadata);
    }

    @Override
    public EmailResult sendEmail(String to, String subject, String body, boolean isHtml,
                                List<Attachment> attachments, Map<String, Object> metadata) {
        if (!enabled) {
            return EmailResult.failure("PROVIDER_DISABLED", "SendGrid provider is not enabled");
        }

        logger.info("Sending email via SendGrid to: {}", to);

        try {
            Map<String, Object> requestBody = buildRequestBody(to, subject, body, isHtml, attachments, metadata);

            SendGridResponse response = webClient.post()
                .uri("/v3/mail/send")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() >= 400, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.error("SendGrid API error: {} - {}", clientResponse.statusCode(), errorBody);
                            return Mono.error(new RuntimeException("SendGrid API error: " + errorBody));
                        })
                )
                .bodyToMono(SendGridResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            // SendGrid returns 202 Accepted with no body on success
            String messageId = UUID.randomUUID().toString();
            logger.info("Email sent successfully via SendGrid. Message ID: {}", messageId);

            Map<String, Object> resultMetadata = new HashMap<>();
            resultMetadata.put("provider", "SENDGRID");
            resultMetadata.put("messageId", messageId);

            return EmailResult.success(messageId, resultMetadata);

        } catch (Exception e) {
            logger.error("Failed to send email via SendGrid", e);
            return EmailResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    @Override
    public EmailResult sendBulkEmail(List<String> to, List<String> cc, List<String> bcc,
                                    String subject, String body, boolean isHtml,
                                    Map<String, Object> metadata) {
        if (!enabled) {
            return EmailResult.failure("PROVIDER_DISABLED", "SendGrid provider is not enabled");
        }

        try {
            Map<String, Object> requestBody = buildBulkRequestBody(to, cc, bcc, subject, body, isHtml, metadata);

            webClient.post()
                .uri("/v3/mail/send")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() >= 400, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.error("SendGrid API error: {} - {}", clientResponse.statusCode(), errorBody);
                            return Mono.error(new RuntimeException("SendGrid API error: " + errorBody));
                        })
                )
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(30))
                .block();

            String messageId = UUID.randomUUID().toString();
            logger.info("Bulk email sent successfully via SendGrid. Message ID: {}", messageId);

            Map<String, Object> resultMetadata = new HashMap<>();
            resultMetadata.put("provider", "SENDGRID");
            resultMetadata.put("messageId", messageId);
            resultMetadata.put("recipientCount", to.size());

            return EmailResult.success(messageId, resultMetadata);

        } catch (Exception e) {
            logger.error("Failed to send bulk email via SendGrid", e);
            return EmailResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(String to, String subject, String body, boolean isHtml,
                                                 List<Attachment> attachments, Map<String, Object> metadata) {
        Map<String, Object> requestBody = new HashMap<>();

        // From
        Map<String, String> from = new HashMap<>();
        from.put("email", fromEmail);
        from.put("name", fromName);
        requestBody.put("from", from);

        // Personalizations (recipients)
        List<Map<String, Object>> personalizations = new ArrayList<>();
        Map<String, Object> personalization = new HashMap<>();

        List<Map<String, String>> toList = new ArrayList<>();
        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", to);
        toList.add(recipient);
        personalization.put("to", toList);

        personalizations.add(personalization);
        requestBody.put("personalizations", personalizations);

        // Subject
        requestBody.put("subject", subject);

        // Content
        List<Map<String, String>> content = new ArrayList<>();
        Map<String, String> contentItem = new HashMap<>();
        contentItem.put("type", isHtml ? "text/html" : "text/plain");
        contentItem.put("value", body);
        content.add(contentItem);
        requestBody.put("content", content);

        // Attachments
        if (attachments != null && !attachments.isEmpty()) {
            List<Map<String, String>> attachmentList = new ArrayList<>();
            for (Attachment att : attachments) {
                Map<String, String> attachment = new HashMap<>();
                attachment.put("content", Base64.getEncoder().encodeToString(att.getContent()));
                attachment.put("filename", att.getFilename());
                attachment.put("type", att.getContentType());
                attachmentList.add(attachment);
            }
            requestBody.put("attachments", attachmentList);
        }

        // Custom headers from metadata
        if (metadata != null && metadata.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) metadata.get("headers");
            requestBody.put("headers", headers);
        }

        // Tracking settings
        Map<String, Object> trackingSettings = new HashMap<>();
        Map<String, Boolean> clickTracking = new HashMap<>();
        clickTracking.put("enable", true);
        trackingSettings.put("click_tracking", clickTracking);
        requestBody.put("tracking_settings", trackingSettings);

        return requestBody;
    }

    private Map<String, Object> buildBulkRequestBody(List<String> to, List<String> cc, List<String> bcc,
                                                     String subject, String body, boolean isHtml,
                                                     Map<String, Object> metadata) {
        Map<String, Object> requestBody = new HashMap<>();

        // From
        Map<String, String> from = new HashMap<>();
        from.put("email", fromEmail);
        from.put("name", fromName);
        requestBody.put("from", from);

        // Personalizations (multiple recipients)
        List<Map<String, Object>> personalizations = new ArrayList<>();
        Map<String, Object> personalization = new HashMap<>();

        // To recipients
        if (to != null && !to.isEmpty()) {
            List<Map<String, String>> toList = new ArrayList<>();
            for (String email : to) {
                Map<String, String> recipient = new HashMap<>();
                recipient.put("email", email);
                toList.add(recipient);
            }
            personalization.put("to", toList);
        }

        // CC recipients
        if (cc != null && !cc.isEmpty()) {
            List<Map<String, String>> ccList = new ArrayList<>();
            for (String email : cc) {
                Map<String, String> recipient = new HashMap<>();
                recipient.put("email", email);
                ccList.add(recipient);
            }
            personalization.put("cc", ccList);
        }

        // BCC recipients
        if (bcc != null && !bcc.isEmpty()) {
            List<Map<String, String>> bccList = new ArrayList<>();
            for (String email : bcc) {
                Map<String, String> recipient = new HashMap<>();
                recipient.put("email", email);
                bccList.add(recipient);
            }
            personalization.put("bcc", bccList);
        }

        personalizations.add(personalization);
        requestBody.put("personalizations", personalizations);

        // Subject
        requestBody.put("subject", subject);

        // Content
        List<Map<String, String>> content = new ArrayList<>();
        Map<String, String> contentItem = new HashMap<>();
        contentItem.put("type", isHtml ? "text/html" : "text/plain");
        contentItem.put("value", body);
        content.add(contentItem);
        requestBody.put("content", content);

        return requestBody;
    }

    /**
     * SendGrid API response structure
     */
    private static class SendGridResponse {
        // SendGrid returns 202 with no body on success
        // Error responses contain error details
        public List<Error> errors;

        public static class Error {
            public String message;
            public String field;
            public String help;
        }
    }
}