package com.mindshift.ums.domain.entity;

import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.TemplateStatus;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "templates")
@EntityListeners(AuditingEntityListener.class)
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private ChannelType channel;

    @Column(nullable = false, length = 10)
    private String locale = "ko-KR";

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> variablesSchema;

    // Kakao specific fields
    @Column(name = "kakao_template_id", length = 100)
    private String kakaoTemplateId;

    @Column(name = "kakao_template_type", length = 20)
    private String kakaoTemplateType;

    @Type(JsonType.class)
    @Column(name = "kakao_buttons", columnDefinition = "jsonb")
    private Map<String, Object> kakaoButtons;

    // SMS specific fields
    @Column(name = "sms_type", length = 10)
    private String smsType;

    @Column(name = "sms_sender_no", length = 20)
    private String smsSenderNo;

    // Email specific fields
    @Column(name = "email_subject_template", columnDefinition = "TEXT")
    private String emailSubjectTemplate;

    @Column(name = "email_from_name", length = 100)
    private String emailFromName;

    @Column(name = "email_from_address", length = 255)
    private String emailFromAddress;

    @Column(name = "email_reply_to", length = 255)
    private String emailReplyTo;

    // FCM specific fields
    @Column(name = "fcm_title_template", columnDefinition = "TEXT")
    private String fcmTitleTemplate;

    @Column(name = "fcm_icon", length = 255)
    private String fcmIcon;

    @Column(name = "fcm_color", length = 7)
    private String fcmColor;

    @Column(name = "fcm_sound", length = 50)
    private String fcmSound;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private TemplateStatus status = TemplateStatus.ACTIVE;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Template() {}

    public Template(String code, ChannelType channel, String locale, String title, String body) {
        this.code = code;
        this.channel = channel;
        this.locale = locale;
        this.title = title;
        this.body = body;
    }

    // Business methods
    public boolean isActive() {
        return status == TemplateStatus.ACTIVE;
    }

    public List<String> validateVariables(Map<String, Object> variables) {
        List<String> missingVariables = new ArrayList<>();

        if (variablesSchema != null) {
            variablesSchema.forEach((key, schema) -> {
                if (schema instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> schemaMap = (Map<String, Object>) schema;
                    Boolean required = (Boolean) schemaMap.get("required");
                    if (Boolean.TRUE.equals(required) && !variables.containsKey(key)) {
                        missingVariables.add(key);
                    }
                }
            });
        }

        return missingVariables;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public ChannelType getChannel() { return channel; }
    public void setChannel(ChannelType channel) { this.channel = channel; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Map<String, Object> getVariablesSchema() { return variablesSchema; }
    public void setVariablesSchema(Map<String, Object> variablesSchema) { this.variablesSchema = variablesSchema; }

    public String getKakaoTemplateId() { return kakaoTemplateId; }
    public void setKakaoTemplateId(String kakaoTemplateId) { this.kakaoTemplateId = kakaoTemplateId; }

    public String getKakaoTemplateType() { return kakaoTemplateType; }
    public void setKakaoTemplateType(String kakaoTemplateType) { this.kakaoTemplateType = kakaoTemplateType; }

    public Map<String, Object> getKakaoButtons() { return kakaoButtons; }
    public void setKakaoButtons(Map<String, Object> kakaoButtons) { this.kakaoButtons = kakaoButtons; }

    public String getSmsType() { return smsType; }
    public void setSmsType(String smsType) { this.smsType = smsType; }

    public String getSmsSenderNo() { return smsSenderNo; }
    public void setSmsSenderNo(String smsSenderNo) { this.smsSenderNo = smsSenderNo; }

    public String getEmailSubjectTemplate() { return emailSubjectTemplate; }
    public void setEmailSubjectTemplate(String emailSubjectTemplate) { this.emailSubjectTemplate = emailSubjectTemplate; }

    public String getEmailFromName() { return emailFromName; }
    public void setEmailFromName(String emailFromName) { this.emailFromName = emailFromName; }

    public String getEmailFromAddress() { return emailFromAddress; }
    public void setEmailFromAddress(String emailFromAddress) { this.emailFromAddress = emailFromAddress; }

    public String getEmailReplyTo() { return emailReplyTo; }
    public void setEmailReplyTo(String emailReplyTo) { this.emailReplyTo = emailReplyTo; }

    public String getFcmTitleTemplate() { return fcmTitleTemplate; }
    public void setFcmTitleTemplate(String fcmTitleTemplate) { this.fcmTitleTemplate = fcmTitleTemplate; }

    public String getFcmIcon() { return fcmIcon; }
    public void setFcmIcon(String fcmIcon) { this.fcmIcon = fcmIcon; }

    public String getFcmColor() { return fcmColor; }
    public void setFcmColor(String fcmColor) { this.fcmColor = fcmColor; }

    public String getFcmSound() { return fcmSound; }
    public void setFcmSound(String fcmSound) { this.fcmSound = fcmSound; }

    public TemplateStatus getStatus() { return status; }
    public void setStatus(TemplateStatus status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Compatibility methods for TemplateService
    public String getTitleTemplate() {
        return title;
    }

    public String getBodyTemplate() {
        return body;
    }

    // Callback URL for external services (if needed)
    public String getCallbackUrl() {
        return null; // Default implementation - can be extended later
    }
}