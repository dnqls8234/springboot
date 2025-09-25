package com.mindshift.ums.service.security;

import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.domain.entity.RecipientPref;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.RecipientType;
import com.mindshift.ums.repository.RecipientPrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Service for managing recipient policies and preferences.
 */
@Service
public class RecipientPolicyService {

    private static final Logger logger = LoggerFactory.getLogger(RecipientPolicyService.class);

    private final RecipientPrefRepository recipientPrefRepository;

    @Autowired
    public RecipientPolicyService(RecipientPrefRepository recipientPrefRepository) {
        this.recipientPrefRepository = recipientPrefRepository;
    }

    /**
     * Check if a message can be sent to a recipient based on their preferences.
     *
     * @param tenantId  Tenant ID
     * @param recipient Recipient information
     * @param channel   Channel to send through
     * @return PolicyCheckResult indicating if message is allowed
     */
    public PolicyCheckResult checkRecipientPolicy(String tenantId, SendMessageDto.RecipientDto recipient, ChannelType channel) {
        logger.debug("Checking recipient policy for tenant: {}, channel: {}", tenantId, channel);

        try {
            // Get recipient identifier based on channel
            String recipientId = getRecipientIdentifier(recipient, channel);
            if (recipientId == null) {
                return PolicyCheckResult.failure("MISSING_RECIPIENT_ID", "No recipient identifier found for channel: " + channel);
            }

            // Find recipient preferences - use recipientKey and recipientType
            RecipientType recipientType = getRecipientType(channel);
            Optional<RecipientPref> prefOpt = recipientPrefRepository
                .findByTenantAndRecipient(tenantId, recipientId, recipientType);

            if (prefOpt.isEmpty()) {
                // No preferences found - allow by default
                logger.debug("No recipient preferences found, allowing message");
                return PolicyCheckResult.success();
            }

            RecipientPref pref = prefOpt.get();

            // Check global opt-out
            if (pref.isOptedOut()) {
                logger.info("Recipient {} has globally opted out from channel {}", recipientId, channel);
                return PolicyCheckResult.failure("RECIPIENT_OPTED_OUT", "Recipient has opted out from " + channel + " messages");
            }

            // Check quiet hours
            if (pref.getQuietHoursStart() != null && pref.getQuietHoursEnd() != null) {
                if (isInQuietHours(pref.getQuietHoursStart(), pref.getQuietHoursEnd())) {
                    logger.info("Message blocked due to quiet hours for recipient: {}", recipientId);
                    return PolicyCheckResult.failure("QUIET_HOURS", "Message blocked due to recipient's quiet hours");
                }
            }

            // Check frequency limits
            if (pref.getMaxDailyMessages() != null) {
                // For now, skip frequency check as we don't have a message history table
                // TODO: Implement proper message counting once we have message history
                logger.debug("Skipping frequency check - message history not yet implemented");
            }

            logger.debug("Recipient policy check passed for: {}", recipientId);
            return PolicyCheckResult.success(pref);

        } catch (Exception e) {
            logger.error("Error checking recipient policy", e);
            // Fail open - allow message if policy check fails
            return PolicyCheckResult.success();
        }
    }

    /**
     * Register that a message was sent to a recipient for frequency tracking.
     *
     * @param tenantId  Tenant ID
     * @param recipient Recipient information
     * @param channel   Channel used
     */
    public void recordMessageSent(String tenantId, SendMessageDto.RecipientDto recipient, ChannelType channel) {
        try {
            String recipientId = getRecipientIdentifier(recipient, channel);
            if (recipientId == null) {
                return;
            }

            // Find or create recipient preference
            RecipientType recipientType = getRecipientType(channel);
            RecipientPref pref = recipientPrefRepository
                .findByTenantAndRecipient(tenantId, recipientId, recipientType)
                .orElseGet(() -> {
                    RecipientPref newPref = new RecipientPref();
                    newPref.setTenantId(tenantId);
                    newPref.setRecipientKey(recipientId);
                    newPref.setRecipientId(recipientId);
                    newPref.setRecipientType(recipientType);
                    newPref.setOptedOut(false);
                    return newPref;
                });

            pref.setLastMessageAt(LocalDateTime.now());
            recipientPrefRepository.save(pref);

        } catch (Exception e) {
            logger.error("Error recording message sent", e);
        }
    }

