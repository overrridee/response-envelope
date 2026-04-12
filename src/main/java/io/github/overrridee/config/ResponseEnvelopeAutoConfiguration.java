package io.github.overrridee.config;

import io.github.overrridee.advice.ResponseEnvelopeAdvice;
import io.github.overrridee.filter.RequestIdFilter;
import io.github.overrridee.handler.GlobalExceptionHandler;
import io.github.overrridee.metrics.EnvelopeMetrics;
import io.github.overrridee.util.EnvelopeResponseBuilder;
import io.github.overrridee.util.RequestIdGenerator;
import io.github.overrridee.util.TimestampFormatter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Response Envelope Auto Configuration.
 *
 * <p>Spring Boot auto-configuration class. Automatically creates required beans.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(EnvelopeProperties.class)
@EnableAspectJAutoProxy
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "response-envelope", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "io.github.overrridee")
public class ResponseEnvelopeAutoConfiguration {

    /**
     * Request ID generator bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestIdGenerator requestIdGenerator() {
        return new RequestIdGenerator();
    }

    /**
     * Timestamp formatter bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public TimestampFormatter timestampFormatter(EnvelopeProperties properties) {
        return new TimestampFormatter(properties);
    }

    /**
     * Envelope field config bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EnvelopeFieldConfig envelopeFieldConfig() {
        return new EnvelopeFieldConfig();
    }

    /**
     * Envelope response builder bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EnvelopeResponseBuilder envelopeResponseBuilder(
            TimestampFormatter timestampFormatter,
            EnvelopeProperties properties,
            EnvelopeFieldConfig fieldConfig) {
        return new EnvelopeResponseBuilder(timestampFormatter, properties, fieldConfig);
    }

    /**
     * Response envelope advice bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ResponseEnvelopeAdvice responseEnvelopeAdvice(
            EnvelopeProperties properties,
            RequestIdGenerator requestIdGenerator,
            EnvelopeResponseBuilder responseBuilder) {
        return new ResponseEnvelopeAdvice(properties, requestIdGenerator, responseBuilder);
    }

    /**
     * Global exception handler bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(
            EnvelopeProperties properties,
            RequestIdGenerator requestIdGenerator,
            TimestampFormatter timestampFormatter) {
        return new GlobalExceptionHandler(properties, requestIdGenerator, timestampFormatter);
    }

    /**
     * Request ID filter bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestIdFilter requestIdFilter(
            EnvelopeProperties properties,
            RequestIdGenerator requestIdGenerator) {
        return new RequestIdFilter(properties, requestIdGenerator);
    }

    /**
     * Metrics configuration (optional).
     */
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "response-envelope.metrics", name = "enabled", havingValue = "true")
    static class MetricsConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EnvelopeMetrics envelopeMetrics(
                MeterRegistry meterRegistry,
                EnvelopeProperties properties) {
            return new EnvelopeMetrics(meterRegistry, properties);
        }
    }
}
