package com.mindshift.ums.service;

import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.api.exception.MessageNotFoundException;
import com.mindshift.ums.api.exception.RateLimitExceededException;
import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.entity.MessageEvent;
import com.mindshift.ums.domain.entity.Template;
import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.enums.MessageStatus;
import com.mindshift.ums.repository.MessageEventRepository;
import com.mindshift.ums.repository.MessageRepository;
import com.mindshift.ums.repository.TenantConfigRepository;
import com.mindshift.ums.service.kafka.KafkaProducerService;
import com.mindshift.ums.service.security.RateLimitService;
import com.mindshift.ums.service.security.RecipientPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main service for message operations using layered architecture.
 * Handles authentication, validation, rate limiting, template processing,
 * message creation, persistence, and event publishing.
 */
@Service
@Transactional
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final MessageEventRepository messageEventRepository;
    private final TenantConfigRepository tenantConfigRepository;
    private final AuthenticationService authenticationService;
    private final ValidationService validationService;
    private final TemplateService templateService;
    private final RateLimitService rateLimitService;
    private final RecipientPolicyService recipientPolicyService;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public MessageService(MessageRepository messageRepository,
                         MessageEventRepository messageEventRepository,
                         TenantConfigRepository tenantConfigRepository,
                         AuthenticationService authenticationService,
                         ValidationService validationService,
                         TemplateService templateService,
                         RateLimitService rateLimitService,
                         RecipientPolicyService recipientPolicyService,
                         KafkaProducerService kafkaProducerService) {
        this.messageRepository = messageRepository;
        this.messageEventRepository = messageEventRepository;
        this.tenantConfigRepository = tenantConfigRepository;
        this.authenticationService = authenticationService;
        this.validationService = validationService;
        this.templateService = templateService;
        this.rateLimitService = rateLimitService;
        this.recipientPolicyService = recipientPolicyService;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Accept a message for processing using layered architecture.
     *
     * @param request        The message request
     * @param idempotencyKey Idempotency key for duplicate prevention
     * @param authorization  Authorization header
     * @return The generated request ID
     */
    @Transactional
    public String acceptMessage(SendMessageDto.SendMessageRequest request,
                               String idempotencyKey,
                               String authorization) {

        logger.info("Processing message request with idempotency key: {}", idempotencyKey);

        try {
            // Step 1: Authentication
            TenantConfig tenant = authenticationService.authenticateTenant(authorization);
            logger.debug("Tenant authenticated: {}", tenant.getTenantId());

            // Step 2: Idempotency check
            var existingMessage = validationService.checkIdempotency(idempotencyKey, tenant.getTenantId());
            if (existingMessage.isPresent()) {
                logger.info("Returning existing message for idempotency key: {}", idempotencyKey);
                return existingMessage.get().getRequestId();
            }

            // Step 3: Basic validation
            validationService.validateMessageRequest(request);
            validationService.validateRecipient(request.getTo(), request.getChannel());

            // Step 4: Rate limiting
            checkRateLimits(tenant, request);

            // Step 5: Template processing
            Template template = templateService.loadTemplate(tenant, request.getTemplateCode(),
                request.getChannel(), request.getLocale());
            templateService.validateTemplate(template, request.getTemplateData());
            Map<String, String> renderedContent = templateService.renderTemplate(template, request.getTemplateData());

            // Step 6: Recipient policy check
            recipientPolicyService.checkRecipientPolicy(tenant.getTenantId(),
                request.getTo(), request.getChannel());

            // Step 7: Create message
            String requestId = generateRequestId();
            Message message = createMessage(request, requestId, tenant, template, renderedContent, idempotencyKey);

            // Step 8: Persist with Outbox pattern
            Message savedMessage = messageRepository.save(message);
            MessageEvent requestEvent = MessageEvent.createRequestedEvent(savedMessage);
            messageEventRepository.save(requestEvent);

            // Step 9: Publish event
            kafkaProducerService.publishMessageRequested(
                savedMessage.getRequestId(),
                savedMessage.getTenantId(),
                savedMessage.getChannel().name(),
                savedMessage.getTemplateCode(),
                savedMessage.getPriority().name()
            );

            // Step 10: Record message sent for policy tracking
            recipientPolicyService.recordMessageSent(tenant.getTenantId(),
                request.getTo(), request.getChannel());

            // Release idempotency lock
            validationService.releaseIdempotencyLock(idempotencyKey);

            logger.info("Message processing completed successfully. RequestId: {}", requestId);
            return requestId;

        } catch (Exception e) {
            logger.error("Failed to process message with idempotency key: {}", idempotencyKey, e);
            validationService.releaseIdempotencyLock(idempotencyKey);
            throw e;
        }
    }

    /**
     * Get the status of a message by request ID.
     *
     * @param requestId The message request ID
     * @return Message status response
     */
    @Transactional(readOnly = true)
    public SendMessageDto.MessageStatusResponse getMessageStatus(String requestId) {
        logger.debug("Getting message status for requestId: {}", requestId);

        Message message = messageRepository.findByRequestId(requestId)
            .orElseThrow(() -> new MessageNotFoundException(requestId));

        return buildMessageStatusResponse(message);
    }

    /**
     * List messages for a tenant with pagination.
     *
     * @param authorization Authorization header to identify tenant
     * @param page         Page number (0-based)
     * @param size         Page size
     * @param status       Optional status filter
     * @return Paginated list of messages
     */
    @Transactional(readOnly = true)
    public Map<String, Object> listMessages(String authorization, int page, int size, String status) {
        logger.debug("Listing messages for page: {}, size: {}, status: {}", page, size, status);

        TenantConfig tenant = authenticateTenant(authorization);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage;

        if (status != null) {
            MessageStatus messageStatus = MessageStatus.valueOf(status.toUpperCase());
            messagePage = messageRepository.findAllByTenantIdAndStatus(tenant.getTenantId(), messageStatus, pageable);
        } else {
            messagePage = messageRepository.findAllByTenantId(tenant.getTenantId(), pageable);
        }

        List<SendMessageDto.MessageStatusResponse> messages = messagePage.getContent().stream()
            .map(this::buildMessageStatusResponse)
            .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("totalElements", messagePage.getTotalElements());
        pagination.put("totalPages", messagePage.getTotalPages());
        pagination.put("hasNext", messagePage.hasNext());
        pagination.put("hasPrevious", messagePage.hasPrevious());
        result.put("pagination", pagination);

        logger.debug("Found {} messages for tenant: {}", messagePage.getTotalElements(), tenant.getTenantId());
        return result;
    }

    /**
     * Build a message status response from a Message entity.
     */
    private SendMessageDto.MessageStatusResponse buildMessageStatusResponse(Message message) {
        SendMessageDto.MessageTimestamps timestamps = new SendMessageDto.MessageTimestamps(
            message.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            message.getSentAt() != null ? message.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            message.getDeliveredAt() != null ? message.getDeliveredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            message.getFailedAt() != null ? message.getFailedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );

        SendMessageDto.ErrorDetail error = null;
        if (message.getErrorCode() != null) {
            error = new SendMessageDto.ErrorDetail(
                message.getErrorCode(),
                message.getErrorMessage() != null ? message.getErrorMessage() : "Unknown error",
                message.getErrorDetails()
            );
        }

        SendMessageDto.MessageStatusResponse response = new SendMessageDto.MessageStatusResponse();
        response.setRequestId(message.getRequestId());
        response.setStatus(message.getStatus().name());
        response.setChannel(message.getChannel());
        response.setTimestamps(timestamps);
        response.setRetries(message.getRetries());
        response.setError(error);
        response.setProviderMessageId(message.getProviderMessageId());
        response.setMeta(message.getMeta());

        return response;
    }

    /**
     * Check rate limits for tenant and recipient.
     */
    private void checkRateLimits(TenantConfig tenant, SendMessageDto.SendMessageRequest request) {
        // Check tenant rate limit
        Integer tenantLimit = tenant.getRateLimit("per_hour");
        int limit = tenantLimit != null ? tenantLimit : 1000;

        var result = rateLimitService.checkRateLimit(tenant.getTenantId(), "messages", limit, 1);
        if (!result.isAllowed()) {
            throw new RateLimitExceededException(
                "Rate limit exceeded for tenant: " + tenant.getTenantId(),
                result.getRemainingTokens(),
                result.getResetTimeSeconds()
            );
        }

        // Check recipient rate limit
        String recipientKey = getRecipientKey(request.getTo());
        var recipientResult = rateLimitService.checkRateLimit(recipientKey, "recipient_messages", 10, 1);
        if (!recipientResult.isAllowed()) {
            throw new RateLimitExceededException(
                "Rate limit exceeded for recipient: " + recipientKey,
                recipientResult.getRemainingTokens(),
                recipientResult.getResetTimeSeconds()
            );
        }
    }

    /**
     * Create message entity from request data.
     */
    private Message createMessage(SendMessageDto.SendMessageRequest request, String requestId,
                                TenantConfig tenant, Template template, Map<String, String> renderedContent,
                                String idempotencyKey) {

        // Calculate TTL expiration
        LocalDateTime ttlExpiresAt = null;
        if (request.getRouting() != null && request.getRouting().getTtlSeconds() != null) {
            ttlExpiresAt = LocalDateTime.now().plusSeconds(request.getRouting().getTtlSeconds());
        }

        // Build recipient JSON
        Map<String, Object> toJson = buildRecipientJson(request.getTo());

        // Create the message entity
        Message message = new Message(requestId, tenant.getTenantId(), request.getChannel(), request.getTemplateCode());
        message.setTemplate(template);
        message.setLocale(request.getLocale());
        message.setToJson(toJson);
        message.setRenderedTitle(renderedContent.get("title"));
        message.setRenderedBody(renderedContent.getOrDefault("body", ""));
        message.setTtlExpiresAt(ttlExpiresAt);
        message.setMeta(request.getMeta());
        message.setIdempotencyKey(idempotencyKey);

        // Set routing information
        if (request.getRouting() != null) {
            message.setPriority(request.getRouting().getPriority());
            message.setRouting(buildRoutingMap(request.getRouting()));
        }

        // Set attachments
        if (request.getAttachments() != null) {
            List<Map<String, Object>> attachments = request.getAttachments().stream()
                .map(this::buildAttachmentMap)
                .toList();
            message.setAttachments(attachments);
        }

        return message;
    }

    private Map<String, Object> buildRecipientJson(SendMessageDto.RecipientDto recipient) {
        Map<String, Object> toJson = new HashMap<>();

        if (recipient.getPhone() != null) {
            toJson.put("phone", recipient.getPhone());
        }
        if (recipient.getEmail() != null) {
            toJson.put("email", recipient.getEmail());
        }
        if (recipient.getPushToken() != null) {
            toJson.put("pushToken", recipient.getPushToken());
        }
        if (recipient.getKakao() != null) {
            toJson.put("kakao", Map.of("userId", recipient.getKakao().getUserId()));
        }

        return toJson;
    }

    private Map<String, Object> buildRoutingMap(SendMessageDto.RoutingDto routing) {
        Map<String, Object> map = new HashMap<>();

        if (routing.getFallback() != null) {
            List<String> fallbackChannels = routing.getFallback().stream()
                .map(Enum::name)
                .toList();
            map.put("fallback", fallbackChannels);
        }

        if (routing.getTtlSeconds() != null) {
            map.put("ttlSeconds", routing.getTtlSeconds());
        }

        map.put("priority", routing.getPriority().name());
        return map;
    }

    private Map<String, Object> buildAttachmentMap(SendMessageDto.AttachmentDto attachment) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", attachment.getType());
        map.put("url", attachment.getUrl());

        if (attachment.getFilename() != null) {
            map.put("filename", attachment.getFilename());
        }

        return map;
    }

    private String getRecipientKey(SendMessageDto.RecipientDto recipient) {
        if (recipient.getPhone() != null) {
            return recipient.getPhone();
        } else if (recipient.getEmail() != null) {
            return recipient.getEmail();
        } else if (recipient.getKakao() != null) {
            return recipient.getKakao().getUserId();
        } else if (recipient.getPushToken() != null) {
            return recipient.getPushToken();
        }
        throw new IllegalArgumentException("No valid recipient identifier found");
    }

    private String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Authenticate tenant from authorization header.
     */
    private TenantConfig authenticateTenant(String authorization) {
        return authenticationService.authenticateTenant(authorization);
    }
}