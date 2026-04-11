package io.github.responseenvelope.enums;

/**
 * Timestamp format options.
 *
 * @author aedemirsen
 * @version 1.0.0
 */
public enum TimestampFormat {

    /**
     * ISO 8601 format: 2024-04-10T14:30:00.000Z
     */
    ISO_8601,

    /**
     * Unix epoch milliseconds: 1712759400000
     */
    EPOCH_MILLIS,

    /**
     * Unix epoch seconds: 1712759400
     */
    EPOCH_SECONDS,

    /**
     * RFC 1123 format: Wed, 10 Apr 2024 14:30:00 GMT
     */
    RFC_1123,

    /**
     * Custom format (from properties)
     */
    CUSTOM
}
