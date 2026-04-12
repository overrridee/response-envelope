package io.github.overrridee.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.overrridee.model.EnvelopeResponse;

import java.io.IOException;

/**
 * Custom JSON serializer for EnvelopeResponse.
 *
 * <p>Used for custom data key support and dynamic field addition.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
public class EnvelopeSerializer extends JsonSerializer<EnvelopeResponse<?>> {

    private final String dataKey;

    public EnvelopeSerializer() {
        this.dataKey = "data";
    }

    public EnvelopeSerializer(String dataKey) {
        this.dataKey = dataKey != null ? dataKey : "data";
    }

    @Override
    public void serialize(EnvelopeResponse<?> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {

        gen.writeStartObject();

        // success
        gen.writeBooleanField("success", value.isSuccess());

        // data (custom key ile)
        if (value.getData() != null) {
            gen.writeObjectField(dataKey, value.getData());
        }

        // message
        if (value.getMessage() != null) {
            gen.writeStringField("message", value.getMessage());
        }

        // timestamp
        if (value.getTimestamp() != null) {
            gen.writeStringField("timestamp", value.getTimestamp());
        }

        // requestId
        if (value.getRequestId() != null) {
            gen.writeStringField("requestId", value.getRequestId());
        }

        // path
        if (value.getPath() != null) {
            gen.writeStringField("path", value.getPath());
        }

        // method
        if (value.getMethod() != null) {
            gen.writeStringField("method", value.getMethod());
        }

        // duration
        if (value.getDuration() != null) {
            gen.writeNumberField("duration", value.getDuration());
        }

        // apiVersion
        if (value.getApiVersion() != null) {
            gen.writeStringField("apiVersion", value.getApiVersion());
        }

        // pagination
        if (value.getPagination() != null) {
            gen.writeObjectField("pagination", value.getPagination());
        }

        // links
        if (value.getLinks() != null) {
            gen.writeObjectField("links", value.getLinks());
        }

        // metadata
        if (value.getMetadata() != null && !value.getMetadata().isEmpty()) {
            for (var entry : value.getMetadata().entrySet()) {
                gen.writeObjectField(entry.getKey(), entry.getValue());
            }
        }

        // errors
        if (value.getErrors() != null) {
            gen.writeObjectField("errors", value.getErrors());
        }

        gen.writeEndObject();
    }
}
