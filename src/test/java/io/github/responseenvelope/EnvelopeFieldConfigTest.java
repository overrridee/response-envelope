package io.github.responseenvelope;

import io.github.responseenvelope.config.EnvelopeFieldConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EnvelopeFieldConfig unit tests.
 */
class EnvelopeFieldConfigTest {

    private EnvelopeFieldConfig config;

    @BeforeEach
    void setUp() {
        config = new EnvelopeFieldConfig();
    }

    @Test
    void shouldEnableFieldsByDefault() {
        assertThat(config.isFieldEnabled("success")).isTrue();
        assertThat(config.isFieldEnabled("data")).isTrue();
        assertThat(config.isFieldEnabled("timestamp")).isTrue();
    }

    @Test
    void shouldDisableSpecificFields() {
        config.getEnabledFields().put("path", false);
        config.getEnabledFields().put("method", false);

        assertThat(config.isFieldEnabled("path")).isFalse();
        assertThat(config.isFieldEnabled("method")).isFalse();
        assertThat(config.isFieldEnabled("data")).isTrue();
    }

    @Test
    void shouldAddCustomFields() {
        config.addCustomField("environment", "production");
        config.addCustomField("region", "eu-west-1");

        assertThat(config.getCustomFields()).containsEntry("environment", "production");
        assertThat(config.getCustomFields()).containsEntry("region", "eu-west-1");
    }

    @Test
    void shouldRemoveCustomFields() {
        config.addCustomField("test", "value");
        assertThat(config.getCustomFields()).containsKey("test");

        config.removeCustomField("test");
        assertThat(config.getCustomFields()).doesNotContainKey("test");
    }

    @Test
    void shouldCustomizeFieldNames() {
        config.getFieldNames().put("success", "status");
        config.getFieldNames().put("data", "result");

        assertThat(config.getFieldName("success")).isEqualTo("status");
        assertThat(config.getFieldName("data")).isEqualTo("result");
        assertThat(config.getFieldName("timestamp")).isEqualTo("timestamp");
    }

    @Test
    void shouldApplyMinimalPreset() {
        config.applyPreset(EnvelopeFieldConfig.Preset.MINIMAL);

        assertThat(config.isFieldEnabled("success")).isTrue();
        assertThat(config.isFieldEnabled("data")).isTrue();
        assertThat(config.isFieldEnabled("message")).isTrue();
        assertThat(config.isFieldEnabled("timestamp")).isTrue(); // Not in preset, defaults to true
    }

    @Test
    void shouldApplyStandardPreset() {
        config.applyPreset(EnvelopeFieldConfig.Preset.STANDARD);

        assertThat(config.isFieldEnabled("success")).isTrue();
        assertThat(config.isFieldEnabled("data")).isTrue();
        assertThat(config.isFieldEnabled("message")).isTrue();
        assertThat(config.isFieldEnabled("timestamp")).isTrue();
        assertThat(config.isFieldEnabled("requestId")).isTrue();
    }

    @Test
    void shouldApplyProductionPreset() {
        config.applyPreset(EnvelopeFieldConfig.Preset.PRODUCTION);

        assertThat(config.isFieldEnabled("success")).isTrue();
        assertThat(config.isFieldEnabled("data")).isTrue();
        assertThat(config.isFieldEnabled("requestId")).isTrue();
        assertThat(config.isFieldEnabled("timestamp")).isFalse();
        assertThat(config.isFieldEnabled("path")).isFalse();
        assertThat(config.isFieldEnabled("method")).isFalse();
        assertThat(config.isFieldEnabled("duration")).isFalse();
    }

    @Test
    void shouldApplyDebugPreset() {
        config.applyPreset(EnvelopeFieldConfig.Preset.DEBUG);

        assertThat(config.isFieldEnabled("success")).isTrue();
        assertThat(config.isFieldEnabled("data")).isTrue();
        assertThat(config.isFieldEnabled("timestamp")).isTrue();
        assertThat(config.isFieldEnabled("requestId")).isTrue();
        assertThat(config.isFieldEnabled("path")).isTrue();
        assertThat(config.isFieldEnabled("method")).isTrue();
        assertThat(config.isFieldEnabled("duration")).isTrue();
        assertThat(config.getCustomFields()).containsEntry("debug", true);
    }

    @Test
    void shouldHandleConditionalFields() {
        config.getConditionalFields().getOnSuccess().put("cached", false);
        config.getConditionalFields().getOnError().put("support", "support@example.com");

        assertThat(config.getConditionalFields().getOnSuccess())
                .containsEntry("cached", false);
        assertThat(config.getConditionalFields().getOnError())
                .containsEntry("support", "support@example.com");
    }

    @Test
    void shouldDisableAllFieldsWhenConfigDisabled() {
        config.setEnabled(false);

        // When config is disabled, all fields should be enabled (no filtering)
        assertThat(config.isFieldEnabled("success")).isTrue();
        assertThat(config.isFieldEnabled("data")).isTrue();
        assertThat(config.isFieldEnabled("anyField")).isTrue();
    }

    @Test
    void shouldSupportFieldOrdering() {
        EnvelopeFieldConfig.FieldOrder order = config.getFieldOrder();
        
        assertThat(order.getOrder()).contains("success", "data", "message");
        assertThat(order.getOrder().get(0)).isEqualTo("success");
        assertThat(order.getOrder().get(1)).isEqualTo("data");
    }
}
