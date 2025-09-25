package com.mindshift.ums.service.kafka;

import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.MessageStatus;
import com.mindshift.ums.repository.MessageRepository;
import com.mindshift.ums.service.adapter.ChannelAdapter;
import com.mindshift.ums.service.adapter.ChannelAdapterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka consumer for processing message events asynchronously.
 */
@Service
public class MessageEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageEventConsumer.class);

    private final MessageRepository messageRepository;
    private final ChannelAdapterService channelAdapterService;

    @Autowired
    public MessageEventConsumer(MessageRepository messageRepository,
                               ChannelAdapterService channelAdapterService) {
        this.messageRepository = messageRepository;
        this.channelAdapterService = channelAdapterService;
    }

    /**
     * Process message requested events to send messages through appropriate channels.
     */
    @KafkaListener(topics = "ums.message.requested.v1", groupId = "ums-message-processor")
    @Transactional
    public void handleMessageRequested(@Payload Map<String, Object> event,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                     Acknowledgment acknowledgment) {

        String requestId = (String) event.get("requestId");
        logger.info("Processing message requested event: {}", requestId);

        try {
            // Find the message in database
            Message message = messageRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + requestId));

            // Skip if message is not in PENDING status
            if (message.getStatus() != MessageStatus.PENDING) {
                logger.warn("Message {} is not in PENDING status, skipping. Current status: {}",
                    requestId, message.getStatus());
                acknowledgment.acknowledge();
                return;
            }

            // Update status to PROCESSING
            message.setStatus(MessageStatus.PROCESSING);
            messageRepository.save(message);

            // Send the message through channel adapter
            ChannelAdapter.SendResult result = channelAdapterService.sendMessage(message);

            // Update message based on result
            updateMessageWithResult(message, result);

            logger.info("Message processing completed: {} -> {}", requestId, message.getStatus());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process message requested event: {}", requestId, e);

            try {
                // Try to update message status to FAILED
                Message message = messageRepository.findByRequestId(requestId).orElse(null);
                if (message != null) {
                    message.setStatus(MessageStatus.FAILED);
                    message.setErrorCode("PROCESSING_ERROR");
                    message.setErrorMessage(e.getMessage());
                    message.setFailedAt(LocalDateTime.now());
                    messageRepository.save(message);
                }
            } catch (Exception updateError) {
                logger.error("Failed to update message status to FAILED: {}", requestId, updateError);
            }

            // Acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }

    private void updateMessageWithResult(Message message, ChannelAdapter.SendResult result) {
        if (result.isSuccess()) {
            message.setStatus(MessageStatus.SENT);
            message.setProviderMessageId(result.getProviderMessageId());
            message.setSentAt(LocalDateTime.now());

            // Store metadata if available
            if (result.getMetadata() != null) {
                message.setMeta(result.getMetadata());
            }

            logger.info("Message sent successfully: {} -> {}", message.getRequestId(), result.getProviderMessageId());

        } else {
            message.setStatus(MessageStatus.FAILED);
            message.setErrorCode(result.getErrorCode());
            message.setErrorMessage(result.getErrorMessage());
            message.setFailedAt(LocalDateTime.now());

            // Store error metadata if available
            if (result.getMetadata() != null) {
                message.setErrorDetails(result.getMetadata());
            }

            logger.error("Message failed to send: {} -> {} ({})",
                message.getRequestId(), result.getErrorCode(), result.getErrorMessage());
        }

        messageRepository.save(message);
    }

    /**
     * Handle delivery status updates from external providers.
     */
    @KafkaListener(topics = "ums.message.delivery.v1", groupId = "ums-delivery-processor")
    @Transactional
    public void handleDeliveryStatus(@Payload Map<String, Object> event,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                   Acknowledgment acknowledgment) {

        String providerMessageId = (String) event.get("providerMessageId");
        String deliveryStatus = (String) event.get("status");

        logger.info("Processing delivery status event: {} -> {}", providerMessageId, deliveryStatus);

        try {
            // Find message by provider message ID
            Message message = messageRepository.findByProviderMessageId(providerMessageId)
                .orElseThrow(() -> new RuntimeException("Message not found by provider ID: " + providerMessageId));

            // Update delivery status
            switch (deliveryStatus.toUpperCase()) {
                case "DELIVERED":
                    message.setStatus(MessageStatus.DELIVERED);
                    message.setDeliveredAt(LocalDateTime.now());
                    break;

                case "FAILED":
                    message.setStatus(MessageStatus.FAILED);
                    message.setErrorCode((String) event.get("errorCode"));
                    message.setErrorMessage((String) event.get("errorMessage"));
                    message.setFailedAt(LocalDateTime.now());
                    break;

                case "READ":
                    // Update metadata to track read status
                    if (message.getMeta() == null) {
                        message.setMeta(Map.of("readAt", LocalDateTime.now().toString()));
                    } else {
                        message.getMeta().put("readAt", LocalDateTime.now().toString());
                    }
                    break;

                default:
                    logger.warn("Unknown delivery status: {} for message: {}", deliveryStatus, providerMessageId);
                    acknowledgment.acknowledge();
                    return;
            }

            messageRepository.save(message);

            logger.info("Delivery status updated: {} -> {}", message.getRequestId(), deliveryStatus);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process delivery status event: {}", providerMessageId, e);
            acknowledgment.acknowledge();
        }
    }
}