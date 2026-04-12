package io.github.overrridee;

import io.github.overrridee.annotation.IgnoreEnvelope;
import io.github.overrridee.annotation.ResponseEnvelope;
import io.github.overrridee.config.EnvelopeProperties;
import io.github.overrridee.model.EnvelopeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ResponseEnvelope Aspect integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ResponseEnvelopeAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldWrapSimpleResponse() throws Exception {
        mockMvc.perform(get("/test/simple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.path").value("/test/simple"))
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.duration").exists())
                .andExpect(jsonPath("$.apiVersion").value("v1"));
    }

    @Test
    void shouldWrapListResponse() throws Exception {
        mockMvc.perform(get("/test/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.pagination.numberOfElements").value(3));
    }

    @Test
    void shouldUseCustomVersion() throws Exception {
        mockMvc.perform(get("/test/v2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.apiVersion").value("v2"));
    }

    @Test
    void shouldIncludeSuccessMessage() throws Exception {
        mockMvc.perform(post("/test/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Created successfully"));
    }

    @Test
    void shouldIgnoreEnvelopeAnnotation() throws Exception {
        mockMvc.perform(get("/test/ignored"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").doesNotExist())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldHandleNullResponse() throws Exception {
        mockMvc.perform(get("/test/null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldUseRequestIdFromHeader() throws Exception {
        String customRequestId = "req_custom123";

        mockMvc.perform(get("/test/simple")
                        .header("X-Request-ID", customRequestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(customRequestId))
                .andExpect(header().string("X-Request-ID", customRequestId));
    }

    @Test
    void shouldExcludeDurationWhenDisabled() throws Exception {
        mockMvc.perform(get("/test/no-duration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.duration").doesNotExist());
    }

    @Test
    void shouldIncludeCustomMetadata() throws Exception {
        mockMvc.perform(get("/test/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.customKey").value("customValue"));
    }

    @Test
    void shouldAddDeprecationHeader() throws Exception {
        mockMvc.perform(get("/test/deprecated"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("X-Deprecation-Warning", "This endpoint is deprecated"));
    }

    @Test
    void shouldAddCacheHeader() throws Exception {
        mockMvc.perform(get("/test/cached"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=300"));
    }

    /**
     * Test controller.
     */
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/simple")
        @ResponseEnvelope
        public TestDto getSimple() {
            return new TestDto(1L, "Test");
        }

        @GetMapping("/list")
        @ResponseEnvelope
        public List<TestDto> getList() {
            return List.of(
                    new TestDto(1L, "One"),
                    new TestDto(2L, "Two"),
                    new TestDto(3L, "Three")
            );
        }

        @GetMapping("/v2")
        @ResponseEnvelope(version = "v2")
        public TestDto getV2() {
            return new TestDto(1L, "V2 Test");
        }

        @PostMapping("/create")
        @ResponseStatus(HttpStatus.CREATED)
        @ResponseEnvelope(successMessage = "Created successfully")
        public TestDto create(@RequestBody Map<String, String> body) {
            return new TestDto(1L, body.get("name"));
        }

        @GetMapping("/ignored")
        @ResponseEnvelope
        @IgnoreEnvelope(reason = "Test ignore")
        public TestDto getIgnored() {
            return new TestDto(1L, "Ignored");
        }

        @GetMapping("/null")
        @ResponseEnvelope
        public TestDto getNull() {
            return null;
        }

        @GetMapping("/no-duration")
        @ResponseEnvelope(includeDuration = false)
        public TestDto getNoDuration() {
            return new TestDto(1L, "No Duration");
        }

        @GetMapping("/metadata")
        @ResponseEnvelope(customMetadata = {"customKey:customValue"})
        public TestDto getMetadata() {
            return new TestDto(1L, "Metadata");
        }

        @GetMapping("/deprecated")
        @ResponseEnvelope(deprecationWarning = "This endpoint is deprecated")
        public TestDto getDeprecated() {
            return new TestDto(1L, "Deprecated");
        }

        @GetMapping("/cached")
        @ResponseEnvelope(cacheDuration = 300)
        public TestDto getCached() {
            return new TestDto(1L, "Cached");
        }
    }

    /**
     * Test DTO.
     */
    record TestDto(Long id, String name) {
    }

    /**
     * Test application.
     */
    @SpringBootApplication
    static class TestApplication {
    }
}
