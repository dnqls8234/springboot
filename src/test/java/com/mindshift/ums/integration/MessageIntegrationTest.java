package com.mindshift.ums.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindshift.ums.UmsApplication;
import com.mindshift.ums.api.dto.SendMessageDto;
import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.entity.Template;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.MessagePriority;
import com.mindshift.ums.domain.enums.TenantStatus;
import com.mindshift.ums.repository.TenantConfigRepository;
import com.mindshift.ums.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UmsApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class MessageIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ums_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // Disable Kafka for integration tests
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9999");
        registry.add("ums.providers.kakao.enabled", () -> "false");
        registry.add("ums.providers.sms.enabled", () -> "false");
        registry.add("ums.providers.email.enabled", () -> "false");
        registry.add("ums.providers.fcm.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantConfigRepository tenantConfigRepository;

    @Autowired
    private TemplateRepository templateRepository;

    private TenantConfig testTenant;
    private Template testTemplate;

    @BeforeEach
    void setUp() {
        // Create test tenant
        testTenant = new TenantConfig();
        testTenant.setTenantId("integration-test-tenant");
        testTenant.setTenantName("Integration Test Tenant");
        testTenant.setApiKey("integration-test-api-key");
        testTenant.setApiSecret("integration-test-secret");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setRateLimit(Map.of("per_hour", 1000));
        testTenant = tenantConfigRepository.save(testTenant);

        // Create test template
        testTemplate = new Template();
        testTemplate.setTenantId(testTenant.getTenantId());
        testTemplate.setCode("welcome_sms");
        testTemplate.setChannel(ChannelType.SMS);
        testTemplate.setLocale("ko");
        testTemplate.setTitleTemplate("환영합니다!");
        testTemplate.setBodyTemplate("{{username}}님, 가입을 환영합니다!");
        testTemplate.setActive(true);
        testTemplate = templateRepository.save(testTemplate);
    }

    @Test
    void sendMessage_EndToEnd_Success() throws Exception {
        // Given
        SendMessageDto.SendMessageRequest request = new SendMessageDto.SendMessageRequest();
        request.setChannel(ChannelType.SMS);
        request.setTemplateCode("welcome_sms");
        request.setLocale("ko");

        SendMessageDto.RecipientDto recipient = new SendMessageDto.RecipientDto();
        recipient.setPhone("+821012345678");
        request.setTo(recipient);

        SendMessageDto.RoutingDto routing = new SendMessageDto.RoutingDto();
        routing.setPriority(MessagePriority.NORMAL);
        routing.setTtlSeconds(3600);
        request.setRouting(routing);

        request.setTemplateData(Map.of("username", "김철수"));

        // When & Then
        String responseContent = mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "integration-test-key")
                .header("Authorization", "Bearer integration-test-api-key")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract request ID for status check
        SendMessageDto.AcceptResponse acceptResponse = objectMapper.readValue(responseContent, SendMessageDto.AcceptResponse.class);
        String requestId = acceptResponse.getRequestId();

        // Verify message status
        mockMvc.perform(get("/v1/messages/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.channel").value("SMS"));
    }

    @Test
    void sendMessage_IdempotencyCheck() throws Exception {
        // Given
        SendMessageDto.SendMessageRequest request = new SendMessageDto.SendMessageRequest();
        request.setChannel(ChannelType.SMS);
        request.setTemplateCode("welcome_sms");
        request.setLocale("ko");

        SendMessageDto.RecipientDto recipient = new SendMessageDto.RecipientDto();
        recipient.setPhone("+821012345678");
        request.setTo(recipient);

        SendMessageDto.RoutingDto routing = new SendMessageDto.RoutingDto();
        routing.setPriority(MessagePriority.NORMAL);
        request.setRouting(routing);

        request.setTemplateData(Map.of("username", "김철수"));

        // First request
        String firstResponse = mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "duplicate-test-key")
                .header("Authorization", "Bearer integration-test-api-key")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SendMessageDto.AcceptResponse firstAcceptResponse = objectMapper.readValue(firstResponse, SendMessageDto.AcceptResponse.class);

        // Second request with same idempotency key
        String secondResponse = mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "duplicate-test-key")
                .header("Authorization", "Bearer integration-test-api-key")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SendMessageDto.AcceptResponse secondAcceptResponse = objectMapper.readValue(secondResponse, SendMessageDto.AcceptResponse.class);

        // Should return same request ID
        assertEquals(firstAcceptResponse.getRequestId(), secondAcceptResponse.getRequestId());
    }

    @Test
    void listMessages_Integration() throws Exception {
        // First, send a message
        SendMessageDto.SendMessageRequest request = new SendMessageDto.SendMessageRequest();
        request.setChannel(ChannelType.SMS);
        request.setTemplateCode("welcome_sms");
        request.setLocale("ko");

        SendMessageDto.RecipientDto recipient = new SendMessageDto.RecipientDto();
        recipient.setPhone("+821012345678");
        request.setTo(recipient);

        SendMessageDto.RoutingDto routing = new SendMessageDto.RoutingDto();
        routing.setPriority(MessagePriority.NORMAL);
        request.setRouting(routing);

        request.setTemplateData(Map.of("username", "김철수"));

        mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "list-test-key")
                .header("Authorization", "Bearer integration-test-api-key")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Then list messages
        mockMvc.perform(get("/v1/messages")
                .header("Authorization", "Bearer integration-test-api-key")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages").isNotEmpty())
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }
}