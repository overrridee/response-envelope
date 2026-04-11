package io.github.responseenvelope.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Request ID generator.
 *
 * <p>Generates unique request IDs for distributed tracing.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Component
public class RequestIdGenerator {

    private static final String PREFIX = "req_";
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong COUNTER = new AtomicLong(0);

    /**
     * Generates unique request ID.
     *
     * <p>Format: req_[timestamp_base36]_[random_6char]</p>
     *
     * @return unique request ID
     */
    public String generate() {
        long timestamp = Instant.now().toEpochMilli();
        String timestampPart = Long.toString(timestamp, 36);
        String randomPart = generateRandomString(6);
        return PREFIX + timestampPart + "_" + randomPart;
    }

    /**
     * Generates UUID-based request ID.
     *
     * @return UUID based request ID
     */
    public String generateUUID() {
        return PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates short format request ID.
     *
     * <p>Format: req_[8char]</p>
     *
     * @return short request ID
     */
    public String generateShort() {
        return PREFIX + generateRandomString(8);
    }

    /**
     * Generates sequential request ID.
     *
     * <p>Format: req_[counter]_[random_4char]</p>
     *
     * @return sequential request ID
     */
    public String generateSequential() {
        long count = COUNTER.incrementAndGet();
        String randomPart = generateRandomString(4);
        return PREFIX + count + "_" + randomPart;
    }

    /**
     * Generates prefixed request ID.
     *
     * @param customPrefix custom prefix
     * @return prefixed request ID
     */
    public String generateWithPrefix(String customPrefix) {
        return customPrefix + "_" + generateRandomString(12);
    }

    /**
     * Generates random string.
     *
     * @param length string length
     * @return random string
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * Checks if request ID is valid.
     *
     * @param requestId ID to check
     * @return true if valid
     */
    public boolean isValid(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            return false;
        }
        return requestId.startsWith(PREFIX) && requestId.length() >= 10;
    }
}
