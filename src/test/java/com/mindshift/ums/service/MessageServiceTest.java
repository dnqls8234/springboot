package com.mindshift.ums.service;

import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.api.exception.MessageNotFoundException;
import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.entity.MessageEvent;
import com.mindshift.ums.domain.entity.Template;
import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.MessagePriority;
import com.mindshift.ums.domain.enums.MessageStatus;
import com.mindshift.ums.repository.MessageEventRepository;
import com.mindshift.ums.repository.MessageRepository;
import com.mindshift.ums.repository.TenantConfigRepository;
import com.mindshift.ums.service.kafka.KafkaProducerService;
import com.mindshift.ums.service.security.RateLimitService;
import com.mindshift.ums.service.security.RecipientPolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageEventRepository messageEventRepository;

    @Mock
    private TenantConfigRepository tenantConfigRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ValidationService validationService;

    @Mock
    private TemplateService templateService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private RecipientPolicyService recipientPolicyService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private MessageService messageService;

    private SendMessageDto.SendMessageRequest testRequest;
    private TenantConfig testTenant;
    private Template testTemplate;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        // Setup test data
        testRequest = new SendMessageDto.SendMessageRequest();
        testRequest.setChannel(ChannelType.SMS);
        testRequest.setTemplateCode("welcome_sms");
        testRequest.setLocale("ko");

        SendMessageDto.RecipientDto recipient = new SendMessageDto.RecipientDto();
        recipient.setPhone("+821012345678");
        testRequest.setTo(recipient);

        SendMessageDto.RoutingDto routing = new SendMessageDto.RoutingDto();
        routing.setPriority(MessagePriority.NORMAL);
        routing.setTtlSeconds(3600);
        testRequest.setRouting(routing);

        testRequest.setTemplateData(Map.of("username", "김철수"));

        // Setup test tenant
        testTenant = new TenantConfig();
        testTenant.setTenantId("test-tenant");
        testTenant.setApiKey("test-api-key");
        testTenant.setApiSecret("test-secret");

        // Setup test template
        testTemplate = new Template();
        testTemplate.setId(1L);
        testTemplate.setCode("welcome_sms");
        testTemplate.setTitleTemplate("환영합니다!");
        testTemplate.setBodyTemplate("{{username}}님, 가입을 환영합니다!");

        // Setup test message
        testMessage = new Message("req_123456", "test-tenant", ChannelType.SMS, "welcome_sms");
        testMessage.setId(1L);
        testMessage.setStatus(MessageStatus.PENDING);
    }

    @Test
    void acceptMessage_Success() {
        // Given
        String idempotencyKey = "test-idempotency-key";
        String authorization = "Bearer test-api-key";

        when(authenticationService.authenticateTenant(authorization)).thenReturn(testTenant);
        when(validationService.checkIdempotency(idempotencyKey, testTenant.getTenantId()))
            .thenReturn(Optional.empty());

        when(rateLimitService.checkRateLimit(anyString(), anyString(), anyInt(), anyInt()))
            .thenReturn(new RateLimitService.RateLimitResult(true, 999, 1000, System.currentTimeMillis() + 3600000));

        when(templateService.loadTemplate(testTenant, "welcome_sms", ChannelType.SMS, "ko"))
            .thenReturn(testTemplate);

        when(templateService.renderTemplate(testTemplate, testRequest.getTemplateData()))
            .thenReturn(Map.of("title", "환영합니다!", "body", "김철수님, 가입을 환영합니다!"));

        when(recipientPolicyService.checkRecipientPolicy(testTenant.getTenantId(), testRequest.getTo(), ChannelType.SMS))
            .thenReturn(RecipientPolicyService.PolicyCheckResult.success());

        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(messageEventRepository.save(any(MessageEvent.class))).thenReturn(new MessageEvent());

        // When
        String result = messageService.acceptMessage(testRequest, idempotencyKey, authorization);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("req_"));

        verify(authenticationService).authenticateTenant(authorization);
        verify(validationService).checkIdempotency(idempotencyKey, testTenant.getTenantId());
        verify(validationService).validateMessageRequest(testRequest);
        verify(validationService).validateRecipient(testRequest.getTo(), ChannelType.SMS);
        verify(templateService).loadTemplate(testTenant, "welcome_sms", ChannelType.SMS, "ko");
        verify(templateService).renderTemplate(testTemplate, testRequest.getTemplateData());
        verify(messageRepository).save(any(Message.class));
        verify(messageEventRepository).save(any(MessageEvent.class));
        verify(kafkaProducerService).publishMessageRequested(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(validationService).releaseIdempotencyLock(idempotencyKey);
    }

    @Test
    void acceptMessage_IdempotencyKeyExists() {
        // Given
        String idempotencyKey = "existing-key";
        String authorization = "Bearer test-api-key";

        when(authenticationService.authenticateTenant(authorization)).thenReturn(testTenant);
        when(validationService.checkIdempotency(idempotencyKey, testTenant.getTenantId()))
            .thenReturn(Optional.of(testMessage));

        // When
        String result = messageService.acceptMessage(testRequest, idempotencyKey, authorization);

        // Then
        assertEquals(testMessage.getRequestId(), result);

        verify(authenticationService).authenticateTenant(authorization);
        verify(validationService).checkIdempotency(idempotencyKey, testTenant.getTenantId());
        verify(messageRepository, never()).save(any());
        verify(kafkaProducerService, never()).publishMessageRequested(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void getMessageStatus_Success() {
        // Given
        String requestId = "req_123456";
        when(messageRepository.findByRequestId(requestId)).thenReturn(Optional.of(testMessage));

        // When
        SendMessageDto.MessageStatusResponse result = messageService.getMessageStatus(requestId);

        // Then
        assertNotNull(result);
        assertEquals(requestId, result.getRequestId());
        assertEquals(MessageStatus.PENDING.name(), result.getStatus());
        assertEquals(ChannelType.SMS, result.getChannel());

        verify(messageRepository).findByRequestId(requestId);
    }

    @Test
    void getMessageStatus_NotFound() {
        // Given
        String requestId = "nonexistent";
        when(messageRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MessageNotFoundException.class, () -> {
            messageService.getMessageStatus(requestId);
        });

        verify(messageRepository).findByRequestId(requestId);
    }

    @Test
    void listMessages_Success() {
        // Given
        String authorization = "Bearer test-api-key";
        when(authenticationService.authenticateTenant(authorization)).thenReturn(testTenant);

        org.springframework.data.domain.Page<Message> mockPage = mock(org.springframework.data.domain.Page.class);
        when(mockPage.getContent()).thenReturn(java.util.List.of(testMessage));
        when(mockPage.getTotalElements()).thenReturn(1L);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(mockPage.hasNext()).thenReturn(false);
        when(mockPage.hasPrevious()).thenReturn(false);

        when(messageRepository.findAllByTenantId(eq(testTenant.getTenantId()), any()))
            .thenReturn(mockPage);

        // When
        Map<String, Object> result = messageService.listMessages(authorization, 0, 20, null);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("messages"));
        assertTrue(result.containsKey("pagination"));

        @SuppressWarnings("unchecked")
        java.util.List<SendMessageDto.MessageStatusResponse> messages =
            (java.util.List<SendMessageDto.MessageStatusResponse>) result.get("messages");
        assertEquals(1, messages.size());

        verify(authenticationService).authenticateTenant(authorization);
        verify(messageRepository).findAllByTenantId(eq(testTenant.getTenantId()), any());
    }
}