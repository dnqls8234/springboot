package com.mindshift.ums.service.adapter.provider.impl;

import com.mindshift.ums.service.adapter.provider.EmailProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * SMTP email provider implementation using Spring Mail.
 * This is the default email provider.
 */
@Component
@ConditionalOnProperty(name = "ums.email.provider", havingValue = "SMTP", matchIfMissing = true)
public class SmtpEmailProvider implements EmailProvider {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailProvider.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String fromName;
    private final boolean enabled;

    public SmtpEmailProvider(
            JavaMailSender mailSender,
            @Value("${ums.email.smtp.from-email:${ums.email.from-email:noreply@example.com}}") String fromEmail,
            @Value("${ums.email.smtp.from-name:${ums.email.from-name:UMS Service}}") String fromName,
            @Value("${ums.email.smtp.enabled:true}") boolean enabled) {

        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.enabled = enabled;

        if (this.enabled) {
            logger.info("SMTP email provider initialized with from: {} <{}>", fromName, fromEmail);
        } else {
            logger.warn("SMTP email provider is disabled");
        }
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return 1; // Lowest priority (default provider)
    }

    @Override
    public EmailResult sendEmail(String to, String subject, String body, boolean isHtml, Map<String, Object> metadata) {
        return sendEmail(to, subject, body, isHtml, null, metadata);
    }

    @Override
    public EmailResult sendEmail(String to, String subject, String body, boolean isHtml,
                                List<Attachment> attachments, Map<String, Object> metadata) {
        if (!enabled) {
            return EmailResult.failure("PROVIDER_DISABLED", "SMTP provider is not enabled");
        }

        logger.info("Sending email via SMTP to: {}", to);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Generate unique message ID
            String messageId = "smtp-" + UUID.randomUUID().toString();

            // Set basic properties
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            // Add custom headers
            mimeMessage.setHeader("Message-ID", messageId + "@" + fromEmail.split("@")[1]);

            if (metadata != null) {
                // Add custom headers from metadata
                if (metadata.containsKey("headers")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> headers = (Map<String, String>) metadata.get("headers");
                    headers.forEach((key, value) -> {
                        try {
                            mimeMessage.setHeader(key, value);
                        } catch (MessagingException e) {
                            logger.warn("Failed to set header {}: {}", key, value);
                        }
                    });
                }

                // Add reply-to if specified
                if (metadata.containsKey("replyTo")) {
                    helper.setReplyTo((String) metadata.get("replyTo"));
                }
            }

            // Add attachments
            if (attachments != null && !attachments.isEmpty()) {
                for (Attachment attachment : attachments) {
                    helper.addAttachment(
                        attachment.getFilename(),
                        () -> new java.io.ByteArrayInputStream(attachment.getContent()),
                        attachment.getContentType()
                    );
                }
            }

            // Send email
            mailSender.send(mimeMessage);

            logger.info("Email sent successfully via SMTP. Message ID: {}", messageId);

            Map<String, Object> resultMetadata = new HashMap<>();
            resultMetadata.put("provider", "SMTP");
            resultMetadata.put("messageId", messageId);
            resultMetadata.put("from", fromEmail);
            resultMetadata.put("to", to);

            return EmailResult.success(messageId, resultMetadata);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send email via SMTP", e);
            return EmailResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    @Override
    public EmailResult sendBulkEmail(List<String> to, List<String> cc, List<String> bcc,
                                    String subject, String body, boolean isHtml,
                                    Map<String, Object> metadata) {
        if (!enabled) {
            return EmailResult.failure("PROVIDER_DISABLED", "SMTP provider is not enabled");
        }

        logger.info("Sending bulk email via SMTP to {} recipients", to.size());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Generate unique message ID
            String messageId = "smtp-bulk-" + UUID.randomUUID().toString();

            // Set basic properties
            helper.setFrom(fromEmail, fromName);

            // Set recipients
            if (to != null && !to.isEmpty()) {
                helper.setTo(to.toArray(new String[0]));
            }
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }
            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(new String[0]));
            }

            helper.setSubject(subject);
            helper.setText(body, isHtml);

            // Add custom headers
            mimeMessage.setHeader("Message-ID", messageId + "@" + fromEmail.split("@")[1]);

            // Send email
            mailSender.send(mimeMessage);

            logger.info("Bulk email sent successfully via SMTP. Message ID: {}", messageId);

            Map<String, Object> resultMetadata = new HashMap<>();
            resultMetadata.put("provider", "SMTP");
            resultMetadata.put("messageId", messageId);
            resultMetadata.put("recipientCount",
                (to != null ? to.size() : 0) +
                (cc != null ? cc.size() : 0) +
                (bcc != null ? bcc.size() : 0));

            return EmailResult.success(messageId, resultMetadata);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send bulk email via SMTP", e);
            return EmailResult.failure("SEND_FAILED", e.getMessage());
        }
    }
}