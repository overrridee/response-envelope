package io.github.overrridee.enums;

/**
 * Response wrapping strategies.
 *
 * @author aedemirsen
 * @version 1.0.0
 */
public enum WrapStrategy {

    /**
     * Always wrap (default)
     */
    ALWAYS,

    /**
     * Only wrap successful responses
     */
    SUCCESS_ONLY,

    /**
     * Only wrap error responses
     */
    ERROR_ONLY,

    /**
     * Only wrap methods returning collection/array
     */
    COLLECTION_ONLY,

    /**
     * Never wrap (for debug)
     */
    NEVER
}
