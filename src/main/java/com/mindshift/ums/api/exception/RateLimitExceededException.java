package com.mindshift.ums.api.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class RateLimitExceededException extends UmsException {

    private int remainingTokens;
    private int resetTimeSeconds;

    public RateLimitExceededException(String tenantId, int limit) {
        super("Rate limit exceeded for tenant: " + tenantId, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS,
              Map.of("limit", limit));
    }

    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
    }

    public RateLimitExceededException(String message, int remainingTokens, int resetTimeSeconds) {
        super(message, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS,
              Map.of("remainingTokens", remainingTokens, "resetTimeSeconds", resetTimeSeconds));
        this.remainingTokens = remainingTokens;
        this.resetTimeSeconds = resetTimeSeconds;
    }

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public int getResetTimeSeconds() {
        return resetTimeSeconds;
    }
}