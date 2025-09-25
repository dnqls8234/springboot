package com.mindshift.ums.service;

import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.api.exception.IdempotencyException;
import com.mindshift.ums.api.exception.ValidationException;
import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for validating message requests and handling idempotency.
 */
@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final MessageRepository messageRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{7,14}$");

    @Autowired
    public ValidationService(MessageRepository messageRepository,
                           RedisTemplate<String, String> redisTemplate) {
        this.messageRepository = messageRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check idempotency for message request.
     *
     * @param idempotencyKey Idempotency key
     * @param tenantId Tenant ID
     * @return Optional message if already processed
     * @throws IdempotencyException if key is being processed
     */
    public Optional<Message> checkIdempotency(String idempotencyKey, String tenantId) {
        logger.debug("Checking idempotency for key: {} and tenant: {}", idempotencyKey, tenantId);

        // Check if message already exists
        Optional<Message> existingMessage = messageRepository.findByIdempotencyKey(idempotencyKey);
        if (existingMessage.isPresent()) {
            Message message = existingMessage.get();
            if (!message.getTenantId().equals(tenantId)) {
                throw new IdempotencyException("Idempotency key belongs to different tenant");
            }
            logger.info("Found existing message for idempotency key: {}", idempotencyKey);
            return existingMessage;
        }

        // Check if key is currently being processed (distributed lock)
        String lockKey = "idempotency_lock:" + idempotencyKey;
        String lockValue = redisTemplate.opsForValue().get(lockKey);

        if (lockValue != null) {
            logger.warn("Idempotency key is currently being processed: {}", idempotencyKey);
            throw new IdempotencyException("Request is currently being processed");
        }

        // Set processing lock (5 minutes TTL)
        redisTemplate.opsForValue().set(lockKey, tenantId, Duration.ofMinutes(5));
        logger.debug("Idempotency check passed, processing lock set for: {}", idempotencyKey);

        return Optional.empty();
    }

    /**
     * Release idempotency lock after processing.
     *
     * @param idempotencyKey Idempotency key
     */
    public void releaseIdempotencyLock(String idempotencyKey) {
        String lockKey = "idempotency_lock:" + idempotencyKey;
        redisTemplate.delete(lockKey);
        logger.debug("Released idempotency lock for: {}", idempotencyKey);
    }

    /**
     * Validate recipient information based on channel.
     *
     * @param recipient Recipient data
     * @param channel Message channel
     * @throws ValidationException if validation fails
     */
    public void validateRecipient(SendMessageDto.RecipientDto recipient, ChannelType channel) {
        logger.debug("Validating recipient for channel: {}", channel);

        Map<String, String> errors = new HashMap<>();

        switch (channel) {
            case SMS:
                validateSmsRecipient(recipient, errors);
                break;
            case EMAIL:
                validateEmailRecipient(recipient, errors);
                break;
            case FCM_PUSH:
                validateFcmRecipient(recipient, errors);
                break;
            case KAKAO_ALIMTALK:
                validateKakaoRecipient(recipient, errors);
                break;
            default:
                errors.put("channel", "Unsupported channel: " + channel);
        }

        if (!errors.isEmpty()) {
            logger.warn("Recipient validation failed for channel {}: {}", channel, errors);
            throw new ValidationException("Recipient validation failed", Map.of("validationErrors", errors));
        }

        logger.debug("Recipient validation passed for channel: {}", channel);
    }

    /**
     * Validate message request basic fields.
     *
     * @param request Message request
     * @throws ValidationException if validation fails
     */
    public void validateMessageRequest(SendMessageDto.SendMessageRequest request) {
        logger.debug("Validating message request");

        Map<String, String> errors = new HashMap<>();

        // Validate required fields
        if (request.getChannel() == null) {
            errors.put("channel", "Channel is required");
        }

        if (request.getTemplateCode() == null || request.getTemplateCode().trim().isEmpty()) {
            errors.put("templateCode", "Template code is required");
        }

        if (request.getTo() == null) {
            errors.put("to", "Recipient information is required");
        }

        // Validate locale format
        if (request.getLocale() != null && !isValidLocale(request.getLocale())) {
            errors.put("locale", "Invalid locale format");
        }

        // Validate routing if present
        if (request.getRouting() != null) {
            validateRouting(request.getRouting(), errors);
        }

        // Validate attachments if present
        if (request.getAttachments() != null) {
            validateAttachments(request.getAttachments(), errors);
        }

        if (!errors.isEmpty()) {
            logger.warn("Message request validation failed: {}", errors);
            throw new ValidationException("Message request validation failed", Map.of("validationErrors", errors));
        }

        logger.debug("Message request validation passed");
    }

    private void validateSmsRecipient(SendMessageDto.RecipientDto recipient, Map<String, String> errors) {
        if (recipient.getPhone() == null || recipient.getPhone().trim().isEmpty()) {
            errors.put("phone", "Phone number is required for SMS");
            return;
        }

        String phone = recipient.getPhone().replaceAll("[^+\\d]", "");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.put("phone", "Invalid phone number format");
        }
    }

    private void validateEmailRecipient(SendMessageDto.RecipientDto recipient, Map<String, String> errors) {
        if (recipient.getEmail() == null || recipient.getEmail().trim().isEmpty()) {
            errors.put("email", "Email address is required for EMAIL");
            return;
        }

        if (!EMAIL_PATTERN.matcher(recipient.getEmail().trim()).matches()) {
            errors.put("email", "Invalid email address format");
        }
    }

    private void validateFcmRecipient(SendMessageDto.RecipientDto recipient, Map<String, String> errors) {
        if (recipient.getPushToken() == null || recipient.getPushToken().trim().isEmpty()) {
            errors.put("pushToken", "Push token is required for FCM");
        }
    }

    private void validateKakaoRecipient(SendMessageDto.RecipientDto recipient, Map<String, String> errors) {
        if (recipient.getKakao() == null) {
            errors.put("kakao", "Kakao information is required for KAKAO_ALIMTALK");
            return;
        }

        if (recipient.getKakao().getUserId() == null || recipient.getKakao().getUserId().trim().isEmpty()) {
            errors.put("kakao.userId", "Kakao user ID is required");
        }
    }

    private void validateRouting(SendMessageDto.RoutingDto routing, Map<String, String> errors) {
        if (routing.getTtlSeconds() != null && routing.getTtlSeconds() <= 0) {
            errors.put("routing.ttlSeconds", "TTL seconds must be positive");
        }

        if (routing.getPriority() == null) {
            errors.put("routing.priority", "Priority is required in routing");
        }
    }

    private void validateAttachments(java.util.List<SendMessageDto.AttachmentDto> attachments, Map<String, String> errors) {
        for (int i = 0; i < attachments.size(); i++) {
            SendMessageDto.AttachmentDto attachment = attachments.get(i);
            String prefix = "attachments[" + i + "]";

            if (attachment.getType() == null || attachment.getType().trim().isEmpty()) {
                errors.put(prefix + ".type", "Attachment type is required");
            }

            if (attachment.getUrl() == null || attachment.getUrl().trim().isEmpty()) {
                errors.put(prefix + ".url", "Attachment URL is required");
            }
        }
    }

    private boolean isValidLocale(String locale) {
        // Simple locale validation (e.g., "en", "ko", "en_US", "ko_KR")
        return locale.matches("^[a-z]{2}(_[A-Z]{2})?$");
    }
}