    /**
     * Opt out a recipient from a specific channel.
     *
     * @param tenantId    Tenant ID
     * @param recipientId Recipient identifier
     * @param channel     Channel to opt out from
     */
    public void optOutRecipient(String tenantId, String recipientId, ChannelType channel) {
        RecipientType recipientType = getRecipientType(channel);
        RecipientPref pref = recipientPrefRepository
            .findByTenantAndRecipient(tenantId, recipientId, recipientType)
            .orElseGet(() -> {
                RecipientPref newPref = new RecipientPref();
                newPref.setTenantId(tenantId);
                newPref.setRecipientKey(recipientId);
                newPref.setRecipientId(recipientId);
                newPref.setRecipientType(recipientType);
                return newPref;
            });

        pref.setOptedOut(true);
        pref.setOptedOutAt(LocalDateTime.now());
        recipientPrefRepository.save(pref);

        logger.info("Recipient {} opted out from channel {} for tenant {}", recipientId, channel, tenantId);
    }

    /**
     * Opt in a recipient to a specific channel.
     *
     * @param tenantId    Tenant ID
     * @param recipientId Recipient identifier
     * @param channel     Channel to opt in to
     */
    public void optInRecipient(String tenantId, String recipientId, ChannelType channel) {
        RecipientType recipientType = getRecipientType(channel);
        Optional<RecipientPref> prefOpt = recipientPrefRepository
            .findByTenantAndRecipient(tenantId, recipientId, recipientType);

        if (prefOpt.isPresent()) {
            RecipientPref pref = prefOpt.get();
            pref.setOptedOut(false);
            pref.setOptedOutAt(null);
            recipientPrefRepository.save(pref);

            logger.info("Recipient {} opted in to channel {} for tenant {}", recipientId, channel, tenantId);
        }
    }

    private String getRecipientIdentifier(SendMessageDto.RecipientDto recipient, ChannelType channel) {
        switch (channel) {
            case SMS:
                return recipient.getPhone();
            case EMAIL:
                return recipient.getEmail();
            case FCM_PUSH:
                return recipient.getPushToken();
            case KAKAO_ALIMTALK:
                return recipient.getKakao() != null ? recipient.getKakao().getUserId() : null;
            default:
                return null;
        }
    }

    private RecipientType getRecipientType(ChannelType channel) {
        switch (channel) {
            case SMS:
                return RecipientType.PHONE;
            case EMAIL:
                return RecipientType.EMAIL;
            case FCM_PUSH:
                return RecipientType.PUSH_TOKEN;
            case KAKAO_ALIMTALK:
                return RecipientType.KAKAO;
            default:
                return RecipientType.OTHER;
        }
    }

    private boolean isInQuietHours(LocalTime quietStart, LocalTime quietEnd) {
        LocalTime now = LocalTime.now();

        if (quietStart.isBefore(quietEnd)) {
            // Normal case: 22:00 - 08:00
            return now.isAfter(quietStart) && now.isBefore(quietEnd);
        } else {
            // Crosses midnight: 22:00 - 08:00 next day
            return now.isAfter(quietStart) || now.isBefore(quietEnd);
        }
    }

    /**
     * Result of a recipient policy check.
     */
    public static class PolicyCheckResult {
        private final boolean allowed;
        private final String errorCode;
        private final String errorMessage;
        private final RecipientPref recipientPreferences;

        private PolicyCheckResult(boolean allowed, String errorCode, String errorMessage, RecipientPref recipientPreferences) {
            this.allowed = allowed;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.recipientPreferences = recipientPreferences;
        }

        public static PolicyCheckResult success() {
            return new PolicyCheckResult(true, null, null, null);
        }

        public static PolicyCheckResult success(RecipientPref preferences) {
            return new PolicyCheckResult(true, null, null, preferences);
        }

        public static PolicyCheckResult failure(String errorCode, String errorMessage) {
            return new PolicyCheckResult(false, errorCode, errorMessage, null);
        }

        public boolean isAllowed() { return allowed; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public RecipientPref getRecipientPreferences() { return recipientPreferences; }
    }
}