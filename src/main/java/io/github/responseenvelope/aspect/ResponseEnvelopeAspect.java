package io.github.responseenvelope.aspect;

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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collection;

/**
 * AOP Aspect that processes ResponseEnvelope annotation.
 *
 * <p>This aspect intercepts controller method return values and
 * wraps them in {@link EnvelopeResponse} format.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Automatic timestamp insertion</li>
 *   <li>Request ID generation and propagation</li>
 *   <li>Processing time measurement</li>
 *   <li>Error handling integration</li>
 *   <li>Metadata enrichment</li>
 *   <li>Pagination support</li>
 *   <li>HATEOAS link support</li>
 * </ul>
 *
 * @author aedemirsen
 * @version 1.0.0
 * @see ResponseEnvelope
 * @see EnvelopeResponse
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
@Order(1)
@Deprecated
public class ResponseEnvelopeAspect {

    private final RequestIdGenerator requestIdGenerator;
    private final EnvelopeProperties properties;
    private final EnvelopeResponseBuilder responseBuilder;

    /**
     * Methods with method-level @ResponseEnvelope annotation.
     */
    @Pointcut("@annotation(io.github.responseenvelope.annotation.ResponseEnvelope)")
    public void methodLevelEnvelope() {
    }

    /**
     * Methods of classes with class-level @ResponseEnvelope annotation.
     */
    @Pointcut("@within(io.github.responseenvelope.annotation.ResponseEnvelope)")
    public void classLevelEnvelope() {
    }

    /**
     * Methods with @IgnoreEnvelope annotation.
     */
    @Pointcut("@annotation(io.github.responseenvelope.annotation.IgnoreEnvelope)")
    public void ignoredMethods() {
    }

    /**
     * Main pointcut: Methods with @ResponseEnvelope at method or class level,
     * but without @IgnoreEnvelope.
     */
    @Around("(methodLevelEnvelope() || classLevelEnvelope()) && !ignoredMethods()")
    public Object wrapResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // If envelope is disabled, execute directly
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get annotation (from method or class level)
        ResponseEnvelope config = getResponseEnvelopeAnnotation(method, joinPoint.getTarget().getClass());

        if (config == null) {
            return joinPoint.proceed();
        }

        // Create context
        EnvelopeContext context = createContext(config);

        log.debug("ResponseEnvelope wrapping started for request: {}", context.getRequestId());

        try {
            // Execute target method
            Object result = joinPoint.proceed();

            // Wrap strategy check
            if (!shouldWrap(result, config)) {
                return result;
            }

            // If already ResponseEntity, extract its body
            Object data = extractData(result);

            // Process custom fields
            processCustomFields(method, context);

            // Create success response
            EnvelopeResponse<Object> envelope = responseBuilder.buildSuccess(data, context);

            log.debug("ResponseEnvelope wrapping completed successfully: {}", context.getRequestId());

            // Add response headers
            HttpHeaders headers = buildResponseHeaders(config, context);

            return ResponseEntity
                    .status(config.httpStatus())
                    .headers(headers)
                    .body(envelope);

        } catch (Exception e) {
            log.error("ResponseEnvelope wrapping failed for request: {}", context.getRequestId(), e);
            throw e;
        }
    }

    /**
     * Gets ResponseEnvelope annotation (from method or class level).
     */
    private ResponseEnvelope getResponseEnvelopeAnnotation(Method method, Class<?> targetClass) {
        // First check method level
        ResponseEnvelope methodAnnotation = AnnotationUtils.findAnnotation(method, ResponseEnvelope.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // Then check class level
        return AnnotationUtils.findAnnotation(targetClass, ResponseEnvelope.class);
    }

    /**
     * Creates request context.
     */
    private EnvelopeContext createContext(ResponseEnvelope config) {
        HttpServletRequest request = getCurrentRequest();

        String requestId = generateRequestId(request);

        return EnvelopeContext.builder()
                .startTime(Instant.now())
                .requestId(requestId)
                .path(request != null ? request.getRequestURI() : "")
                .method(request != null ? request.getMethod() : "")
                .queryString(request != null ? request.getQueryString() : null)
                .clientIp(request != null ? getClientIp(request) : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .config(config)
                .build();
    }

    /**
     * Generates request ID or gets from existing header.
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
     * Gets current HTTP request.
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * Gets current HTTP response.
     */
    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getResponse() : null;
    }

    /**
     * Gets client IP address (even behind proxy).
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Checks whether wrapping should be done according to wrap strategy.
     */
    private boolean shouldWrap(Object result, ResponseEnvelope config) {
        WrapStrategy strategy = config.wrapStrategy();

        return switch (strategy) {
            case ALWAYS -> true;
            case NEVER -> false;
            case SUCCESS_ONLY -> result != null;
            case ERROR_ONLY -> false; // Errors are handled in exception handler
            case COLLECTION_ONLY -> result instanceof Collection<?> ||
                    (result instanceof ResponseEntity<?> re && re.getBody() instanceof Collection<?>);
        };
    }

    /**
     * Extracts data from ResponseEntity.
     */
    private Object extractData(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getBody();
        }
        return result;
    }

    /**
     * Processes custom fields.
     */
    private void processCustomFields(Method method, EnvelopeContext context) {
        // Single annotation
        EnvelopeField field = method.getAnnotation(EnvelopeField.class);
        if (field != null) {
            processEnvelopeField(field, context);
        }

        // Multiple annotations
        EnvelopeFields fields = method.getAnnotation(EnvelopeFields.class);
        if (fields != null) {
            for (EnvelopeField f : fields.value()) {
                processEnvelopeField(f, context);
            }
        }
    }

    /**
     * Processes a single EnvelopeField.
     */
    private void processEnvelopeField(EnvelopeField field, EnvelopeContext context) {
        String value = field.value();

        // If not SpEL expression, use directly
        if (!value.startsWith("#{")) {
            context.addMetadata(field.name(), value);
            return;
        }

        // If SpEL expression, evaluate (simple implementation)
        // In real implementation, SpEL parser can be used
        context.addMetadata(field.name(), value);
    }

    /**
     * Creates response headers.
     */
    private HttpHeaders buildResponseHeaders(ResponseEnvelope config, EnvelopeContext context) {
        HttpHeaders headers = new HttpHeaders();

        // Request ID header
        if (properties.getDefaultConfig().isPropagateRequestId()) {
            headers.set(properties.getDefaultConfig().getRequestIdHeader(), context.getRequestId());
        }

        // Custom headers
        for (String header : config.customHeaders()) {
            String[] parts = header.split(":", 2);
            if (parts.length == 2) {
                headers.set(parts[0].trim(), parts[1].trim());
            }
        }

        // Deprecation warning
        if (!config.deprecationWarning().isEmpty()) {
            headers.set("Deprecation", "true");
            headers.set("X-Deprecation-Warning", config.deprecationWarning());
        }

        // Cache headers
        if (config.cacheDuration() > 0) {
            headers.setCacheControl("max-age=" + config.cacheDuration());
        }

        return headers;
    }
}
