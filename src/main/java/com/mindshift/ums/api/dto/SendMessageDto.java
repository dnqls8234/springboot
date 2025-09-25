package com.mindshift.ums.api.dto;

import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.MessagePriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendMessageDto {

    public static class SendMessageRequest {
        @NotNull(message = "Channel is required")
        private ChannelType channel;

        @NotBlank(message = "Template code is required")
        @Size(max = 100, message = "Template code must be less than 100 characters")
        private String templateCode;

        @NotNull(message = "Recipient information is required")
        @Valid
        private RecipientDto to;

        @NotBlank(message = "Locale is required")
        @Size(max = 10, message = "Locale must be less than 10 characters")
        private String locale = "ko-KR";

        private Map<String, Object> variables;

        private List<AttachmentDto> attachments;

        @Valid
        private RoutingDto routing;

        private Map<String, Object> meta;

        // Constructors
        public SendMessageRequest() {}

        // Getters and Setters
        public ChannelType getChannel() { return channel; }
        public void setChannel(ChannelType channel) { this.channel = channel; }

        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

        public RecipientDto getTo() { return to; }
        public void setTo(RecipientDto to) { this.to = to; }

        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }

        public Map<String, Object> getVariables() { return variables; }
        public void setVariables(Map<String, Object> variables) { this.variables = variables; }

        public List<AttachmentDto> getAttachments() { return attachments; }
        public void setAttachments(List<AttachmentDto> attachments) { this.attachments = attachments; }

        public RoutingDto getRouting() { return routing; }
        public void setRouting(RoutingDto routing) { this.routing = routing; }

        public Map<String, Object> getMeta() { return meta; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }

        // Compatibility methods
        public Map<String, Object> getTemplateData() {
            return variables;
        }

        public void setTemplateData(Map<String, Object> templateData) {
            this.variables = templateData;
        }

        public String getIdempotencyKey() {
            return meta != null ? (String) meta.get("idempotencyKey") : null;
        }

        public void setIdempotencyKey(String idempotencyKey) {
            if (meta == null) meta = new java.util.HashMap<>();
            meta.put("idempotencyKey", idempotencyKey);
        }
    }

    public static class RecipientDto {
        private String phone;
        private String email;
        private String pushToken;
        private KakaoRecipientDto kakao;

        // Constructors
        public RecipientDto() {}

        // Validation method
        public List<String> validate() {
            List<String> errors = new ArrayList<>();

            if (isBlank(phone) && isBlank(email) && isBlank(pushToken) &&
                (kakao == null || isBlank(kakao.getUserId()))) {
                errors.add("At least one recipient identifier is required");
            }

            if (phone != null && !phone.matches("^01[0-9]{8,9}$")) {
                errors.add("Invalid phone number format");
            }

            if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                errors.add("Invalid email format");
            }

            return errors;
        }

        private boolean isBlank(String str) {
            return str == null || str.trim().isEmpty();
        }

        // Getters and Setters
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPushToken() { return pushToken; }
        public void setPushToken(String pushToken) { this.pushToken = pushToken; }

        public KakaoRecipientDto getKakao() { return kakao; }
        public void setKakao(KakaoRecipientDto kakao) { this.kakao = kakao; }
    }

    public static class KakaoRecipientDto {
        private String userId;

        public KakaoRecipientDto() {}

        public KakaoRecipientDto(String userId) {
            this.userId = userId;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    public static class AttachmentDto {
        @NotBlank(message = "Attachment type is required")
        private String type;

        @NotBlank(message = "Attachment URL is required")
        private String url;

        private String filename;

        public AttachmentDto() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }

    public static class RoutingDto {
        private List<ChannelType> fallback;
        private Integer ttlSeconds;
        private MessagePriority priority = MessagePriority.NORMAL;

        public RoutingDto() {}

        public List<ChannelType> getFallback() { return fallback; }
        public void setFallback(List<ChannelType> fallback) { this.fallback = fallback; }

        public Integer getTtlSeconds() { return ttlSeconds; }
        public void setTtlSeconds(Integer ttlSeconds) { this.ttlSeconds = ttlSeconds; }

        public MessagePriority getPriority() { return priority; }
        public void setPriority(MessagePriority priority) { this.priority = priority; }
    }

    public static class AcceptResponse {
        private String requestId;
        private String status;
        private String timestamp;

        public AcceptResponse() {}

        public AcceptResponse(String requestId, String status, String timestamp) {
            this.requestId = requestId;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class MessageStatusResponse {
        private String requestId;
        private String status;
        private ChannelType channel;
        private MessageTimestamps timestamps;
        private Integer retries;
        private ErrorDetail error;
        private String providerMessageId;
        private Map<String, Object> meta;

        public MessageStatusResponse() {}

        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public ChannelType getChannel() { return channel; }
        public void setChannel(ChannelType channel) { this.channel = channel; }

        public MessageTimestamps getTimestamps() { return timestamps; }
        public void setTimestamps(MessageTimestamps timestamps) { this.timestamps = timestamps; }

        public Integer getRetries() { return retries; }
        public void setRetries(Integer retries) { this.retries = retries; }

        public ErrorDetail getError() { return error; }
        public void setError(ErrorDetail error) { this.error = error; }

        public String getProviderMessageId() { return providerMessageId; }
        public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }

        public Map<String, Object> getMeta() { return meta; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }
    }

    public static class MessageTimestamps {
        private String accepted;
        private String sent;
        private String delivered;
        private String failed;

        public MessageTimestamps() {}

        public MessageTimestamps(String accepted, String sent, String delivered, String failed) {
            this.accepted = accepted;
            this.sent = sent;
            this.delivered = delivered;
            this.failed = failed;
        }

        public String getAccepted() { return accepted; }
        public void setAccepted(String accepted) { this.accepted = accepted; }

        public String getSent() { return sent; }
        public void setSent(String sent) { this.sent = sent; }

        public String getDelivered() { return delivered; }
        public void setDelivered(String delivered) { this.delivered = delivered; }

        public String getFailed() { return failed; }
        public void setFailed(String failed) { this.failed = failed; }
    }

    public static class ErrorDetail {
        private String code;
        private String message;
        private Map<String, Object> details;

        public ErrorDetail() {}

        public ErrorDetail(String code, String message, Map<String, Object> details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }
}