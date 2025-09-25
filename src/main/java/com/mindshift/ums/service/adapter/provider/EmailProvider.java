package com.mindshift.ums.service.adapter.provider;

import java.util.List;
import java.util.Map;

/**
 * Interface for Email service providers.
 * Implement this interface to add support for a new Email provider.
 */
public interface EmailProvider {

    /**
     * Get the name of the provider.
     * @return Provider name (e.g., "SMTP", "SENDGRID", "AWS_SES", "MAILGUN")
     */
    String getProviderName();

    /**
     * Check if this provider is enabled and configured.
     * @return true if the provider can be used
     */
    boolean isEnabled();

    /**
     * Send an email message.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (HTML or plain text)
     * @param isHtml Whether the body is HTML
     * @param metadata Additional metadata
     * @return Result containing success status and provider message ID
     */
    EmailResult sendEmail(String to, String subject, String body, boolean isHtml, Map<String, Object> metadata);

    /**
     * Send an email with attachments.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param isHtml Whether the body is HTML
     * @param attachments List of attachments
     * @param metadata Additional metadata
     * @return Result containing success status and provider message ID
     */
    EmailResult sendEmail(String to, String subject, String body, boolean isHtml,
                         List<Attachment> attachments, Map<String, Object> metadata);

    /**
     * Send email to multiple recipients.
     *
     * @param to List of recipient email addresses
     * @param cc List of CC recipients
     * @param bcc List of BCC recipients
     * @param subject Email subject
     * @param body Email body
     * @param isHtml Whether the body is HTML
     * @param metadata Additional metadata
     * @return Result containing success status and provider message ID
     */
    default EmailResult sendBulkEmail(List<String> to, List<String> cc, List<String> bcc,
                                      String subject, String body, boolean isHtml,
                                      Map<String, Object> metadata) {
        // Default implementation sends to first recipient only
        if (to != null && !to.isEmpty()) {
            return sendEmail(to.get(0), subject, body, isHtml, metadata);
        }
        return EmailResult.failure("NO_RECIPIENTS", "No recipients provided");
    }

    /**
     * Get the priority of this provider (higher number = higher priority).
     * @return Priority value (default 0)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Email attachment.
     */
    class Attachment {
        private final String filename;
        private final byte[] content;
        private final String contentType;

        public Attachment(String filename, byte[] content, String contentType) {
            this.filename = filename;
            this.content = content;
            this.contentType = contentType;
        }

        public String getFilename() { return filename; }
        public byte[] getContent() { return content; }
        public String getContentType() { return contentType; }
    }

    /**
     * Result of email sending operation.
     */
    class EmailResult {
        private final boolean success;
        private final String messageId;
        private final String errorCode;
        private final String errorMessage;
        private final Map<String, Object> metadata;

        private EmailResult(boolean success, String messageId, String errorCode,
                           String errorMessage, Map<String, Object> metadata) {
            this.success = success;
            this.messageId = messageId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.metadata = metadata;
        }

        public static EmailResult success(String messageId, Map<String, Object> metadata) {
            return new EmailResult(true, messageId, null, null, metadata);
        }

        public static EmailResult failure(String errorCode, String errorMessage) {
            return new EmailResult(false, null, errorCode, errorMessage, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
}