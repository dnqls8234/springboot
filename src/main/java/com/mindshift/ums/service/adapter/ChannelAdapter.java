package com.mindshift.ums.service.adapter;

import com.mindshift.ums.domain.entity.Message;

import java.util.Map;

/**
 * Base interface for channel adapters.
 * Each channel implementation must provide the actual message sending logic.
 */
public interface ChannelAdapter {

    /**
     * Send a message through this channel.
     *
     * @param message The message to send
     * @return Result containing provider message ID and any additional metadata
     */
    SendResult send(Message message);

    /**
     * Get the channel type this adapter handles.
     *
     * @return The channel type
     */
    String getChannelType();

    /**
     * Check if this adapter can handle the given message.
     *
     * @param message The message to check
     * @return true if this adapter can send the message
     */
    boolean canHandle(Message message);

    /**
     * Result of a channel send operation.
     */
    class SendResult {
        private final boolean success;
        private final String providerMessageId;
        private final String errorCode;
        private final String errorMessage;
        private final Map<String, Object> metadata;

        private SendResult(boolean success, String providerMessageId, String errorCode, String errorMessage, Map<String, Object> metadata) {
            this.success = success;
            this.providerMessageId = providerMessageId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.metadata = metadata;
        }

        public static SendResult success(String providerMessageId, Map<String, Object> metadata) {
            return new SendResult(true, providerMessageId, null, null, metadata);
        }

        public static SendResult success(String providerMessageId) {
            return new SendResult(true, providerMessageId, null, null, null);
        }

        public static SendResult failure(String errorCode, String errorMessage) {
            return new SendResult(false, null, errorCode, errorMessage, null);
        }

        public static SendResult failure(String errorCode, String errorMessage, Map<String, Object> metadata) {
            return new SendResult(false, null, errorCode, errorMessage, metadata);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getProviderMessageId() { return providerMessageId; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
}