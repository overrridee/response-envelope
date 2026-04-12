package io.github.overrridee;

import io.github.overrridee.util.RequestIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequestIdGenerator unit tests.
 */
class RequestIdGeneratorTest {

    private RequestIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RequestIdGenerator();
    }

    @Test
    void shouldGenerateUniqueIds() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            String id = generator.generate();
            assertThat(ids.add(id)).isTrue();
        }
    }

    @Test
    void shouldStartWithPrefix() {
        String id = generator.generate();
        assertThat(id).startsWith("req_");
    }

    @Test
    void shouldGenerateValidId() {
        String id = generator.generate();
        assertThat(generator.isValid(id)).isTrue();
    }

    @Test
    void shouldGenerateUUIDBasedId() {
        String id = generator.generateUUID();

        assertThat(id).startsWith("req_");
        assertThat(id.length()).isEqualTo(36); // req_ + 32 hex chars
    }

    @Test
    void shouldGenerateShortId() {
        String id = generator.generateShort();

        assertThat(id).startsWith("req_");
        assertThat(id.length()).isEqualTo(12); // req_ + 8 chars
    }

    @Test
    void shouldGenerateSequentialId() {
        String id1 = generator.generateSequential();
        String id2 = generator.generateSequential();

        assertThat(id1).startsWith("req_");
        assertThat(id2).startsWith("req_");
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldGenerateWithCustomPrefix() {
        String id = generator.generateWithPrefix("custom");

        assertThat(id).startsWith("custom_");
        assertThat(id.length()).isEqualTo(19); // custom_ + 12 chars
    }

    @Test
    void shouldValidateCorrectId() {
        assertThat(generator.isValid("req_abc123xyz")).isTrue();
        assertThat(generator.isValid("req_1234567890")).isTrue();
    }

    @Test
    void shouldInvalidateIncorrectId() {
        assertThat(generator.isValid(null)).isFalse();
        assertThat(generator.isValid("")).isFalse();
        assertThat(generator.isValid("abc123")).isFalse();
        assertThat(generator.isValid("req_")).isFalse();
        assertThat(generator.isValid("req_12345")).isFalse(); // too short
    }

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        Set<String> ids = java.util.Collections.synchronizedSet(new HashSet<>());
        int threadCount = 10;
        int idsPerThread = 100;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    ids.add(generator.generate());
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(ids).hasSize(threadCount * idsPerThread);
    }
}
