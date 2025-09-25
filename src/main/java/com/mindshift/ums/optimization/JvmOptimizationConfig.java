package com.mindshift.ums.optimization;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class JvmOptimizationConfig {

    @Bean(name = "messageProcessingExecutor")
    public TaskExecutor messageProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core thread pool sizing
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);

        // Thread naming and lifecycle
        executor.setThreadNamePrefix("msg-proc-");
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);

        // Rejection policy for high load
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    @Bean(name = "channelAdapterExecutor")
    public TaskExecutor channelAdapterExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Optimized for I/O bound operations (channel adapters)
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("channel-");
        executor.setKeepAliveSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    @Bean(name = "kafkaProducerExecutor")
    public TaskExecutor kafkaProducerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Kafka event publishing
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("kafka-");
        executor.setKeepAliveSeconds(30);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "ums-service");
    }
}