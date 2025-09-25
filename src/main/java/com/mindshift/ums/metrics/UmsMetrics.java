package com.mindshift.ums.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for UMS service monitoring.
 */
@Component
public class UmsMetrics {

    private final MeterRegistry meterRegistry;

    // Message metrics
    private final Counter messagesAccepted;
    private final Counter messagesProcessed;
    private final Counter messagesFailed;
    private final Counter messagesDelivered;

    // Channel-specific metrics
    private final Counter kakaoMessagesSent;
    private final Counter smsMessagesSent;
    private final Counter emailMessagesSent;
    private final Counter fcmMessagesSent;

    // Error metrics
    private final Counter authenticationErrors;
    private final Counter validationErrors;
    private final Counter rateLimitErrors;
    private final Counter templateErrors;

    // Performance metrics
    private final Timer messageProcessingTime;
    private final Timer channelAdapterTime;

    @Autowired
    public UmsMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.messagesAccepted = Counter.builder("ums.messages.accepted")
            .description("Total number of messages accepted")
            .register(meterRegistry);

        this.messagesProcessed = Counter.builder("ums.messages.processed")
            .description("Total number of messages processed")
            .register(meterRegistry);

        this.messagesFailed = Counter.builder("ums.messages.failed")
            .description("Total number of messages failed")
            .register(meterRegistry);

        this.messagesDelivered = Counter.builder("ums.messages.delivered")
            .description("Total number of messages delivered")
            .register(meterRegistry);

        // Channel metrics
        this.kakaoMessagesSent = Counter.builder("ums.messages.channel.kakao")
            .description("Messages sent via Kakao Alimtalk")
            .register(meterRegistry);

        this.smsMessagesSent = Counter.builder("ums.messages.channel.sms")
            .description("Messages sent via SMS")
            .register(meterRegistry);

        this.emailMessagesSent = Counter.builder("ums.messages.channel.email")
            .description("Messages sent via Email")
            .register(meterRegistry);

        this.fcmMessagesSent = Counter.builder("ums.messages.channel.fcm")
            .description("Messages sent via FCM Push")
            .register(meterRegistry);

        // Error metrics
        this.authenticationErrors = Counter.builder("ums.errors.authentication")
            .description("Authentication errors")
            .register(meterRegistry);

        this.validationErrors = Counter.builder("ums.errors.validation")
            .description("Validation errors")
            .register(meterRegistry);

        this.rateLimitErrors = Counter.builder("ums.errors.rate_limit")
            .description("Rate limit errors")
            .register(meterRegistry);

        this.templateErrors = Counter.builder("ums.errors.template")
            .description("Template processing errors")
            .register(meterRegistry);

        // Timer metrics
        this.messageProcessingTime = Timer.builder("ums.processing.time")
            .description("Time taken to process messages")
            .register(meterRegistry);

        this.channelAdapterTime = Timer.builder("ums.channel.adapter.time")
            .description("Time taken by channel adapters")
            .register(meterRegistry);
    }

    // Message metrics methods
    public void incrementMessagesAccepted() {
        messagesAccepted.increment();
    }

    public void incrementMessagesProcessed() {
        messagesProcessed.increment();
    }

    public void incrementMessagesFailed() {
        messagesFailed.increment();
    }

    public void incrementMessagesDelivered() {
        messagesDelivered.increment();
    }

    // Channel-specific methods
    public void incrementKakaoMessages() {
        kakaoMessagesSent.increment();
    }

    public void incrementSmsMessages() {
        smsMessagesSent.increment();
    }

    public void incrementEmailMessages() {
        emailMessagesSent.increment();
    }

    public void incrementFcmMessages() {
        fcmMessagesSent.increment();
    }

    // Error metrics methods
    public void incrementAuthenticationErrors() {
        authenticationErrors.increment();
    }

    public void incrementValidationErrors() {
        validationErrors.increment();
    }

    public void incrementRateLimitErrors() {
        rateLimitErrors.increment();
    }

    public void incrementTemplateErrors() {
        templateErrors.increment();
    }

    // Timer methods
    public Timer.Sample startMessageProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordMessageProcessingTime(Timer.Sample sample) {
        sample.stop(messageProcessingTime);
    }

    public Timer.Sample startChannelAdapterTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordChannelAdapterTime(Timer.Sample sample) {
        sample.stop(channelAdapterTime);
    }

    // Custom gauge for active tenant count
    public void recordActiveTenants(int count) {
        meterRegistry.gauge("ums.tenants.active", count);
    }

    // Custom gauge for message queue size
    public void recordMessageQueueSize(int size) {
        meterRegistry.gauge("ums.queue.size", size);
    }
}