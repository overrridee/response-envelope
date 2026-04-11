package io.github.overrridee.context;

import io.github.overrridee.model.EnvelopeContext;

/**
 * Thread-local holder for EnvelopeContext.
 *
 * <p>Holds and shares context information in request scope.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
public final class EnvelopeContextHolder {

    private static final ThreadLocal<EnvelopeContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private EnvelopeContextHolder() {
        // Utility class
    }

    /**
     * Gets current context.
     *
     * @return EnvelopeContext or null
     */
    public static EnvelopeContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * Sets context.
     *
     * @param context EnvelopeContext
     */
    public static void setContext(EnvelopeContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * Clears context.
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * Gets request ID.
     *
     * @return request ID or null
     */
    public static String getRequestId() {
        EnvelopeContext context = getContext();
        return context != null ? context.getRequestId() : null;
    }

    /**
     * Checks if context exists.
     *
     * @return true if context exists
     */
    public static boolean hasContext() {
        return CONTEXT_HOLDER.get() != null;
    }
}
