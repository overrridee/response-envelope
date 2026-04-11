package io.github.responseenvelope.config;

import io.github.responseenvelope.enums.TimestampFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Response Envelope configuration properties.
 *
 * <h2>Example application.yml:</h2>
 * <pre>{@code
 * response-envelope:
 *   enabled: true
 *   default-config:
 *     include-timestamp: true
 *     include-request-id: true
 *     include-path: true
 *     include-duration: true
 *     timestamp-format: ISO_8601
 *     request-id-header: X-Request-ID
 *   error-config:
 *     include-stacktrace: false
 *     include-exception-class: true
 *   metrics:
 *     enabled: true
 *     slo-duration: 500
 * }</pre>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "response-envelope")
public class EnvelopeProperties {

    /**
     * Enables or disables the Response Envelope feature.
     */
    private boolean enabled = true;

    /**
     * Default configuration.
     */
    @NestedConfigurationProperty
    private DefaultConfig defaultConfig = new DefaultConfig();

    /**
     * Error configuration.
     */
    @NestedConfigurationProperty
    private ErrorConfig errorConfig = new ErrorConfig();

    /**
     * Metrics configuration.
     */
    @NestedConfigurationProperty
    private MetricsConfig metrics = new MetricsConfig();

    /**
     * Cache configuration.
     */
    @NestedConfigurationProperty
    private CacheConfig cache = new CacheConfig();

    /**
     * Compression configuration.
     */
    @NestedConfigurationProperty
    private CompressionConfig compression = new CompressionConfig();

    /**
     * Request ID configuration.
     */
    @NestedConfigurationProperty
    private RequestIdConfig requestId = new RequestIdConfig();

    /**
     * Version-specific configurations.
     */
    private Map<String, VersionConfig> versions = new HashMap<>();

    /**
     * Default configuration.
     */
    @Getter
    @Setter
    public static class DefaultConfig {

        /**
         * Should timestamp be included?
         */
        private boolean includeTimestamp = true;

        /**
         * Should request ID be included?
         */
        private boolean includeRequestId = true;

        /**
         * Should path be included?
         */
        private boolean includePath = true;

        /**
         * Should method be included?
         */
        private boolean includeMethod = true;

        /**
         * Should duration be included?
         */
        private boolean includeDuration = true;

        /**
         * Should API version be included?
         */
        private boolean includeApiVersion = true;

        /**
         * Timestamp format.
         */
        private TimestampFormat timestampFormat = TimestampFormat.ISO_8601;

        /**
         * Custom timestamp pattern (for CUSTOM format).
         */
        private String customTimestampPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

        /**
         * Request ID header name.
         */
        private String requestIdHeader = "X-Request-ID";

        /**
         * Should request ID header be added to response?
         */
        private boolean propagateRequestId = true;

        /**
         * Default API version.
         */
        private String defaultVersion = "v1";

        /**
         * Should null values be included?
         */
        private boolean includeNulls = false;

        /**
         * Default data key.
         */
        private String dataKey = "data";

        /**
         * Timezone.
         */
        private String timezone = "UTC";
    }

    /**
     * Error configuration.
     */
    @Getter
    @Setter
    public static class ErrorConfig {

        /**
         * Is global exception handler active?
         */
        private boolean enabled = true;

        /**
         * Should stack trace be included?
         */
        private boolean includeStacktrace = false;

        /**
         * Should exception class name be included?
         */
        private boolean includeExceptionClass = true;

        /**
         * Should detailed error messages be shown?
         */
        private boolean showDetailedMessages = true;

        /**
         * Should field info be shown for validation errors?
         */
        private boolean includeFieldErrors = true;

        /**
         * Documentation URL pattern.
         */
        private String documentationUrlPattern = "";

        /**
         * Error source (service name).
         */
        private String errorSource = "";

        /**
         * Hide sensitive info in production mode.
         */
        private boolean hideInProduction = true;
    }

    /**
     * Metrics configuration.
     */
    @Getter
    @Setter
    public static class MetricsConfig {

        /**
         * Are metrics active?
         */
        private boolean enabled = false;

        /**
         * SLO duration (ms).
         */
        private long sloDuration = 500;

        /**
         * Histogram buckets.
         */
        private double[] histogramBuckets = {0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0};

        /**
         * Metrik prefix.
         */
        private String prefix = "envelope_response";

        /**
         * Tag'ler.
         */
        private Map<String, String> tags = new HashMap<>();
    }

    /**
     * Cache configuration.
     */
    @Getter
    @Setter
    public static class CacheConfig {

        /**
         * Is cache active?
         */
        private boolean enabled = false;

        /**
         * Default cache duration (seconds).
         */
        private int defaultDuration = 60;

        /**
         * Should Cache-Control header be added?
         */
        private boolean addCacheControlHeader = true;

        /**
         * ETag support.
         */
        private boolean etagEnabled = false;
    }

    /**
     * Compression configuration.
     */
    @Getter
    @Setter
    public static class CompressionConfig {

        /**
         * Is compression active?
         */
        private boolean enabled = false;

        /**
         * Minimum size (bytes).
         */
        private int minSize = 1024;

        /**
         * Compression level (1-9).
         */
        private int level = 6;

        /**
         * Content types to be compressed.
         */
        private String[] contentTypes = {"application/json", "text/plain"};
    }

    /**
     * Request ID configuration.
     */
    @Getter
    @Setter
    public static class RequestIdConfig {

        /**
         * Is Request ID filter active?
         */
        private boolean enabled = true;

        /**
         * Request ID header name.
         */
        private String header = "X-Request-ID";

        /**
         * Should Request ID be added to response header?
         */
        private boolean propagate = true;

        /**
         * ID generation strategy (UUID, SHORT, SEQUENTIAL, CUSTOM).
         */
        private String strategy = "UUID";

        /**
         * Prefix (optional).
         */
        private String prefix = "req_";
    }

    /**
     * Version-based configuration.
     */
    @Getter
    @Setter
    public static class VersionConfig {

        /**
         * Custom data key for this version.
         */
        private String dataKey;

        /**
         * Custom timestamp format for this version.
         */
        private TimestampFormat timestampFormat;

        /**
         * Legacy mode.
         */
        private boolean legacyMode = false;

        /**
         * Deprecation warning.
         */
        private String deprecationWarning;

        /**
         * Sunset date.
         */
        private String sunsetDate;
    }
}
