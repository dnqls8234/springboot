package com.mindshift.ums.service.adapter.impl;

import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.service.adapter.ChannelAdapter;
import com.mindshift.ums.service.adapter.provider.ProviderManager;
import com.mindshift.ums.service.adapter.provider.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SMS channel adapter using a generic SMS provider.
 */
@Component
public class SmsAdapter implements ChannelAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SmsAdapter.class);

    private final ProviderManager providerManager;
    private final String preferredProvider;
    private final boolean enableFallback;

    @Autowired
    public SmsAdapter(
            ProviderManager providerManager,
            @Value("${ums.sms.preferred-provider:}") String preferredProvider,
            @Value("${ums.sms.enable-fallback:true}") boolean enableFallback) {

        this.providerManager = providerManager;
        this.preferredProvider = preferredProvider;
        this.enableFallback = enableFallback;

        logger.info("SMS Adapter initialized with preferred provider: {} (fallback: {})",
            preferredProvider.isEmpty() ? "AUTO" : preferredProvider,
            enableFallback ? "enabled" : "disabled");
    }

    @Override
    public SendResult send(Message message) {
        logger.info("Sending SMS message: {}", message.getRequestId());

        try {
            Map<String, Object> recipient = message.getToJson();
            String phoneNumber = (String) recipient.get("phone");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return SendResult.failure("INVALID_PHONE", "Phone number not found in recipient data");
            }

            // Normalize phone number (remove spaces, dashes, etc.)
            phoneNumber = normalizePhoneNumber(phoneNumber);

            if (!isValidPhoneNumber(phoneNumber)) {
                return SendResult.failure("INVALID_PHONE_FORMAT", "Invalid phone number format: " + phoneNumber);
            }

            // Prepare metadata for the provider
            Map<String, Object> providerMetadata = new HashMap<>();
            providerMetadata.put("requestId", message.getRequestId());
            providerMetadata.put("templateCode", message.getTemplateCode());
            if (message.getMeta() != null) {
                providerMetadata.putAll(message.getMeta());
            }

            // Get SMS content
            String smsContent = message.getRenderedBody();
            String smsTitle = message.getRenderedTitle();

            SmsProvider.SmsResult result;

            // Try preferred provider first if specified
            if (!preferredProvider.isEmpty()) {
                Optional<SmsProvider> provider = providerManager.getSmsProvider(preferredProvider);
                if (provider.isPresent()) {
                    logger.info("Using preferred SMS provider: {}", preferredProvider);
                    result = provider.get().sendSms(phoneNumber, smsTitle, smsContent, providerMetadata);

                    if (!result.isSuccess() && enableFallback) {
                        logger.warn("Preferred provider failed, trying fallback providers");
                        result = providerManager.sendSmsWithFallback(phoneNumber, smsContent, providerMetadata);
                    }
                } else {
                    logger.warn("Preferred provider {} not available, using best available", preferredProvider);
                    result = sendWithBestProvider(phoneNumber, smsTitle, smsContent, providerMetadata);
                }
            } else {
                // Use best available provider
                result = sendWithBestProvider(phoneNumber, smsTitle, smsContent, providerMetadata);
            }

            if (result.isSuccess()) {
                logger.info("SMS sent successfully: {} -> {}", message.getRequestId(), result.getMessageId());

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("channel", "SMS");
                metadata.put("phoneNumber", phoneNumber);
                if (result.getMetadata() != null) {
                    metadata.putAll(result.getMetadata());
                }

                return SendResult.success(result.getMessageId(), metadata);
            } else {
                return SendResult.failure(result.getErrorCode(), result.getErrorMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to send SMS message: {}", message.getRequestId(), e);
            return SendResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    private SmsProvider.SmsResult sendWithBestProvider(String phoneNumber, String title, String content,
                                                       Map<String, Object> metadata) {
        if (enableFallback) {
            // Use fallback mechanism which tries all providers
            return providerManager.sendSmsWithFallback(phoneNumber, content, metadata);
        } else {
            // Use only the best provider without fallback
            Optional<SmsProvider> provider = providerManager.getBestSmsProvider();
            if (provider.isPresent()) {
                return provider.get().sendSms(phoneNumber, title, content, metadata);
            } else {
                return SmsProvider.SmsResult.failure("NO_PROVIDER", "No SMS provider is available");
            }
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non-digit characters except +
        String normalized = phoneNumber.replaceAll("[^+\\d]", "");

        // Add country code if missing
        if (!normalized.startsWith("+")) {
            if (normalized.startsWith("010") || normalized.startsWith("011") || normalized.startsWith("016") || normalized.startsWith("017") || normalized.startsWith("018") || normalized.startsWith("019")) {
                normalized = "+82" + normalized.substring(1); // Korea
            } else if (normalized.length() == 10 || normalized.length() == 11) {
                normalized = "+82" + normalized; // Assume Korea for 10-11 digit numbers
            }
        }

        return normalized;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic validation for international phone numbers
        return phoneNumber.matches("^\\+[1-9]\\d{7,14}$");
    }

    @Override
    public String getChannelType() {
        return ChannelType.SMS.name();
    }

    @Override
    public boolean canHandle(Message message) {
        if (message.getChannel() != ChannelType.SMS) {
            return false;
        }

        Map<String, Object> recipient = message.getToJson();
        return recipient != null && recipient.containsKey("phone");
    }

}