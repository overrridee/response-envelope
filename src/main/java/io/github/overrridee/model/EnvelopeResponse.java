package io.github.overrridee.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Map;

/**
 * Standard API response envelope.
 *
 * <p>Wraps all API responses in consistent format.</p>
 *
 * <h2>Success Response Example:</h2>
 * <pre>{@code
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "Operation completed successfully",
 *   "timestamp": "2024-04-10T14:30:00Z",
 *   "requestId": "req_abc123",
 *   "path": "/api/users/42",
 *   "method": "GET",
 *   "duration": 145,
 *   "apiVersion": "v1"
 * }
 * }</pre>
 *
 * <h2>Error Response Example:</h2>
 * <pre>{@code
 * {
 *   "success": false,
 *   "data": null,
 *   "message": "User not found",
 *   "timestamp": "2024-04-10T14:30:00Z",
 *   "requestId": "req_xyz789",
 *   "path": "/api/users/999",
 *   "method": "GET",
 *   "duration": 45,
 *   "apiVersion": "v1",
 *   "errors": {
 *     "code": "ERR_BIZ_002",
 *     "message": "Entity Not Found",
 *     "details": "User with id 999 does not exist",
 *     "fieldErrors": []
 *   }
 * }
 * }</pre>
 *
 * @param <T> Data type
 * @author aedemirsen
 * @version 1.0.0
 */
@Getter
@Builder(toBuilder = true)
@JsonPropertyOrder({
        "success", "data", "message", "timestamp",
        "requestId", "path", "method", "duration",
        "apiVersion", "pagination", "links", "metadata", "errors"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvelopeResponse<T> {

    /**
     * Is operation successful?
     */
    @JsonProperty("success")
    private final boolean success;

    /**
     * Response data.
     * <p>Actual data for successful operations, null on error.</p>
     */
    @JsonProperty("data")
    private final T data;

    /**
     * Message (optional).
     * <p>Success or error message.</p>
     */
    @JsonProperty("message")
    private final String message;

    /**
     * Timestamp.
     * <p>Format: ISO-8601 or epoch (depending on configuration)</p>
     */
    @JsonProperty("timestamp")
    private final String timestamp;

    /**
     * Request tracking ID.
     * <p>Used for distributed tracing and log correlation.</p>
     */
    @JsonProperty("requestId")
    private final String requestId;

    /**
     * Request path.
     */
    @JsonProperty("path")
    private final String path;

    /**
     * HTTP method.
     */
    @JsonProperty("method")
    private final String method;

    /**
     * Processing time (ms).
     */
    @JsonProperty("duration")
    private final Long duration;

    /**
     * API versiyonu.
     */
    @JsonProperty("apiVersion")
    private final String apiVersion;

    /**
     * Pagination info (for collection responses).
     */
    @JsonProperty("pagination")
    private final PaginationInfo pagination;

    /**
     * HATEOAS linkleri.
     */
    @JsonProperty("links")
    private final Links links;

    /**
     * Custom metadata.
     */
    @Singular("metadata")
    private final Map<String, Object> metadata;

    /**
     * Error details (only in error cases).
     */
    @JsonProperty("errors")
    private final ErrorDetails errors;

    /**
     * Adds custom metadata to JSON root.
     */
    @JsonAnyGetter
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Creates success response.
     *
     * @param data response data
     * @param <T>  data type
     * @return EnvelopeResponse
     */
    public static <T> EnvelopeResponse<T> success(T data) {
        return EnvelopeResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates success response (with message).
     *
     * @param data    response data
     * @param message success message
     * @param <T>     data type
     * @return EnvelopeResponse
     */
    public static <T> EnvelopeResponse<T> success(T data, String message) {
        return EnvelopeResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Creates error response.
     *
     * @param errorDetails error details
     * @param <T>          data type
     * @return EnvelopeResponse
     */
    public static <T> EnvelopeResponse<T> error(ErrorDetails errorDetails) {
        return EnvelopeResponse.<T>builder()
                .success(false)
                .errors(errorDetails)
                .message(errorDetails.getMessage())
                .build();
    }

    /**
     * Creates error response (simple).
     *
     * @param code    error code
     * @param message error message
     * @param <T>     data type
     * @return EnvelopeResponse
     */
    public static <T> EnvelopeResponse<T> error(String code, String message) {
        return EnvelopeResponse.<T>builder()
                .success(false)
                .errors(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .build())
                .message(message)
                .build();
    }

    /**
     * Pagination info.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationInfo {

        /**
         * Current page (0-indexed).
         */
        @JsonProperty("page")
        private final Integer page;

        /**
         * Page size.
         */
        @JsonProperty("size")
        private final Integer size;

        /**
         * Total element count.
         */
        @JsonProperty("totalElements")
        private final Long totalElements;

        /**
         * Total page count.
         */
        @JsonProperty("totalPages")
        private final Integer totalPages;

        /**
         * Is first page?
         */
        @JsonProperty("first")
        private final Boolean first;

        /**
         * Is last page?
         */
        @JsonProperty("last")
        private final Boolean last;

        /**
         * Number of elements in current page.
         */
        @JsonProperty("numberOfElements")
        private final Integer numberOfElements;

        /**
         * Is empty?
         */
        @JsonProperty("empty")
        private final Boolean empty;
    }

    /**
     * HATEOAS links.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Links {

        /**
         * Self link.
         */
        @JsonProperty("self")
        private final String self;

        /**
         * First page link.
         */
        @JsonProperty("first")
        private final String first;

        /**
         * Previous page link.
         */
        @JsonProperty("prev")
        private final String prev;

        /**
         * Next page link.
         */
        @JsonProperty("next")
        private final String next;

        /**
         * Last page link.
         */
        @JsonProperty("last")
        private final String last;

        /**
         * Custom links.
         */
        @Singular("link")
        @JsonProperty("related")
        private final Map<String, String> related;
    }

    /**
     * Error details.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {

        /**
         * Error code.
         */
        @JsonProperty("code")
        private final String code;

        /**
         * Error message.
         */
        @JsonProperty("message")
        private final String message;

        /**
         * Detailed description.
         */
        @JsonProperty("details")
        private final String details;

        /**
         * Exception class name (in debug mode).
         */
        @JsonProperty("exception")
        private final String exception;

        /**
         * Stack trace (in debug mode).
         */
        @JsonProperty("stackTrace")
        private final String stackTrace;

        /**
         * Field-level validation errors.
         */
        @JsonProperty("fieldErrors")
        @Singular
        private final List<FieldError> fieldErrors;

        /**
         * Error source (which service).
         */
        @JsonProperty("source")
        private final String source;

        /**
         * Documentation link.
         */
        @JsonProperty("documentationUrl")
        private final String documentationUrl;
    }

    /**
     * Field-level validation error.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {

        /**
         * Field name.
         */
        @JsonProperty("field")
        private final String field;

        /**
         * Rejected value.
         */
        @JsonProperty("rejectedValue")
        private final Object rejectedValue;

        /**
         * Error message.
         */
        @JsonProperty("message")
        private final String message;

        /**
         * Error code.
         */
        @JsonProperty("code")
        private final String code;
    }
}
