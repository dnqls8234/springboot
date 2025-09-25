package com.mindshift.ums.service.adapter.impl;

import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.service.adapter.ChannelAdapter;
import com.mindshift.ums.service.adapter.provider.EmailProvider;
import com.mindshift.ums.service.adapter.provider.ProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Email channel adapter using Spring Mail.
 */
@Component
public class EmailAdapter implements ChannelAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EmailAdapter.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ProviderManager providerManager;
    private final String preferredProvider;
    private final boolean enableFallback;

    @Autowired
    public EmailAdapter(
            ProviderManager providerManager,
            @Value("${ums.email.preferred-provider:}") String preferredProvider,
            @Value("${ums.email.enable-fallback:true}") boolean enableFallback) {

        this.providerManager = providerManager;
        this.preferredProvider = preferredProvider;
        this.enableFallback = enableFallback;

        logger.info("Email Adapter initialized with preferred provider: {} (fallback: {})",
            preferredProvider.isEmpty() ? "AUTO" : preferredProvider,
            enableFallback ? "enabled" : "disabled");
    }

    @Override
    public SendResult send(Message message) {
        logger.info("Sending email message: {}", message.getRequestId());

        try {
            Map<String, Object> recipient = message.getToJson();
            String emailAddress = (String) recipient.get("email");

            if (emailAddress == null || emailAddress.trim().isEmpty()) {
                return SendResult.failure("INVALID_EMAIL", "Email address not found in recipient data");
            }

            if (!isValidEmail(emailAddress)) {
                return SendResult.failure("INVALID_EMAIL_FORMAT", "Invalid email format: " + emailAddress);
            }

            // Prepare email content
            String subject = message.getRenderedTitle() != null ? message.getRenderedTitle() : "Message from UMS";
            String body = message.getRenderedBody();
            boolean isHtml = isHtmlContent(body);

            // Prepare metadata
            Map<String, Object> providerMetadata = new HashMap<>();
            providerMetadata.put("requestId", message.getRequestId());
            providerMetadata.put("templateCode", message.getTemplateCode());
            if (message.getMeta() != null) {
                providerMetadata.putAll(message.getMeta());
            }

            // Handle attachments if present
            List<EmailProvider.Attachment> attachments = null;
            if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
                attachments = convertAttachments(message.getAttachments());
            }

            EmailProvider.EmailResult result = sendWithProvider(emailAddress, subject, body, isHtml, attachments, providerMetadata);

            if (result.isSuccess()) {
                logger.info("Email sent successfully: {} -> {}", message.getRequestId(), result.getMessageId());

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("channel", "EMAIL");
                metadata.put("emailAddress", emailAddress);
                if (result.getMetadata() != null) {
                    metadata.putAll(result.getMetadata());
                }

                return SendResult.success(result.getMessageId(), metadata);
            } else {
                return SendResult.failure(result.getErrorCode(), result.getErrorMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to send email message: {}", message.getRequestId(), e);
            return SendResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    private EmailProvider.EmailResult sendWithProvider(String toEmail, String subject, String body,
                                                       boolean isHtml, List<EmailProvider.Attachment> attachments,
                                                       Map<String, Object> metadata) {
        // Try preferred provider first if specified
        if (!preferredProvider.isEmpty()) {
            Optional<EmailProvider> provider = providerManager.getEmailProvider(preferredProvider);
            if (provider.isPresent()) {
                logger.info("Using preferred Email provider: {}", preferredProvider);
                EmailProvider.EmailResult result = provider.get().sendEmail(toEmail, subject, body, isHtml, attachments, metadata);

                if (!result.isSuccess() && enableFallback) {
                    logger.warn("Preferred provider failed, trying fallback providers");
                    return providerManager.sendEmailWithFallback(toEmail, subject, body, isHtml, metadata);
                }
                return result;
            } else {
                logger.warn("Preferred provider {} not available, using best available", preferredProvider);
            }
        }

        // Use best available provider or fallback
        if (enableFallback) {
            return providerManager.sendEmailWithFallback(toEmail, subject, body, isHtml, metadata);
        } else {
            Optional<EmailProvider> provider = providerManager.getBestEmailProvider();
            if (provider.isPresent()) {
                return provider.get().sendEmail(toEmail, subject, body, isHtml, attachments, metadata);
            } else {
                return EmailProvider.EmailResult.failure("NO_PROVIDER", "No Email provider is available");
            }
        }
    }

    private List<EmailProvider.Attachment> convertAttachments(List<Map<String, Object>> attachments) {
        List<EmailProvider.Attachment> converted = new ArrayList<>();

        for (Map<String, Object> attachment : attachments) {
            String filename = (String) attachment.get("filename");
            String contentType = (String) attachment.get("type");
            Object content = attachment.get("content");

            if (filename != null && content != null) {
                byte[] bytes = null;

                if (content instanceof String) {
                    // Base64 encoded content
                    bytes = java.util.Base64.getDecoder().decode((String) content);
                } else if (content instanceof byte[]) {
                    bytes = (byte[]) content;
                }

                if (bytes != null) {
                    converted.add(new EmailProvider.Attachment(filename, bytes, contentType));
                }
            }
        }

        return converted;
    }

    private boolean isHtmlContent(String content) {
        // Simple check for HTML content
        return content != null && (
            content.contains("<html>") ||
            content.contains("<body>") ||
            content.contains("<p>") ||
            content.contains("<br>") ||
            content.contains("<div>")
        );
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public String getChannelType() {
        return ChannelType.EMAIL.name();
    }

    @Override
    public boolean canHandle(Message message) {
        if (message.getChannel() != ChannelType.EMAIL) {
            return false;
        }

        Map<String, Object> recipient = message.getToJson();
        return recipient != null && recipient.containsKey("email");
    }
}