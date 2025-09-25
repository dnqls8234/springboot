package com.mindshift.ums.optimization;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheOptimizationConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Template cache - longer TTL since templates don't change often
        cacheConfigurations.put("templates", defaultConfig
                .entryTtl(Duration.ofHours(2)));

        // Tenant config cache - medium TTL
        cacheConfigurations.put("tenants", defaultConfig
                .entryTtl(Duration.ofMinutes(30)));

        // Rate limit cache - short TTL for accurate rate limiting
        cacheConfigurations.put("rateLimits", defaultConfig
                .entryTtl(Duration.ofMinutes(1)));

        // Message cache - for idempotency checks
        cacheConfigurations.put("messages", defaultConfig
                .entryTtl(Duration.ofHours(24)));

        // Provider status cache - for circuit breaker patterns
        cacheConfigurations.put("providerStatus", defaultConfig
                .entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}