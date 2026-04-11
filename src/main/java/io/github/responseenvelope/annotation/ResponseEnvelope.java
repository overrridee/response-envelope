package io.github.overrridee.annotation;

import io.github.overrridee.enums.TimestampFormat;
import io.github.overrridee.enums.WrapStrategy;

import java.lang.annotation.*;

/**
 * Wraps controller method return value in standard envelope format.
 *
 * <p>When applied to Spring MVC controller methods, this annotation
 * automatically wraps the method's return value in {@code EnvelopeResponse} format.</p>
 *
 * <h2>Basic Usage:</h2>
 * <pre>{@code
 * @GetMapping("/users/{id}")
 * @ResponseEnvelope
 * public User getUser(@PathVariable Long id) {
 *     return userService.findById(id);
 * }
 * }</pre>
 *
 * <h2>Customized Usage:</h2>
 * <pre>{@code
 * @PostMapping("/orders")
 * @ResponseEnvelope(
 *     version = "v2",
 *     successMessage = "Order created successfully",
 *     httpStatus = 201,
 *     includeDuration = true
 * )
 * public Order createOrder(@RequestBody OrderDto dto) {
 *     return orderService.create(dto);
 * }
 * }</pre>
 *
 * @author aedemirsen
 * @version 1.0.0
 * @see io.github.overrridee.aspect.ResponseEnvelopeAspect
 * @see io.github.overrridee.model.EnvelopeResponse
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseEnvelope {

    /**
     * Response version. Provides different format support for different API versions.
     * <p>Example: "v1", "v2", "2024-01"</p>
     *
     * @return version string, default "v1"
     */
    String version() default "v1";

    /**
     * Should timestamp be added?
     * <p>Recommended for debug and audit in production environments.</p>
     *
     * @return true if timestamp should be added, default true
     */
    boolean includeTimestamp() default true;

    /**
     * Timestamp format.
     *
     * @return timestamp format type, default ISO_8601
     */
    TimestampFormat timestampFormat() default TimestampFormat.ISO_8601;

    /**
     * Should request ID be added? Useful for distributed tracing.
     * <p>Critically important for request tracking in microservice architectures.</p>
     *
     * @return true if requestId should be added, default true
     */
    boolean includeRequestId() default true;

    /**
     * Should request path be added?
     *
     * @return true if path should be added, default true
     */
    boolean includePath() default true;

    /**
     * Should HTTP method info be added?
     *
     * @return true if method should be added, default true
     */
    boolean includeMethod() default true;

    /**
     * Should processing time (ms) be added?
     * <p>Useful for performance monitoring.</p>
     *
     * @return true if duration should be added, default true
     */
    boolean includeDuration() default true;

    /**
     * Should API version info be added?
     *
     * @return true if apiVersion should be added, default true
     */
    boolean includeApiVersion() default true;

    /**
     * Response HTTP status code.
     * <p>Used for successful responses. In error cases,
     * determined by exception handler.</p>
     *
     * @return HTTP status code, default 200
     */
    int httpStatus() default 200;

    /**
     * Custom success message.
     * <p>If left empty, message field will not be included in response.</p>
     *
     * @return success message, default empty string
     */
    String successMessage() default "";

    /**
     * Custom key for data field.
     * <p>For example, domain-specific names like "users", "orders" can be used.</p>
     *
     * @return data key, default "data"
     */
    String dataKey() default "data";

    /**
     * Response group info. Can be used for rate limiting and monitoring.
     *
     * @return group name, default "default"
     */
    String group() default "default";

    /**
     * Should null values be visible in JSON?
     *
     * @return true if null values should be included, default false
     */
    boolean includeNulls() default false;

    /**
     * Legacy mode - response format compatible with old systems.
     * <p>Returns simplified response in V1 format.</p>
     *
     * @return true if legacy format should be used, default false
     */
    boolean legacyMode() default false;

    /**
     * Wrapping strategy.
     *
     * @return wrap strategy, default ALWAYS
     */
    WrapStrategy wrapStrategy() default WrapStrategy.ALWAYS;

    /**
     * Custom headers to add to response.
     * <p>Format: "Header-Name:Header-Value"</p>
     *
     * @return custom header array
     */
    String[] customHeaders() default {};

    /**
     * Custom key-value pairs to add to metadata field.
     * <p>Format: "key:value"</p>
     *
     * @return custom metadata array
     */
    String[] customMetadata() default {};

    /**
     * Response cache duration (seconds).
     * <p>0 or negative value disables cache.</p>
     *
     * @return cache duration in seconds, default 0 (no cache)
     */
    int cacheDuration() default 0;

    /**
     * Should pagination info be automatically added?
     * <p>Valid for methods returning Page, Slice or Collection.</p>
     *
     * @return true if pagination metadata should be added, default true
     */
    boolean includePagination() default true;

    /**
     * Should HATEOAS links be added?
     *
     * @return true if self link should be added, default false
     */
    boolean includeLinks() default false;

    /**
     * Compress response (gzip).
     * <p>Saves bandwidth for large responses.</p>
     *
     * @return true if compression active, default false
     */
    boolean compress() default false;

    /**
     * Minimum compression size (bytes).
     * <p>Responses below this size will not be compressed.</p>
     *
     * @return minimum bytes, default 1024
     */
    int compressionThreshold() default 1024;

    /**
     * Should extra info be added in debug mode?
     * <p>Should only be activated in development environment.</p>
     *
     * @return true if debug info should be added, default false
     */
    boolean debugMode() default false;

    /**
     * Deprecation warning.
     * <p>If endpoint is deprecated, this message is added to response header.</p>
     *
     * @return deprecation message, empty if not deprecated
     */
    String deprecationWarning() default "";

    /**
     * Should rate limit info be added?
     * <p>Adds X-RateLimit-* headers to response.</p>
     *
     * @return true if rate limit info should be added, default false
     */
    boolean includeRateLimitInfo() default false;
}
