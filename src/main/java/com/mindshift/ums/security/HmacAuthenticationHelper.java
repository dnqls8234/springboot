package com.mindshift.ums.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Helper class for HMAC-SHA256 authentication.
 */
@Component
public class HmacAuthenticationHelper {

    private static final Logger logger = LoggerFactory.getLogger(HmacAuthenticationHelper.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300; // 5 minutes

    /**
     * Generate HMAC signature for request authentication.
     *
     * @param method    HTTP method (GET, POST, etc.)
     * @param uri       Request URI
     * @param body      Request body (empty string for GET requests)
     * @param timestamp Unix timestamp
     * @param apiSecret API secret key
     * @return HMAC signature in hexadecimal format
     */
    public String generateSignature(String method, String uri, String body, long timestamp, String apiSecret) {
        try {
            // Create the string to sign: METHOD|URI|BODY|TIMESTAMP
            String stringToSign = method.toUpperCase() + "|" + uri + "|" + body + "|" + timestamp;

            // Generate HMAC-SHA256 signature
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] signature = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to generate HMAC signature", e);
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    /**
     * Validate HMAC signature from request.
     *
     * @param method         HTTP method
     * @param uri           Request URI
     * @param body          Request body
     * @param timestamp     Timestamp from request header
     * @param providedSignature Signature from request header
     * @param apiSecret     API secret key
     * @return true if signature is valid and timestamp is within tolerance
     */
    public boolean validateSignature(String method, String uri, String body, long timestamp,
                                   String providedSignature, String apiSecret) {
        try {
            // Check timestamp tolerance
            long currentTimestamp = Instant.now().getEpochSecond();
            if (Math.abs(currentTimestamp - timestamp) > TIMESTAMP_TOLERANCE_SECONDS) {
                logger.warn("Request timestamp is outside tolerance: {} seconds difference",
                    Math.abs(currentTimestamp - timestamp));
                return false;
            }

            // Generate expected signature
            String expectedSignature = generateSignature(method, uri, body, timestamp, apiSecret);

            // Compare signatures using constant-time comparison
            return constantTimeEquals(expectedSignature, providedSignature);

        } catch (Exception e) {
            logger.error("Error validating HMAC signature", e);
            return false;
        }
    }

    /**
     * Extract API key from Authorization header.
     * Expected format: "Bearer <api_key>" or "ApiKey <api_key>"
     *
     * @param authorizationHeader Authorization header value
     * @return API key or null if invalid format
     */
    public String extractApiKey(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            return null;
        }

        String trimmed = authorizationHeader.trim();

        if (trimmed.startsWith("Bearer ")) {
            return trimmed.substring(7);
        } else if (trimmed.startsWith("ApiKey ")) {
            return trimmed.substring(7);
        } else {
            // Assume the entire header value is the API key
            return trimmed;
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     *
     * @param expected Expected signature
     * @param actual   Actual signature
     * @return true if strings are equal
     */
    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return expected == actual;
        }

        if (expected.length() != actual.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }

        return result == 0;
    }

    /**
     * Generate a current timestamp.
     *
     * @return Current Unix timestamp
     */
    public long getCurrentTimestamp() {
        return Instant.now().getEpochSecond();
    }

    /**
     * Validate timestamp is within acceptable range.
     *
     * @param timestamp Timestamp to validate
     * @return true if timestamp is within tolerance
     */
    public boolean isTimestampValid(long timestamp) {
        long currentTimestamp = getCurrentTimestamp();
        return Math.abs(currentTimestamp - timestamp) <= TIMESTAMP_TOLERANCE_SECONDS;
    }
}