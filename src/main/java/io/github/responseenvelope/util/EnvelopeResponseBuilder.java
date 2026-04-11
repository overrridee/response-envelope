package io.github.overrridee.util;

import io.github.overrridee.annotation.ResponseEnvelope;
import io.github.overrridee.config.EnvelopeFieldConfig;
import io.github.overrridee.config.EnvelopeProperties;
import io.github.overrridee.model.EnvelopeContext;
import io.github.overrridee.model.EnvelopeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * EnvelopeResponse builder utility.
 *
 * <p>Creates EnvelopeResponse based on context and configuration.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class EnvelopeResponseBuilder {

    private final TimestampFormatter timestampFormatter;
    private final EnvelopeProperties properties;
    private final EnvelopeFieldConfig fieldConfig;

    /**
     * Creates success response.
     *
     * @param data    response data
     * @param context request context
     * @param <T>     data type
     * @return EnvelopeResponse
     */
    public <T> EnvelopeResponse<T> buildSuccess(T data, EnvelopeContext context) {
        ResponseEnvelope config = context.getConfig();

        EnvelopeResponse.EnvelopeResponseBuilder<T> builder = EnvelopeResponse.<T>builder()
                .success(fieldConfig.isFieldEnabled("success") ? true : null)
                .data(fieldConfig.isFieldEnabled("data") ? data : null);

        // Timestamp
        if (config.includeTimestamp() && fieldConfig.isFieldEnabled("timestamp")) {
            builder.timestamp(timestampFormatter.format(Instant.now(), config.timestampFormat()));
        }

        // Request ID
        if (config.includeRequestId() && fieldConfig.isFieldEnabled("requestId")) {
            builder.requestId(context.getRequestId());
        }

        // Path
        if (config.includePath() && fieldConfig.isFieldEnabled("path")) {
            builder.path(context.getPath());
        }

        // Method
        if (config.includeMethod() && fieldConfig.isFieldEnabled("method")) {
            builder.method(context.getMethod());
        }

        // Duration
        if (config.includeDuration() && fieldConfig.isFieldEnabled("duration")) {
            builder.duration(context.calculateDuration());
        }

        // API Version
        if (config.includeApiVersion() && fieldConfig.isFieldEnabled("apiVersion")) {
            builder.apiVersion(config.version());
        }

        // Success message
        if (!config.successMessage().isEmpty()) {
            builder.message(config.successMessage());
        }

        // Pagination
        if (config.includePagination()) {
            EnvelopeResponse.PaginationInfo pagination = extractPagination(data);
            if (pagination != null) {
                builder.pagination(pagination);
            }
        }

        // Links
        if (config.includeLinks()) {
            builder.links(buildLinks(context, data));
        }

        // Custom metadata
        Map<String, Object> metadata = buildMetadata(config, context);
        if (!metadata.isEmpty()) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                builder.metadata(entry.getKey(), entry.getValue());
            }
        }
        
        // Global custom fields from field config
        if (fieldConfig.isEnabled()) {
            for (Map.Entry<String, Object> entry : fieldConfig.getCustomFields().entrySet()) {
                builder.metadata(entry.getKey(), entry.getValue());
            }
            
            // Conditional fields - onSuccess
            for (Map.Entry<String, Object> entry : fieldConfig.getConditionalFields().getOnSuccess().entrySet()) {
                builder.metadata(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    /**
     * Creates error response.
     *
     * @param errorDetails error details
     * @param context      request context
     * @param <T>          data type
     * @return EnvelopeResponse
     */
    public <T> EnvelopeResponse<T> buildError(
            EnvelopeResponse.ErrorDetails errorDetails,
            EnvelopeContext context) {

        ResponseEnvelope config = context.getConfig();

        EnvelopeResponse.EnvelopeResponseBuilder<T> builder = EnvelopeResponse.<T>builder()
                .success(false)
                .errors(errorDetails)
                .message(errorDetails.getMessage());

        // Timestamp
        if (config.includeTimestamp()) {
            builder.timestamp(timestampFormatter.format(Instant.now(), config.timestampFormat()));
        }

        // Request ID
        if (config.includeRequestId()) {
            builder.requestId(context.getRequestId());
        }

        // Path
        if (config.includePath()) {
            builder.path(context.getPath());
        }

        // Method
        if (config.includeMethod()) {
            builder.method(context.getMethod());
        }

        // Duration
        if (config.includeDuration()) {
            builder.duration(context.calculateDuration());
        }

        // API Version
        if (config.includeApiVersion()) {
            builder.apiVersion(config.version());
        }

        return builder.build();
    }

    /**
     * Extracts pagination info.
     *
     * @param data response data
     * @return PaginationInfo or null
     */
    @SuppressWarnings("rawtypes")
    private EnvelopeResponse.PaginationInfo extractPagination(Object data) {
        if (data instanceof Page<?> page) {
            return EnvelopeResponse.PaginationInfo.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .numberOfElements(page.getNumberOfElements())
                    .empty(page.isEmpty())
                    .build();
        }

        if (data instanceof Slice<?> slice) {
            return EnvelopeResponse.PaginationInfo.builder()
                    .page(slice.getNumber())
                    .size(slice.getSize())
                    .first(slice.isFirst())
                    .last(slice.isLast())
                    .numberOfElements(slice.getNumberOfElements())
                    .empty(slice.isEmpty())
                    .build();
        }

        if (data instanceof Collection<?> collection) {
            return EnvelopeResponse.PaginationInfo.builder()
                    .numberOfElements(collection.size())
                    .empty(collection.isEmpty())
                    .build();
        }

        return null;
    }

    /**
     * Creates HATEOAS links.
     *
     * @param context request context
     * @param data    response data
     * @return Links
     */
    private EnvelopeResponse.Links buildLinks(EnvelopeContext context, Object data) {
        EnvelopeResponse.Links.LinksBuilder builder = EnvelopeResponse.Links.builder()
                .self(context.getFullPath());

        if (data instanceof Page<?> page) {
            String basePath = context.getPath();

            if (!page.isFirst()) {
                builder.first(basePath + "?page=0&size=" + page.getSize());
                builder.prev(basePath + "?page=" + (page.getNumber() - 1) + "&size=" + page.getSize());
            }

            if (!page.isLast()) {
                builder.next(basePath + "?page=" + (page.getNumber() + 1) + "&size=" + page.getSize());
                builder.last(basePath + "?page=" + (page.getTotalPages() - 1) + "&size=" + page.getSize());
            }
        }

        return builder.build();
    }

    /**
     * Creates custom metadata.
     *
     * @param config  annotation config
     * @param context request context
     * @return metadata map
     */
    private Map<String, Object> buildMetadata(ResponseEnvelope config, EnvelopeContext context) {
        Map<String, Object> metadata = new HashMap<>(context.getCustomMetadata());

        // Annotation'dan gelen custom metadata
        for (String meta : config.customMetadata()) {
            String[] parts = meta.split(":", 2);
            if (parts.length == 2) {
                metadata.put(parts[0].trim(), parts[1].trim());
            }
        }

        // Debug mode
        if (config.debugMode()) {
            metadata.put("debug", Map.of(
                    "clientIp", context.getClientIp() != null ? context.getClientIp() : "unknown",
                    "userAgent", context.getUserAgent() != null ? context.getUserAgent() : "unknown",
                    "queryString", context.getQueryString() != null ? context.getQueryString() : ""
            ));
        }

        return metadata;
    }
}
