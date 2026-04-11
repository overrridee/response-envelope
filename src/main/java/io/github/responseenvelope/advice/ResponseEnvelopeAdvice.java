package io.github.responseenvelope.advice;

import io.github.responseenvelope.annotation.EnvelopeField;
import io.github.responseenvelope.annotation.EnvelopeFields;
import io.github.responseenvelope.annotation.IgnoreEnvelope;
import io.github.responseenvelope.annotation.ResponseEnvelope;
import io.github.responseenvelope.config.EnvelopeProperties;
import io.github.responseenvelope.enums.WrapStrategy;
import io.github.responseenvelope.model.EnvelopeContext;
import io.github.responseenvelope.model.EnvelopeResponse;
import io.github.responseenvelope.util.EnvelopeResponseBuilder;
import io.github.responseenvelope.util.RequestIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collection;

/**
 * ResponseBodyAdvice implementation for wrapping responses.
 *
 * <p>This advice automatically wraps controller method return values with
 * @ResponseEnvelope annotation into EnvelopeResponse format.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "response-envelope", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResponseEnvelopeAdvice implements ResponseBodyAdvice<Object> {

    private final EnvelopeProperties properties;
    private final RequestIdGenerator requestIdGenerator;
    private final EnvelopeResponseBuilder responseBuilder;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!properties.isEnabled()) {
            return false;
        }

        Method method = returnType.getMethod();
        if (method == null) {
            return false;
        }

        // Skip if @IgnoreEnvelope present
        if (method.isAnnotationPresent(IgnoreEnvelope.class)) {
            return false;
        }

        // Check if @ResponseEnvelope present at method or class level
        ResponseEnvelope methodAnnotation = AnnotationUtils.findAnnotation(method, ResponseEnvelope.class);
        if (methodAnnotation != null) {
            return true;
        }

        Class<?> containingClass = returnType.getContainingClass();
        ResponseEnvelope classAnnotation = AnnotationUtils.findAnnotation(containingClass, ResponseEnvelope.class);
        return classAnnotation != null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {

        // Skip if already EnvelopeResponse
        if (body instanceof EnvelopeResponse) {
            return body;
        }

        Method method = returnType.getMethod();
        if (method == null) {
            return body;
        }

        // Get annotation
        ResponseEnvelope config = getResponseEnvelopeAnnotation(method, returnType.getContainingClass());
        if (config == null) {
            return body;
        }

        // Check wrap strategy
        if (!shouldWrap(body, config)) {
            return body;
        }

        // Create context
        EnvelopeContext context = createContext(config, request);

        // Process custom fields
        processCustomFields(method, context);

        // Add response headers
        addResponseHeaders(config, context, response);

        // Build success response
        EnvelopeResponse<Object> envelope = responseBuilder.buildSuccess(body, context);

        log.debug("Response wrapped successfully for request: {}", context.getRequestId());

        return envelope;
    }

    /**
     * Gets the ResponseEnvelope annotation.
     */
    private ResponseEnvelope getResponseEnvelopeAnnotation(Method method, Class<?> targetClass) {
        ResponseEnvelope methodAnnotation = AnnotationUtils.findAnnotation(method, ResponseEnvelope.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotationUtils.findAnnotation(targetClass, ResponseEnvelope.class);
    }

    /**
     * Checks wrap strategy.
     */
    private boolean shouldWrap(Object body, ResponseEnvelope config) {
        WrapStrategy strategy = config.wrapStrategy();

        return switch (strategy) {
            case ALWAYS -> true;
            case NEVER -> false;
            case SUCCESS_ONLY -> body != null;
            case ERROR_ONLY -> false;
            case COLLECTION_ONLY -> body instanceof Collection<?>;
        };
    }

    /**
     * Creates context.
     */
    private EnvelopeContext createContext(ResponseEnvelope config, ServerHttpRequest request) {
        HttpServletRequest servletRequest = null;
        if (request instanceof ServletServerHttpRequest) {
            servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        }

        String requestId = generateRequestId(servletRequest);

        return EnvelopeContext.builder()
                .startTime(getStartTime(servletRequest))
                .requestId(requestId)
                .path(request.getURI().getPath())
                .method(request.getMethod().name())
                .queryString(request.getURI().getQuery())
                .clientIp(servletRequest != null ? getClientIp(servletRequest) : null)
                .userAgent(servletRequest != null ? servletRequest.getHeader("User-Agent") : null)
                .config(config)
                .build();
    }

    /**
     * Gets request start time.
     */
    private Instant getStartTime(HttpServletRequest request) {
        if (request != null) {
            Object startTime = request.getAttribute("envelope.startTime");
            if (startTime instanceof Long) {
                return Instant.ofEpochMilli((Long) startTime);
            }
        }
        return Instant.now();
    }

    /**
     * Generates or extracts request ID from header.
     */
    private String generateRequestId(HttpServletRequest request) {
        if (request != null) {
            String headerName = properties.getDefaultConfig().getRequestIdHeader();
            String headerId = request.getHeader(headerName);
            if (headerId != null && !headerId.isEmpty()) {
                return headerId;
            }
        }
        return requestIdGenerator.generate();
    }

    /**
     * Gets client IP.
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Processes custom fields.
     */
    private void processCustomFields(Method method, EnvelopeContext context) {
        EnvelopeField field = method.getAnnotation(EnvelopeField.class);
        if (field != null) {
            context.addMetadata(field.name(), field.value());
        }

        EnvelopeFields fields = method.getAnnotation(EnvelopeFields.class);
        if (fields != null) {
            for (EnvelopeField f : fields.value()) {
                context.addMetadata(f.name(), f.value());
            }
        }
    }

    /**
     * Adds response headers.
     */
    private void addResponseHeaders(ResponseEnvelope config, EnvelopeContext context, ServerHttpResponse response) {
        // Request ID header
        if (properties.getDefaultConfig().isPropagateRequestId()) {
            response.getHeaders().set(properties.getDefaultConfig().getRequestIdHeader(), context.getRequestId());
        }

        // Custom headers
        for (String header : config.customHeaders()) {
            String[] parts = header.split(":", 2);
            if (parts.length == 2) {
                response.getHeaders().set(parts[0].trim(), parts[1].trim());
            }
        }

        // Deprecation warning
        if (!config.deprecationWarning().isEmpty()) {
            response.getHeaders().set("Deprecation", "true");
            response.getHeaders().set("X-Deprecation-Warning", config.deprecationWarning());
        }

        // Cache headers
        if (config.cacheDuration() > 0) {
            response.getHeaders().setCacheControl("max-age=" + config.cacheDuration());
        }
    }
}
