package io.github.overrridee.exception;

import io.github.overrridee.enums.ErrorCode;

/**
 * Business logic error exception.
 *
 * @author aedemirsen
 * @version 1.0.0
 */
public class BusinessException extends EnvelopeException {

    /**
     * Simple constructor.
     *
     * @param message error message
     */
    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    /**
     * Detailed constructor.
     *
     * @param message error message
     * @param details detailed description
     */
    public BusinessException(String message, String details) {
        super(ErrorCode.BUSINESS_ERROR, message, details);
    }

    /**
     * Constructor with ErrorCode.
     *
     * @param errorCode error code
     * @param message   error message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructor with ErrorCode and details.
     *
     * @param errorCode error code
     * @param message   error message
     * @param details   detailed description
     */
    public BusinessException(ErrorCode errorCode, String message, String details) {
        super(errorCode, message, details);
    }
}
