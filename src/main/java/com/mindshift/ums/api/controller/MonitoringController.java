package com.mindshift.ums.api.controller;

import com.mindshift.ums.metrics.UmsMetrics;
import com.mindshift.ums.repository.MessageRepository;
import com.mindshift.ums.repository.TenantConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/monitoring")
@Tag(name = "Monitoring", description = "시스템 모니터링 및 통계 API")
public class MonitoringController {

    private final MessageRepository messageRepository;
    private final TenantConfigRepository tenantConfigRepository;
    private final UmsMetrics umsMetrics;

    @Autowired
    public MonitoringController(MessageRepository messageRepository,
                               TenantConfigRepository tenantConfigRepository,
                               UmsMetrics umsMetrics) {
        this.messageRepository = messageRepository;
        this.tenantConfigRepository = tenantConfigRepository;
        this.umsMetrics = umsMetrics;
    }

    @GetMapping("/stats")
    @Operation(
        summary = "시스템 통계 조회",
        description = "메시지 발송 통계 및 시스템 현황을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "통계 조회 성공",
            content = @Content(schema = @Schema(implementation = SystemStats.class))
        )
    })
    public ResponseEntity<SystemStats> getSystemStats() {
        SystemStats stats = new SystemStats();

        // 메시지 통계
        stats.setTotalMessages(messageRepository.count());
        stats.setTotalTenants(tenantConfigRepository.count());

        // 채널별 통계 (최근 24시간)
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        stats.setMessagesLast24h(messageRepository.countByCreatedAtAfter(yesterday));

        // 상태별 통계
        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("PENDING", messageRepository.countByStatus(com.mindshift.ums.domain.enums.MessageStatus.PENDING));
        statusStats.put("PROCESSING", messageRepository.countByStatus(com.mindshift.ums.domain.enums.MessageStatus.PROCESSING));
        statusStats.put("SENT", messageRepository.countByStatus(com.mindshift.ums.domain.enums.MessageStatus.SENT));
        statusStats.put("DELIVERED", messageRepository.countByStatus(com.mindshift.ums.domain.enums.MessageStatus.DELIVERED));
        statusStats.put("FAILED", messageRepository.countByStatus(com.mindshift.ums.domain.enums.MessageStatus.FAILED));
        stats.setStatusStats(statusStats);

        // 채널별 통계
        Map<String, Long> channelStats = new HashMap<>();
        channelStats.put("SMS", messageRepository.countByChannel(com.mindshift.ums.domain.enums.ChannelType.SMS));
        channelStats.put("EMAIL", messageRepository.countByChannel(com.mindshift.ums.domain.enums.ChannelType.EMAIL));
        channelStats.put("KAKAO_ALIMTALK", messageRepository.countByChannel(com.mindshift.ums.domain.enums.ChannelType.KAKAO_ALIMTALK));
        channelStats.put("FCM_PUSH", messageRepository.countByChannel(com.mindshift.ums.domain.enums.ChannelType.FCM_PUSH));
        stats.setChannelStats(channelStats);

        stats.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health-summary")
    @Operation(
        summary = "시스템 상태 요약",
        description = "시스템의 전반적인 상태를 요약하여 제공합니다."
    )
    public ResponseEntity<HealthSummary> getHealthSummary() {
        HealthSummary summary = new HealthSummary();

        // 기본 상태 정보
        summary.setStatus("UP");
        summary.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 큐 상태 (간단한 예시)
        long pendingMessages = messageRepository.countByStatus(com.mindshift.ums.domain.enums.MessageStatus.PENDING);
        summary.setPendingMessages(pendingMessages);
        summary.setQueueHealthy(pendingMessages < 1000); // 1000개 미만이면 정상

        // 에러율 계산 (최근 1시간)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTotal = messageRepository.countByCreatedAtAfter(oneHourAgo);
        long recentFailed = messageRepository.countByStatusAndCreatedAtAfter(
            com.mindshift.ums.domain.enums.MessageStatus.FAILED, oneHourAgo);

        double errorRate = recentTotal > 0 ? (double) recentFailed / recentTotal * 100 : 0;
        summary.setErrorRate(errorRate);
        summary.setErrorRateHealthy(errorRate < 5.0); // 5% 미만이면 정상

        return ResponseEntity.ok(summary);
    }

    /**
     * 시스템 통계 응답 DTO
     */
    public static class SystemStats {
        private long totalMessages;
        private long totalTenants;
        private long messagesLast24h;
        private Map<String, Long> statusStats;
        private Map<String, Long> channelStats;
        private String timestamp;

        // Getters and setters
        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }

        public long getTotalTenants() { return totalTenants; }
        public void setTotalTenants(long totalTenants) { this.totalTenants = totalTenants; }

        public long getMessagesLast24h() { return messagesLast24h; }
        public void setMessagesLast24h(long messagesLast24h) { this.messagesLast24h = messagesLast24h; }

        public Map<String, Long> getStatusStats() { return statusStats; }
        public void setStatusStats(Map<String, Long> statusStats) { this.statusStats = statusStats; }

        public Map<String, Long> getChannelStats() { return channelStats; }
        public void setChannelStats(Map<String, Long> channelStats) { this.channelStats = channelStats; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 시스템 상태 요약 응답 DTO
     */
    public static class HealthSummary {
        private String status;
        private String timestamp;
        private long pendingMessages;
        private boolean queueHealthy;
        private double errorRate;
        private boolean errorRateHealthy;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public long getPendingMessages() { return pendingMessages; }
        public void setPendingMessages(long pendingMessages) { this.pendingMessages = pendingMessages; }

        public boolean isQueueHealthy() { return queueHealthy; }
        public void setQueueHealthy(boolean queueHealthy) { this.queueHealthy = queueHealthy; }

        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }

        public boolean isErrorRateHealthy() { return errorRateHealthy; }
        public void setErrorRateHealthy(boolean errorRateHealthy) { this.errorRateHealthy = errorRateHealthy; }
    }
}