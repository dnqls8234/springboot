package com.mindshift.ums.performance;

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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for UMS Service.
 * Tests throughput, latency, and scalability under various load conditions.
 */
@SpringBootTest(classes = UmsApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class MessagePerformanceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ums_perf_test")
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

        // Disable external services for performance tests
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9999");
        registry.add("ums.rate-limit.enabled", () -> "false");
        registry.add("ums.security.hmac.enabled", () -> "false");
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
        testTenant.setTenantId("perf-test-tenant");
        testTenant.setTenantName("Performance Test Tenant");
        testTenant.setApiKey("perf-test-api-key");
        testTenant.setApiSecret("perf-test-secret");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setRateLimit(Map.of("per_hour", 100000));
        testTenant = tenantConfigRepository.save(testTenant);

        // Create test template
        testTemplate = new Template();
        testTemplate.setTenantId(testTenant.getTenantId());
        testTemplate.setCode("perf_test_sms");
        testTemplate.setChannel(ChannelType.SMS);
        testTemplate.setLocale("ko");
        testTemplate.setTitleTemplate("성능 테스트");
        testTemplate.setBodyTemplate("{{username}}님, 성능 테스트 메시지입니다.");
        testTemplate.setActive(true);
        testTemplate = templateRepository.save(testTemplate);
    }

    @Test
    void testSingleMessageLatency() throws Exception {
        SendMessageDto.SendMessageRequest request = createTestRequest("latency-test-001");

        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "latency-test-001")
                .header("Authorization", "Bearer perf-test-api-key")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;

        System.out.println("Single message latency: " + latency + "ms");

        // Assert latency is under 100ms
        assert latency < 100 : "Message latency too high: " + latency + "ms";
    }

    @Test
    void testConcurrentMessageThroughput() throws Exception {
        int numberOfThreads = 10;
        int messagesPerThread = 50;
        int totalMessages = numberOfThreads * messagesPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    try {
                        String idempotencyKey = String.format("throughput-test-%d-%d", threadId, j);
                        SendMessageDto.SendMessageRequest request = createTestRequest(idempotencyKey);

                        mockMvc.perform(post("/v1/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Idempotency-Key", idempotencyKey)
                                .header("Authorization", "Bearer perf-test-api-key")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isAccepted());

                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Error in thread " + threadId + ": " + e.getMessage());
                    }
                }
            }, executor);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double throughput = (double) totalMessages / (totalTime / 1000.0);

        System.out.println("=== Concurrent Throughput Test Results ===");
        System.out.println("Total messages: " + totalMessages);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Errors: " + errorCount.get());
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " messages/second");

        executor.shutdown();

        // Assert at least 95% success rate
        double successRate = (double) successCount.get() / totalMessages;
        assert successRate >= 0.95 : "Success rate too low: " + (successRate * 100) + "%";

        // Assert minimum throughput (adjust based on your requirements)
        assert throughput >= 50 : "Throughput too low: " + throughput + " messages/second";
    }

    @Test
    void testMemoryUsageUnderLoad() throws Exception {
        Runtime runtime = Runtime.getRuntime();

        // Force garbage collection and measure initial memory
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        int numberOfMessages = 1000;

        for (int i = 0; i < numberOfMessages; i++) {
            String idempotencyKey = "memory-test-" + i;
            SendMessageDto.SendMessageRequest request = createTestRequest(idempotencyKey);

            mockMvc.perform(post("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Idempotency-Key", idempotencyKey)
                    .header("Authorization", "Bearer perf-test-api-key")
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());
        }

        // Force garbage collection and measure final memory
        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        System.out.println("=== Memory Usage Test Results ===");
        System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory increase: " + (memoryIncrease / 1024 / 1024) + " MB");
        System.out.println("Memory per message: " + (memoryIncrease / numberOfMessages) + " bytes");

        // Assert memory increase is reasonable (less than 50MB for 1000 messages)
        assert memoryIncrease < 50 * 1024 * 1024 : "Memory usage too high: " + (memoryIncrease / 1024 / 1024) + " MB";
    }

    @Test
    void testBurstTrafficHandling() throws Exception {
        int burstSize = 100;
        int numberOfBursts = 5;
        int delayBetweenBursts = 1000; // 1 second

        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalErrors = new AtomicInteger(0);

        for (int burst = 0; burst < numberOfBursts; burst++) {
            System.out.println("Starting burst " + (burst + 1) + "/" + numberOfBursts);

            ExecutorService executor = Executors.newFixedThreadPool(20);
            AtomicInteger burstSuccess = new AtomicInteger(0);
            AtomicInteger burstErrors = new AtomicInteger(0);

            long burstStartTime = System.currentTimeMillis();

            CompletableFuture<Void>[] futures = new CompletableFuture[burstSize];
            for (int i = 0; i < burstSize; i++) {
                final int messageId = burst * burstSize + i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        String idempotencyKey = "burst-test-" + messageId;
                        SendMessageDto.SendMessageRequest request = createTestRequest(idempotencyKey);

                        mockMvc.perform(post("/v1/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Idempotency-Key", idempotencyKey)
                                .header("Authorization", "Bearer perf-test-api-key")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isAccepted());

                        burstSuccess.incrementAndGet();
                    } catch (Exception e) {
                        burstErrors.incrementAndGet();
                    }
                }, executor);
            }

            CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);
            executor.shutdown();

            long burstEndTime = System.currentTimeMillis();
            double burstThroughput = (double) burstSize / ((burstEndTime - burstStartTime) / 1000.0);

            System.out.println("Burst " + (burst + 1) + " completed in " +
                (burstEndTime - burstStartTime) + "ms, throughput: " +
                String.format("%.2f", burstThroughput) + " messages/second");

            totalSuccess.addAndGet(burstSuccess.get());
            totalErrors.addAndGet(burstErrors.get());

            // Wait between bursts
            if (burst < numberOfBursts - 1) {
                Thread.sleep(delayBetweenBursts);
            }
        }

        System.out.println("=== Burst Traffic Test Results ===");
        System.out.println("Total messages: " + (burstSize * numberOfBursts));
        System.out.println("Total successful: " + totalSuccess.get());
        System.out.println("Total errors: " + totalErrors.get());

        // Assert overall success rate
        double overallSuccessRate = (double) totalSuccess.get() / (burstSize * numberOfBursts);
        assert overallSuccessRate >= 0.95 : "Overall success rate too low: " + (overallSuccessRate * 100) + "%";
    }

    private SendMessageDto.SendMessageRequest createTestRequest(String idempotencyKey) {
        SendMessageDto.SendMessageRequest request = new SendMessageDto.SendMessageRequest();
        request.setIdempotencyKey(idempotencyKey);
        request.setChannel(ChannelType.SMS);
        request.setTemplateCode("perf_test_sms");
        request.setLocale("ko");

        SendMessageDto.RecipientDto recipient = new SendMessageDto.RecipientDto();
        recipient.setPhone("+821012345678");
        request.setTo(recipient);

        SendMessageDto.RoutingDto routing = new SendMessageDto.RoutingDto();
        routing.setPriority(MessagePriority.NORMAL);
        request.setRouting(routing);

        request.setTemplateData(Map.of("username", "성능테스트"));

        return request;
    }
}