package com.mindshift.ums.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting service using Redis and token bucket algorithm.
 */
@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    private final RedisTemplate<String, String> redisTemplate;

    // Lua script for atomic token bucket operations
    private static final String TOKEN_BUCKET_LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local tokens = tonumber(ARGV[2])
        local interval = tonumber(ARGV[3])
        local requested = tonumber(ARGV[4])

        local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
        local current_tokens = tonumber(bucket[1])
        local last_refill = tonumber(bucket[2])

        local now = redis.call('TIME')
        local current_time = tonumber(now[1])

        if current_tokens == nil then
            current_tokens = capacity
            last_refill = current_time
        end

        -- Calculate tokens to add based on time elapsed
        local time_passed = current_time - last_refill
        local tokens_to_add = math.floor(time_passed / interval * tokens)
        current_tokens = math.min(capacity, current_tokens + tokens_to_add)

        -- Check if enough tokens available
        if current_tokens >= requested then
            current_tokens = current_tokens - requested

            -- Update bucket
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, interval * 2)

            return {1, current_tokens, capacity}
        else
            -- Update last refill time even if request is denied
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, interval * 2)

            return {0, current_tokens, capacity}
        end
        """;

    @Autowired
    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if a request is allowed under rate limiting rules.
     *
     * @param tenantId         Tenant identifier
     * @param limitType        Type of limit (e.g., "messages", "api_calls")
     * @param requestsPerHour  Number of requests allowed per hour
     * @param tokensRequested  Number of tokens requested (default: 1)
     * @return RateLimitResult indicating if request is allowed
     */
    public RateLimitResult checkRateLimit(String tenantId, String limitType, int requestsPerHour, int tokensRequested) {
        String key = buildRateLimitKey(tenantId, limitType);

        try {
            // Token bucket parameters
            int capacity = requestsPerHour;
            int tokensPerHour = requestsPerHour;
            int intervalSeconds = 3600; // 1 hour

            RedisScript<Long> script = RedisScript.of(TOKEN_BUCKET_LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(tokensPerHour),
                String.valueOf(intervalSeconds),
                String.valueOf(tokensRequested));

            // Parse result: [allowed (1/0), remaining_tokens, capacity]
            boolean allowed = result != null && result == 1;

            // Get remaining tokens for rate limit headers
            String[] bucketInfo = redisTemplate.opsForHash().multiGet(key,
                java.util.List.of("tokens", "last_refill")).toArray(new String[0]);

            int remainingTokens = bucketInfo[0] != null ? Integer.parseInt(bucketInfo[0]) : capacity;

            logger.debug("Rate limit check for {}: allowed={}, remaining={}, capacity={}",
                key, allowed, remainingTokens, capacity);

            return new RateLimitResult(allowed, remainingTokens, capacity, calculateResetTime(intervalSeconds));

        } catch (Exception e) {
            logger.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if rate limiting fails
            return new RateLimitResult(true, requestsPerHour, requestsPerHour, System.currentTimeMillis() + 3600000);
        }
    }

    /**
     * Check rate limit with default 1 token request.
     */
    public RateLimitResult checkRateLimit(String tenantId, String limitType, int requestsPerHour) {
        return checkRateLimit(tenantId, limitType, requestsPerHour, 1);
    }

    /**
     * Reset rate limit for a tenant and limit type.
     *
     * @param tenantId  Tenant identifier
     * @param limitType Type of limit
     */
    public void resetRateLimit(String tenantId, String limitType) {
        String key = buildRateLimitKey(tenantId, limitType);
        redisTemplate.delete(key);
        logger.info("Rate limit reset for key: {}", key);
    }

    /**
     * Get current rate limit status without consuming tokens.
     *
     * @param tenantId  Tenant identifier
     * @param limitType Type of limit
     * @return Current rate limit status
     */
    public RateLimitStatus getRateLimitStatus(String tenantId, String limitType) {
        String key = buildRateLimitKey(tenantId, limitType);

        try {
            String[] bucketInfo = redisTemplate.opsForHash().multiGet(key,
                java.util.List.of("tokens", "last_refill")).toArray(new String[0]);

            if (bucketInfo[0] == null) {
                // No rate limit data exists yet
                return new RateLimitStatus(0, 0, 0, System.currentTimeMillis());
            }

            int currentTokens = Integer.parseInt(bucketInfo[0]);
            long lastRefill = Long.parseLong(bucketInfo[1]);

            return new RateLimitStatus(currentTokens, 0, 0, lastRefill * 1000);

        } catch (Exception e) {
            logger.error("Error getting rate limit status for key: {}", key, e);
            return new RateLimitStatus(0, 0, 0, System.currentTimeMillis());
        }
    }

    private String buildRateLimitKey(String tenantId, String limitType) {
        return String.format("rate_limit:%s:%s", tenantId, limitType);
    }

    private long calculateResetTime(int intervalSeconds) {
        return System.currentTimeMillis() + (intervalSeconds * 1000L);
    }

    /**
     * Result of a rate limit check.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final int remainingTokens;
        private final int capacity;
        private final long resetTimeMillis;

        public RateLimitResult(boolean allowed, int remainingTokens, int capacity, long resetTimeMillis) {
            this.allowed = allowed;
            this.remainingTokens = remainingTokens;
            this.capacity = capacity;
            this.resetTimeMillis = resetTimeMillis;
        }

        public boolean isAllowed() { return allowed; }
        public int getRemainingTokens() { return remainingTokens; }
        public int getCapacity() { return capacity; }
        public long getResetTimeMillis() { return resetTimeMillis; }
        public int getResetTimeSeconds() { return (int) (resetTimeMillis / 1000); }
    }

    /**
     * Current rate limit status.
     */
    public static class RateLimitStatus {
        private final int currentTokens;
        private final int requestsMade;
        private final int requestsRemaining;
        private final long lastUpdateMillis;

        public RateLimitStatus(int currentTokens, int requestsMade, int requestsRemaining, long lastUpdateMillis) {
            this.currentTokens = currentTokens;
            this.requestsMade = requestsMade;
            this.requestsRemaining = requestsRemaining;
            this.lastUpdateMillis = lastUpdateMillis;
        }

        public int getCurrentTokens() { return currentTokens; }
        public int getRequestsMade() { return requestsMade; }
        public int getRequestsRemaining() { return requestsRemaining; }
        public long getLastUpdateMillis() { return lastUpdateMillis; }
    }
}