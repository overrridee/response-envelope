package io.github.overrridee.metrics;

import io.github.overrridee.config.EnvelopeProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Response Envelope metrics.
 *
 * <p>Integrates with Micrometer. Supports Prometheus, Datadog, etc.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Component
@Slf4j
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "response-envelope.metrics", name = "enabled", havingValue = "true")
public class EnvelopeMetrics {

    private final MeterRegistry meterRegistry;
    private final EnvelopeProperties properties;

    private final Map<String, Timer> timerCache = new ConcurrentHashMap<>();
    private final Map<String, Counter> counterCache = new ConcurrentHashMap<>();

    public EnvelopeMetrics(MeterRegistry meterRegistry, EnvelopeProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
        initializeMetrics();
    }

    /**
     * Initializes metrics.
     */
    private void initializeMetrics() {
        String prefix = properties.getMetrics().getPrefix();
        List<Tag> tags = mapToTags(properties.getMetrics().getTags());

        // Success counter
        Counter.builder(prefix + "_success_total")
                .description("Total successful responses")
                .tags(tags)
                .register(meterRegistry);

        // Error counter
        Counter.builder(prefix + "_error_total")
                .description("Total error responses")
                .tags(tags)
                .register(meterRegistry);

        log.info("EnvelopeMetrics initialized with prefix: {}", prefix);
    }

    /**
     * Converts Map to Tag list.
     */
    private List<Tag> mapToTags(Map<String, String> tagMap) {
        List<Tag> tags = new ArrayList<>();
        if (tagMap != null) {
            tagMap.forEach((key, value) -> tags.add(Tag.of(key, value)));
        }
        return tags;
    }

    /**
     * Records successful response.
     *
     * @param path     request path
     * @param method   HTTP method
     * @param duration processing time (ms)
     * @param version  API version
     */
    public void recordSuccess(String path, String method, long duration, String version) {
        String prefix = properties.getMetrics().getPrefix();

        // Timer
        Timer timer = getOrCreateTimer(prefix + "_duration_seconds", path, method, version);
        timer.record(duration, TimeUnit.MILLISECONDS);

        // Counter
        Counter counter = getOrCreateCounter(prefix + "_success_total", path, method, version);
        counter.increment();

        // SLO check
        long sloDuration = properties.getMetrics().getSloDuration();
        if (duration > sloDuration) {
            Counter sloViolation = getOrCreateCounter(prefix + "_slo_violation_total", path, method, version);
            sloViolation.increment();
            log.warn("SLO violation: {} {} took {}ms (SLO: {}ms)", method, path, duration, sloDuration);
        }
    }

    /**
     * Records error response.
     *
     * @param path      request path
     * @param method    HTTP method
     * @param duration  processing time (ms)
     * @param errorCode error code
     * @param version   API version
     */
    public void recordError(String path, String method, long duration, String errorCode, String version) {
        String prefix = properties.getMetrics().getPrefix();

        // Timer
        Timer timer = getOrCreateTimer(prefix + "_duration_seconds", path, method, version);
        timer.record(duration, TimeUnit.MILLISECONDS);

        // Error counter
        Counter counter = meterRegistry.counter(
                prefix + "_error_total",
                "path", normalizePath(path),
                "method", method,
                "error_code", errorCode,
                "version", version
        );
        counter.increment();
    }

    /**
     * Gets or creates Timer.
     */
    private Timer getOrCreateTimer(String name, String path, String method, String version) {
        String key = name + "_" + path + "_" + method + "_" + version;
        return timerCache.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .description("Response duration")
                        .tags("path", normalizePath(path), "method", method, "version", version)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .publishPercentileHistogram()
                        .serviceLevelObjectives(Duration.ofMillis(properties.getMetrics().getSloDuration()))
                        .register(meterRegistry)
        );
    }

    /**
     * Gets or creates Counter.
     */
    private Counter getOrCreateCounter(String name, String path, String method, String version) {
        String key = name + "_" + path + "_" + method + "_" + version;
        return counterCache.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .tags("path", normalizePath(path), "method", method, "version", version)
                        .register(meterRegistry)
        );
    }

    /**
     * Normalizes path (cleans path variables).
     */
    private String normalizePath(String path) {
        if (path == null) {
            return "unknown";
        }
        // /users/123 -> /users/{id}
        return path.replaceAll("/\\d+", "/{id}")
                .replaceAll("/[a-f0-9-]{36}", "/{uuid}");
    }
}
