package com.mindshift.ums.service.adapter.impl;

import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.service.adapter.ChannelAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Kakao Alimtalk channel adapter for sending business messages.
 */
@Component
public class KakaoAlimtalkAdapter implements ChannelAdapter {

    private static final Logger logger = LoggerFactory.getLogger(KakaoAlimtalkAdapter.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String senderKey;
    private final String apiUrl;

    public KakaoAlimtalkAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${ums.kakao.api-key}") String apiKey,
            @Value("${ums.kakao.sender-key}") String senderKey,
            @Value("${ums.kakao.api-url:https://api.kakaowork.com}") String apiUrl) {

        this.webClient = webClientBuilder
            .baseUrl(apiUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();

        this.apiKey = apiKey;
        this.senderKey = senderKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public SendResult send(Message message) {
        logger.info("Sending Kakao Alimtalk message: {}", message.getRequestId());

        try {
            Map<String, Object> recipient = message.getToJson();
            if (!recipient.containsKey("kakao")) {
                return SendResult.failure("INVALID_RECIPIENT", "Kakao user ID not found in recipient data");
            }

            @SuppressWarnings("unchecked")
            Map<String, String> kakaoInfo = (Map<String, String>) recipient.get("kakao");
            String userId = kakaoInfo.get("userId");

            if (userId == null || userId.trim().isEmpty()) {
                return SendResult.failure("INVALID_USER_ID", "Kakao user ID is empty");
            }

            Map<String, Object> requestBody = buildRequestBody(message, userId);

            String providerMessageId = webClient.post()
                .uri("/v1/message/send")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    logger.error("Kakao API error: {} for message {}", response.statusCode(), message.getRequestId());
                    return response.bodyToMono(String.class)
                        .doOnNext(body -> logger.error("Kakao API error body: {}", body))
                        .then(Mono.error(new RuntimeException("Kakao API error: " + response.statusCode())));
                })
                .bodyToMono(KakaoResponse.class)
                .timeout(Duration.ofSeconds(30))
                .map(response -> response.messageId)
                .block();

            logger.info("Kakao Alimtalk sent successfully: {} -> {}", message.getRequestId(), providerMessageId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("channel", "KAKAO_ALIMTALK");
            metadata.put("userId", userId);
            metadata.put("senderKey", senderKey);

            return SendResult.success(providerMessageId, metadata);

        } catch (Exception e) {
            logger.error("Failed to send Kakao Alimtalk message: {}", message.getRequestId(), e);
            return SendResult.failure("SEND_FAILED", e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(Message message, String userId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sender_key", senderKey);
        requestBody.put("template_code", message.getTemplateCode());
        requestBody.put("receiver_id", userId);
        requestBody.put("message", message.getRenderedBody());

        // Add template variables if available
        if (message.getMeta() != null) {
            requestBody.put("template_parameter", message.getMeta());
        }

        // Add callback information if available
        if (message.getTemplate() != null && message.getTemplate().getCallbackUrl() != null) {
            requestBody.put("callback_url", message.getTemplate().getCallbackUrl());
        }

        return requestBody;
    }

    @Override
    public String getChannelType() {
        return ChannelType.KAKAO_ALIMTALK.name();
    }

    @Override
    public boolean canHandle(Message message) {
        if (message.getChannel() != ChannelType.KAKAO_ALIMTALK) {
            return false;
        }

        Map<String, Object> recipient = message.getToJson();
        return recipient != null && recipient.containsKey("kakao");
    }

    /**
     * Response structure from Kakao API
     */
    private static class KakaoResponse {
        public String messageId;
        public String status;
        public String message;
    }
}