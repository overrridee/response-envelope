package io.github.overrridee;

import io.github.overrridee.annotation.ResponseEnvelope;
import io.github.overrridee.enums.ErrorCode;
import io.github.overrridee.exception.BusinessException;
import io.github.overrridee.exception.EntityNotFoundException;
import io.github.overrridee.exception.ValidationException;
import io.github.overrridee.model.EnvelopeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Global Exception Handler integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/error-test/not-found/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_BIZ_002"))
                .andExpect(jsonPath("$.errors.message").value("User not found with id: 999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.path").value("/error-test/not-found/999"));
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(get("/error-test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_VAL_001"))
                .andExpect(jsonPath("$.errors.fieldErrors").isArray())
                .andExpect(jsonPath("$.errors.fieldErrors[0].field").value("email"))
                .andExpect(jsonPath("$.errors.fieldErrors[0].message").value("Invalid email format"));
    }

    @Test
    void shouldHandleBusinessException() throws Exception {
        mockMvc.perform(get("/error-test/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_BIZ_001"))
                .andExpect(jsonPath("$.message").value("Business rule violated"));
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/error-test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_VAL_001"))
                .andExpect(jsonPath("$.errors.fieldErrors").isArray())
                .andExpect(jsonPath("$.errors.fieldErrors[0].field").value("name"));
    }

    @Test
    void shouldHandleMissingRequestParameter() throws Exception {
        mockMvc.perform(get("/error-test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_VAL_003"))
                .andExpect(jsonPath("$.errors.fieldErrors[0].field").value("required"));
    }

    @Test
    void shouldHandleMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/error-test/not-found/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_405"));
    }

    @Test
    void shouldHandleHttpMessageNotReadable() throws Exception {
        mockMvc.perform(post("/error-test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_VAL_004"));
    }

    @Test
    void shouldHandleTypeMismatch() throws Exception {
        mockMvc.perform(get("/error-test/not-found/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_VAL_002"));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/error-test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.code").value("ERR_500"));
    }

    @Test
    void shouldIncludeRequestIdInErrorResponse() throws Exception {
        String customRequestId = "req_error123";

        mockMvc.perform(get("/error-test/not-found/999")
                        .header("X-Request-ID", customRequestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.requestId").value(customRequestId));
    }

    /**
     * Test controller for error scenarios.
     */
    @RestController
    @RequestMapping("/error-test")
    static class ErrorTestController {

        @GetMapping("/not-found/{id}")
        @ResponseEnvelope
        public String getNotFound(@PathVariable Long id) {
            throw new EntityNotFoundException("User", id);
        }

        @GetMapping("/validation")
        @ResponseEnvelope
        public String getValidation() {
            throw new ValidationException("email", "Invalid email format", "invalid@");
        }

        @GetMapping("/business")
        @ResponseEnvelope
        public String getBusiness() {
            throw new BusinessException("Business rule violated");
        }

        @PostMapping("/validate")
        @ResponseEnvelope
        public ValidateDto validate(@Valid @RequestBody ValidateDto dto) {
            return dto;
        }

        @GetMapping("/param")
        @ResponseEnvelope
        public String getParam(@RequestParam String required) {
            return required;
        }

        @GetMapping("/generic")
        @ResponseEnvelope
        public String getGeneric() {
            throw new RuntimeException("Unexpected error");
        }
    }

    /**
     * Validation test DTO.
     */
    record ValidateDto(
            @NotBlank(message = "Name is required")
            @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
            String name
    ) {
    }

    /**
     * Test application.
     */
    @SpringBootApplication
    static class TestApplication {
    }
}
