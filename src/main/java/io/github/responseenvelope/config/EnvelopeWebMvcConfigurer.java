package io.github.responseenvelope.config;

import io.github.responseenvelope.interceptor.EnvelopeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc configuration for Response Envelope.
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "response-envelope", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EnvelopeWebMvcConfigurer implements WebMvcConfigurer {

    private final EnvelopeInterceptor envelopeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(envelopeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/error"
                );
    }
}
