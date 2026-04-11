package io.github.responseenvelope.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Response envelope field configuration.
 *
 * <p>Allows developers to dynamically control fields in the response.</p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * response-envelope:
 *   field-config:
 *     enabled-fields:
 *       success: true
 *       data: true
 *       timestamp: true
 *       requestId: true
 *       path: false
 *       method: false
 *     custom-fields:
 *       environment: production
 *       region: eu-west-1
 *       tenant: default
 * }</pre>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "response-envelope.field-config")
public class EnvelopeFieldConfig {

    /**
     * Whether field configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * Which fields to include in response.
     * <p>Key: field name, Value: enabled?</p>
     */
    private Map<String, Boolean> enabledFields = new HashMap<>();

    /**
     * Global custom fields.
     * <p>Automatically added to all responses.</p>
     */
    private Map<String, Object> customFields = new HashMap<>();

    /**
     * Field name customization.
     * <p>Used to change default field names.</p>
     * <p>Example: success -> status, data -> result</p>
     */
    private Map<String, String> fieldNames = new HashMap<>();

    /**
     * Conditional fields.
     * <p>Add/remove fields based on certain conditions.</p>
     */
    private ConditionalFields conditionalFields = new ConditionalFields();

    /**
     * Field ordering.
     * <p>Field order in response JSON.</p>
     */
    private FieldOrder fieldOrder = new FieldOrder();

    /**
     * Checks if a field is enabled.
     *
     * @param fieldName field name
     * @return true if enabled
     */
    public boolean isFieldEnabled(String fieldName) {
        if (!enabled) {
            return true; // All fields enabled if config disabled
        }
        return enabledFields.getOrDefault(fieldName, true);
    }

    /**
     * Returns field name with customization applied.
     *
     * @param defaultName default field name
     * @return customized or default name
     */
    public String getFieldName(String defaultName) {
        return fieldNames.getOrDefault(defaultName, defaultName);
    }

    /**
     * Adds a custom field.
     *
     * @param key   field name
     * @param value field value
     */
    public void addCustomField(String key, Object value) {
        customFields.put(key, value);
    }

    /**
     * Removes a custom field.
     *
     * @param key field name
     */
    public void removeCustomField(String key) {
        customFields.remove(key);
    }

    /**
     * Conditional field configuration.
     */
    @Data
    public static class ConditionalFields {

        /**
         * Fields to add only on error.
         */
        private Map<String, Object> onError = new HashMap<>();

        /**
         * Fields to add only on success.
         */
        private Map<String, Object> onSuccess = new HashMap<>();

        /**
         * Fields to add only on specific HTTP status codes.
         */
        private Map<Integer, Map<String, Object>> onHttpStatus = new HashMap<>();

        /**
         * Fields to add only on specific path patterns.
         */
        private Map<String, Map<String, Object>> onPathPattern = new HashMap<>();
    }

    /**
     * Field ordering configuration.
     */
    @Data
    public static class FieldOrder {

        /**
         * Whether field ordering is enabled.
         */
        private boolean enabled = false;

        /**
         * Field order.
         * <p>Written to JSON in specified order.</p>
         */
        private java.util.List<String> order = java.util.List.of(
                "success",
                "data",
                "message",
                "timestamp",
                "requestId",
                "path",
                "method",
                "duration",
                "apiVersion",
                "pagination",
                "links",
                "errors"
        );
    }

    /**
     * Preset configurations.
     */
    public enum Preset {
        /**
         * Minimal - Only basic fields.
         */
        MINIMAL,

        /**
         * Standard - Balanced field set.
         */
        STANDARD,

        /**
         * Full - All fields.
         */
        FULL,

        /**
         * Debug - Extra fields for debugging.
         */
        DEBUG,

        /**
         * Production - Optimized for production.
         */
        PRODUCTION
    }

    /**
     * Applies preset configuration.
     *
     * @param preset preset type
     */
    public void applyPreset(Preset preset) {
        enabledFields.clear();

        switch (preset) {
            case MINIMAL -> {
                enabledFields.put("success", true);
                enabledFields.put("data", true);
                enabledFields.put("message", true);
            }
            case STANDARD -> {
                enabledFields.put("success", true);
                enabledFields.put("data", true);
                enabledFields.put("message", true);
                enabledFields.put("timestamp", true);
                enabledFields.put("requestId", true);
            }
            case FULL -> {
                // All fields enabled (default)
            }
            case DEBUG -> {
                enabledFields.put("success", true);
                enabledFields.put("data", true);
                enabledFields.put("message", true);
                enabledFields.put("timestamp", true);
                enabledFields.put("requestId", true);
                enabledFields.put("path", true);
                enabledFields.put("method", true);
                enabledFields.put("duration", true);
                customFields.put("debug", true);
            }
            case PRODUCTION -> {
                enabledFields.put("success", true);
                enabledFields.put("data", true);
                enabledFields.put("message", true);
                enabledFields.put("requestId", true);
                enabledFields.put("timestamp", false);
                enabledFields.put("path", false);
                enabledFields.put("method", false);
                enabledFields.put("duration", false);
            }
        }
    }
}
