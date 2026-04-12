package io.github.overrridee;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.overrridee.model.EnvelopeResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EnvelopeResponse unit tests.
 */
class EnvelopeResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateSuccessResponse() {
        EnvelopeResponse<String> response = EnvelopeResponse.success("test data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldCreateSuccessResponseWithMessage() {
        EnvelopeResponse<String> response = EnvelopeResponse.success("test data", "Operation successful");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getMessage()).isEqualTo("Operation successful");
    }

    @Test
    void shouldCreateErrorResponse() {
        EnvelopeResponse.ErrorDetails errorDetails = EnvelopeResponse.ErrorDetails.builder()
                .code("ERR_001")
                .message("Something went wrong")
                .details("Detailed error description")
                .build();

        EnvelopeResponse<Void> response = EnvelopeResponse.error(errorDetails);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().getCode()).isEqualTo("ERR_001");
        assertThat(response.getErrors().getMessage()).isEqualTo("Something went wrong");
    }

    @Test
    void shouldCreateErrorResponseWithCodeAndMessage() {
        EnvelopeResponse<Void> response = EnvelopeResponse.error("ERR_002", "Error message");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors().getCode()).isEqualTo("ERR_002");
        assertThat(response.getErrors().getMessage()).isEqualTo("Error message");
    }

    @Test
    void shouldBuildCompleteResponse() {
        EnvelopeResponse<Map<String, String>> response = EnvelopeResponse.<Map<String, String>>builder()
                .success(true)
                .data(Map.of("key", "value"))
                .message("Success")
                .timestamp("2024-04-10T14:30:00Z")
                .requestId("req_123")
                .path("/api/test")
                .method("GET")
                .duration(100L)
                .apiVersion("v1")
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("key", "value");
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getTimestamp()).isEqualTo("2024-04-10T14:30:00Z");
        assertThat(response.getRequestId()).isEqualTo("req_123");
        assertThat(response.getPath()).isEqualTo("/api/test");
        assertThat(response.getMethod()).isEqualTo("GET");
        assertThat(response.getDuration()).isEqualTo(100L);
        assertThat(response.getApiVersion()).isEqualTo("v1");
    }

    @Test
    void shouldBuildPaginationInfo() {
        EnvelopeResponse.PaginationInfo pagination = EnvelopeResponse.PaginationInfo.builder()
                .page(0)
                .size(20)
                .totalElements(100L)
                .totalPages(5)
                .first(true)
                .last(false)
                .numberOfElements(20)
                .empty(false)
                .build();

        assertThat(pagination.getPage()).isEqualTo(0);
        assertThat(pagination.getSize()).isEqualTo(20);
        assertThat(pagination.getTotalElements()).isEqualTo(100L);
        assertThat(pagination.getTotalPages()).isEqualTo(5);
        assertThat(pagination.getFirst()).isTrue();
        assertThat(pagination.getLast()).isFalse();
    }

    @Test
    void shouldBuildLinks() {
        EnvelopeResponse.Links links = EnvelopeResponse.Links.builder()
                .self("/api/users?page=1")
                .first("/api/users?page=0")
                .prev("/api/users?page=0")
                .next("/api/users?page=2")
                .last("/api/users?page=5")
                .link("related", "/api/users/1/orders")
                .build();

        assertThat(links.getSelf()).isEqualTo("/api/users?page=1");
        assertThat(links.getFirst()).isEqualTo("/api/users?page=0");
        assertThat(links.getPrev()).isEqualTo("/api/users?page=0");
        assertThat(links.getNext()).isEqualTo("/api/users?page=2");
        assertThat(links.getLast()).isEqualTo("/api/users?page=5");
        assertThat(links.getRelated()).containsEntry("related", "/api/users/1/orders");
    }

    @Test
    void shouldBuildFieldErrors() {
        EnvelopeResponse.FieldError fieldError = EnvelopeResponse.FieldError.builder()
                .field("email")
                .message("Invalid email format")
                .rejectedValue("invalid-email")
                .code("Email")
                .build();

        assertThat(fieldError.getField()).isEqualTo("email");
        assertThat(fieldError.getMessage()).isEqualTo("Invalid email format");
        assertThat(fieldError.getRejectedValue()).isEqualTo("invalid-email");
        assertThat(fieldError.getCode()).isEqualTo("Email");
    }

    @Test
    void shouldBuildErrorDetailsWithFieldErrors() {
        EnvelopeResponse.ErrorDetails errorDetails = EnvelopeResponse.ErrorDetails.builder()
                .code("ERR_VAL_001")
                .message("Validation failed")
                .fieldError(EnvelopeResponse.FieldError.builder()
                        .field("name")
                        .message("Name is required")
                        .build())
                .fieldError(EnvelopeResponse.FieldError.builder()
                        .field("email")
                        .message("Invalid email")
                        .build())
                .build();

        assertThat(errorDetails.getFieldErrors()).hasSize(2);
        assertThat(errorDetails.getFieldErrors().get(0).getField()).isEqualTo("name");
        assertThat(errorDetails.getFieldErrors().get(1).getField()).isEqualTo("email");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        EnvelopeResponse<String> response = EnvelopeResponse.<String>builder()
                .success(true)
                .data("test")
                .timestamp("2024-04-10T14:30:00Z")
                .requestId("req_123")
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"data\":\"test\"");
        assertThat(json).contains("\"timestamp\":\"2024-04-10T14:30:00Z\"");
        assertThat(json).contains("\"requestId\":\"req_123\"");
    }

    @Test
    void shouldExcludeNullFieldsFromJson() throws Exception {
        EnvelopeResponse<String> response = EnvelopeResponse.<String>builder()
                .success(true)
                .data("test")
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).doesNotContain("\"message\"");
        assertThat(json).doesNotContain("\"timestamp\"");
        assertThat(json).doesNotContain("\"errors\"");
    }

    @Test
    void shouldUseToBuilder() {
        EnvelopeResponse<String> original = EnvelopeResponse.<String>builder()
                .success(true)
                .data("original")
                .build();

        EnvelopeResponse<String> modified = original.toBuilder()
                .message("Modified")
                .build();

        assertThat(modified.getData()).isEqualTo("original");
        assertThat(modified.getMessage()).isEqualTo("Modified");
    }

    @Test
    void shouldIncludeMetadata() {
        EnvelopeResponse<String> response = EnvelopeResponse.<String>builder()
                .success(true)
                .data("test")
                .metadata("customKey", "customValue")
                .metadata("anotherKey", 123)
                .build();

        assertThat(response.getMetadata()).containsEntry("customKey", "customValue");
        assertThat(response.getMetadata()).containsEntry("anotherKey", 123);
    }
}
