package io.github.overrridee.model;

import io.github.overrridee.annotation.ResponseEnvelope;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Carries request context information.
 *
 * <p>Created by Aspect and used in response creation process.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
public class EnvelopeContext {

    /**
     * Request start time.
     */
    private final Instant startTime;

    /**
     * Request ID.
     */
    private final String requestId;

    /**
     * Request path.
     */
    private final String path;

    /**
     * HTTP method.
     */
    private final String method;

    /**
     * Query parameters.
     */
    private final String queryString;

    /**
     * Client IP address.
     */
    private final String clientIp;

    /**
     * User-Agent header.
     */
    private final String userAgent;

    /**
     * Annotation configuration.
     */
    private final ResponseEnvelope config;

    /**
     * Custom metadata.
     */
    @Builder.Default
    private final Map<String, Object> customMetadata = new HashMap<>();

    /**
     * Calculates processing time.
     *
     * @return processing time (ms)
     */
    public long calculateDuration() {
        return Instant.now().toEpochMilli() - startTime.toEpochMilli();
    }

    /**
     * Adds custom metadata.
     *
     * @param key   metadata key
     * @param value metadata value
     */
    public void addMetadata(String key, Object value) {
        customMetadata.put(key, value);
    }

    /**
     * Creates full URL.
     *
     * @return full URL with query string
     */
    public String getFullPath() {
        if (queryString != null && !queryString.isEmpty()) {
            return path + "?" + queryString;
        }
        return path;
    }
}
