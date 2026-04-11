package io.github.overrridee.util;

import io.github.overrridee.config.EnvelopeProperties;
import io.github.overrridee.enums.TimestampFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Timestamp formatter.
 *
 * <p>Supports different timestamp formats.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class TimestampFormatter {

    private final EnvelopeProperties properties;

    private static final DateTimeFormatter RFC_1123_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME;

    /**
     * Formats timestamp.
     *
     * @param instant timestamp
     * @param format  format type
     * @return formatted timestamp
     */
    public String format(Instant instant, TimestampFormat format) {
        if (instant == null) {
            instant = Instant.now();
        }

        return switch (format) {
            case ISO_8601 -> formatIso8601(instant);
            case EPOCH_MILLIS -> String.valueOf(instant.toEpochMilli());
            case EPOCH_SECONDS -> String.valueOf(instant.getEpochSecond());
            case RFC_1123 -> formatRfc1123(instant);
            case CUSTOM -> formatCustom(instant);
        };
    }

    /**
     * Formats timestamp with default format.
     *
     * @param instant timestamp
     * @return formatted timestamp
     */
    public String format(Instant instant) {
        return format(instant, properties.getDefaultConfig().getTimestampFormat());
    }

    /**
     * Formats current time.
     *
     * @return formatted timestamp
     */
    public String formatNow() {
        return format(Instant.now());
    }

    /**
     * Formats in ISO 8601 format.
     *
     * @param instant timestamp
     * @return ISO 8601 formatted string
     */
    private String formatIso8601(Instant instant) {
        ZoneId zoneId = ZoneId.of(properties.getDefaultConfig().getTimezone());
        ZonedDateTime zdt = instant.atZone(zoneId);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zdt);
    }

    /**
     * Formats in RFC 1123 format.
     *
     * @param instant timestamp
     * @return RFC 1123 formatted string
     */
    private String formatRfc1123(Instant instant) {
        ZonedDateTime zdt = instant.atZone(ZoneId.of("GMT"));
        return RFC_1123_FORMATTER.format(zdt);
    }

    /**
     * Formats with custom pattern.
     *
     * @param instant timestamp
     * @return custom formatted string
     */
    private String formatCustom(Instant instant) {
        String pattern = properties.getDefaultConfig().getCustomTimestampPattern();
        ZoneId zoneId = ZoneId.of(properties.getDefaultConfig().getTimezone());
        ZonedDateTime zdt = instant.atZone(zoneId);
        return DateTimeFormatter.ofPattern(pattern).format(zdt);
    }

    /**
     * Parses timestamp string.
     *
     * @param timestamp timestamp string
     * @param format    format type
     * @return Instant
     */
    public Instant parse(String timestamp, TimestampFormat format) {
        return switch (format) {
            case ISO_8601 -> Instant.parse(timestamp);
            case EPOCH_MILLIS -> Instant.ofEpochMilli(Long.parseLong(timestamp));
            case EPOCH_SECONDS -> Instant.ofEpochSecond(Long.parseLong(timestamp));
            case RFC_1123 -> {
                ZonedDateTime zdt = ZonedDateTime.parse(timestamp, RFC_1123_FORMATTER);
                yield zdt.toInstant();
            }
            case CUSTOM -> {
                String pattern = properties.getDefaultConfig().getCustomTimestampPattern();
                ZoneId zoneId = ZoneId.of(properties.getDefaultConfig().getTimezone());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
                yield Instant.from(formatter.parse(timestamp));
            }
        };
    }
}
