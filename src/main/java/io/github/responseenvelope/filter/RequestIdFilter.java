package io.github.overrridee.filter;

import io.github.overrridee.config.EnvelopeProperties;
import io.github.overrridee.util.RequestIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request ID filter.
 *
 * <p>Generates unique ID for each request or extracts from existing header.
 * Adds to MDC for logging and writes to response header.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Component("responseEnvelopeRequestIdFilter")
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "response-envelope", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "response-envelope.request-id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String MDC_REQUEST_ID_KEY = "requestId";

    private final EnvelopeProperties properties;
    private final RequestIdGenerator requestIdGenerator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerName = properties.getDefaultConfig().getRequestIdHeader();
        String requestId = request.getHeader(headerName);

        // Generate request ID if not present
        if (requestId == null || requestId.isEmpty()) {
            requestId = requestIdGenerator.generate();
        }

        // Add to MDC for logging
        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        try {
            // Add header to request (via wrapper)
            HttpServletRequest wrappedRequest = new RequestIdRequestWrapper(request, headerName, requestId);

            // Add to response header
            if (properties.getDefaultConfig().isPropagateRequestId()) {
                response.setHeader(headerName, requestId);
            }

            log.debug("Request started: {} {} [{}]",
                    request.getMethod(), request.getRequestURI(), requestId);

            filterChain.doFilter(wrappedRequest, response);

            log.debug("Request completed: {} {} [{}] - Status: {}",
                    request.getMethod(), request.getRequestURI(), requestId, response.getStatus());

        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    /**
     * Request wrapper - for adding headers.
     */
    private static class RequestIdRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> customHeaders;

        public RequestIdRequestWrapper(HttpServletRequest request, String headerName, String requestId) {
            super(request);
            this.customHeaders = new HashMap<>();
            this.customHeaders.put(headerName.toLowerCase(), requestId);
        }

        @Override
        public String getHeader(String name) {
            String header = customHeaders.get(name.toLowerCase());
            if (header != null) {
                return header;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String header = customHeaders.get(name.toLowerCase());
            if (header != null) {
                return Collections.enumeration(List.of(header));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.addAll(customHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
