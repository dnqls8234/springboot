package com.mindshift.ums.service.adapter.provider;

import java.util.Map;

/**
 * Interface for SMS service providers.
 * Implement this interface to add support for a new SMS provider.
 */
public interface SmsProvider {

    /**
     * Get the name of the provider.
     * @return Provider name (e.g., "TWILIO", "SOLAPI", "COOLSMS")
     */
    String getProviderName();

    /**
     * Check if this provider is enabled and configured.
     * @return true if the provider can be used
     */
    boolean isEnabled();

    /**
     * Send an SMS message.
     *
     * @param phoneNumber Recipient phone number (E.164 format)
     * @param message Message content
     * @param metadata Additional metadata for the message
     * @return Result containing success status and provider message ID
     */
    SmsResult sendSms(String phoneNumber, String message, Map<String, Object> metadata);

    /**
     * Send an SMS message with title (for LMS/MMS).
     *
     * @param phoneNumber Recipient phone number
     * @param title Message title
     * @param message Message content
     * @param metadata Additional metadata
     * @return Result containing success status and provider message ID
     */
    SmsResult sendSms(String phoneNumber, String title, String message, Map<String, Object> metadata);

    /**
     * Get the delivery status of a sent message.
     *
     * @param messageId Provider's message ID
     * @return Delivery status
     */
    default DeliveryStatus getDeliveryStatus(String messageId) {
        return DeliveryStatus.UNKNOWN;
    }

    /**
     * Get the priority of this provider (higher number = higher priority).
     * Used when multiple providers are available.
     *
     * @return Priority value (default 0)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Result of SMS sending operation.
     */
    class SmsResult {
        private final boolean success;
        private final String messageId;
        private final String errorCode;
        private final String errorMessage;
        private final Map<String, Object> metadata;

        private SmsResult(boolean success, String messageId, String errorCode,
                         String errorMessage, Map<String, Object> metadata) {
            this.success = success;
            this.messageId = messageId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.metadata = metadata;
        }

        public static SmsResult success(String messageId, Map<String, Object> metadata) {
            return new SmsResult(true, messageId, null, null, metadata);
        }

        public static SmsResult failure(String errorCode, String errorMessage) {
            return new SmsResult(false, null, errorCode, errorMessage, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    /**
     * Delivery status of a message.
     */
    enum DeliveryStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
        UNKNOWN
    }
}