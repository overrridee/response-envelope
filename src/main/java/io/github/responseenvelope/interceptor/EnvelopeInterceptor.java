package io.github.responseenvelope.interceptor;

import io.github.responseenvelope.config.EnvelopeProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * HTTP request interceptor for envelope processing.
 *
 * <p>Adds request start time and other metadata to request attributes.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "response-envelope", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EnvelopeInterceptor implements HandlerInterceptor {

    public static final String START_TIME_ATTRIBUTE = "envelope.startTime";
    public static final String REQUEST_ID_ATTRIBUTE = "envelope.requestId";

    private final EnvelopeProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isEnabled()) {
            return true;
        }

        // Record request start time
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        log.trace("Envelope interceptor preHandle: {} {}", request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // Post-processing if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!properties.isEnabled()) {
            return;
        }

        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.trace("Envelope interceptor afterCompletion: {} {} - {}ms",
                    request.getMethod(), request.getRequestURI(), duration);
        }
    }
}
