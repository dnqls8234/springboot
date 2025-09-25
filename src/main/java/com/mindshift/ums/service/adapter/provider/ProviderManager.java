package com.mindshift.ums.service.adapter.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Manager for selecting and managing message providers.
 * Handles provider selection based on availability, configuration, and priority.
 */
@Component
public class ProviderManager {

    private static final Logger logger = LoggerFactory.getLogger(ProviderManager.class);

    private final List<SmsProvider> smsProviders;
    private final List<EmailProvider> emailProviders;

    @Autowired
    public ProviderManager(List<SmsProvider> smsProviders, List<EmailProvider> emailProviders) {
        this.smsProviders = smsProviders;
        this.emailProviders = emailProviders;

        logProviderStatus();
    }

    /**
     * Get the best available SMS provider.
     * Selects based on enabled status and priority.
     *
     * @return The best available SMS provider, or empty if none available
     */
    public Optional<SmsProvider> getBestSmsProvider() {
        return smsProviders.stream()
            .filter(SmsProvider::isEnabled)
            .max(Comparator.comparingInt(SmsProvider::getPriority));
    }

    /**
     * Get a specific SMS provider by name.
     *
     * @param providerName Provider name (e.g., "TWILIO", "SOLAPI")
     * @return The provider if found and enabled
     */
    public Optional<SmsProvider> getSmsProvider(String providerName) {
        return smsProviders.stream()
            .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
            .filter(SmsProvider::isEnabled)
            .findFirst();
    }

    /**
     * Get all enabled SMS providers.
     *
     * @return List of enabled SMS providers
     */
    public List<SmsProvider> getEnabledSmsProviders() {
        return smsProviders.stream()
            .filter(SmsProvider::isEnabled)
            .sorted(Comparator.comparingInt(SmsProvider::getPriority).reversed())
            .toList();
    }

    /**
     * Get the best available Email provider.
     * Selects based on enabled status and priority.
     *
     * @return The best available Email provider, or empty if none available
     */
    public Optional<EmailProvider> getBestEmailProvider() {
        return emailProviders.stream()
            .filter(EmailProvider::isEnabled)
            .max(Comparator.comparingInt(EmailProvider::getPriority));
    }

    /**
     * Get a specific Email provider by name.
     *
     * @param providerName Provider name (e.g., "SMTP", "SENDGRID")
     * @return The provider if found and enabled
     */
    public Optional<EmailProvider> getEmailProvider(String providerName) {
        return emailProviders.stream()
            .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
            .filter(EmailProvider::isEnabled)
            .findFirst();
    }

    /**
     * Get all enabled Email providers.
     *
     * @return List of enabled Email providers
     */
    public List<EmailProvider> getEnabledEmailProviders() {
        return emailProviders.stream()
            .filter(EmailProvider::isEnabled)
            .sorted(Comparator.comparingInt(EmailProvider::getPriority).reversed())
            .toList();
    }

    /**
     * Try to send SMS with fallback providers.
     * If the primary provider fails, tries the next available provider.
     *
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @param metadata Message metadata
     * @return Result from the first successful provider
     */
    public SmsProvider.SmsResult sendSmsWithFallback(String phoneNumber, String message,
                                                     java.util.Map<String, Object> metadata) {
        List<SmsProvider> providers = getEnabledSmsProviders();

        if (providers.isEmpty()) {
            logger.error("No SMS providers available");
            return SmsProvider.SmsResult.failure("NO_PROVIDER", "No SMS provider is configured and enabled");
        }

        for (SmsProvider provider : providers) {
            try {
                logger.info("Attempting to send SMS via {}", provider.getProviderName());
                SmsProvider.SmsResult result = provider.sendSms(phoneNumber, message, metadata);

                if (result.isSuccess()) {
                    logger.info("SMS sent successfully via {}", provider.getProviderName());
                    return result;
                } else {
                    logger.warn("Failed to send SMS via {}: {} - {}",
                        provider.getProviderName(), result.getErrorCode(), result.getErrorMessage());
                }
            } catch (Exception e) {
                logger.error("Error sending SMS via {}", provider.getProviderName(), e);
            }
        }

        return SmsProvider.SmsResult.failure("ALL_PROVIDERS_FAILED",
            "Failed to send SMS through all available providers");
    }

    /**
     * Try to send Email with fallback providers.
     *
     * @param to Recipient email
     * @param subject Email subject
     * @param body Email body
     * @param isHtml Whether the body is HTML
     * @param metadata Message metadata
     * @return Result from the first successful provider
     */
    public EmailProvider.EmailResult sendEmailWithFallback(String to, String subject, String body,
                                                           boolean isHtml, java.util.Map<String, Object> metadata) {
        List<EmailProvider> providers = getEnabledEmailProviders();

        if (providers.isEmpty()) {
            logger.error("No Email providers available");
            return EmailProvider.EmailResult.failure("NO_PROVIDER",
                "No Email provider is configured and enabled");
        }

        for (EmailProvider provider : providers) {
            try {
                logger.info("Attempting to send Email via {}", provider.getProviderName());
                EmailProvider.EmailResult result = provider.sendEmail(to, subject, body, isHtml, metadata);

                if (result.isSuccess()) {
                    logger.info("Email sent successfully via {}", provider.getProviderName());
                    return result;
                } else {
                    logger.warn("Failed to send Email via {}: {} - {}",
                        provider.getProviderName(), result.getErrorCode(), result.getErrorMessage());
                }
            } catch (Exception e) {
                logger.error("Error sending Email via {}", provider.getProviderName(), e);
            }
        }

        return EmailProvider.EmailResult.failure("ALL_PROVIDERS_FAILED",
            "Failed to send Email through all available providers");
    }

    private void logProviderStatus() {
        logger.info("=== Provider Status ===");

        logger.info("SMS Providers:");
        for (SmsProvider provider : smsProviders) {
            logger.info("  - {} [Priority: {}]: {}",
                provider.getProviderName(),
                provider.getPriority(),
                provider.isEnabled() ? "ENABLED" : "DISABLED");
        }

        logger.info("Email Providers:");
        for (EmailProvider provider : emailProviders) {
            logger.info("  - {} [Priority: {}]: {}",
                provider.getProviderName(),
                provider.getPriority(),
                provider.isEnabled() ? "ENABLED" : "DISABLED");
        }
    }
}