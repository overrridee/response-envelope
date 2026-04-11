package io.github.responseenvelope.exception;

import io.github.responseenvelope.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Response Envelope base exception.
 *
 * <p>All envelope exceptions extend from this class.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Getter
public class EnvelopeException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final String details;
    private final transient Object[] args;

    /**
     * Simple constructor.
     *
     * @param message error message
     */
    public EnvelopeException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.details = null;
        this.args = null;
    }

    /**
     * Constructor with ErrorCode.
     *
     * @param errorCode error code
     * @param message   error message
     */
    public EnvelopeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = null;
        this.args = null;
    }

    /**
     * Detailed constructor.
     *
     * @param errorCode error code
     * @param message   error message
     * @param details   detailed description
     */
    public EnvelopeException(ErrorCode errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
        this.args = null;
    }

    /**
     * Constructor with cause.
     *
     * @param errorCode error code
     * @param message   error message
     * @param cause     root cause
     */
    public EnvelopeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = cause.getMessage();
        this.args = null;
    }

    /**
     * Full constructor.
     *
     * @param errorCode  error code
     * @param httpStatus HTTP status
     * @param message    error message
     * @param details    detailed description
     * @param cause      root cause
     * @param args       message arguments
     */
    public EnvelopeException(ErrorCode errorCode, HttpStatus httpStatus, String message,
                             String details, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
        this.args = args;
    }

    /**
     * Static factory method for builder pattern.
     *
     * @return EnvelopeExceptionBuilder
     */
    public static EnvelopeExceptionBuilder builder() {
        return new EnvelopeExceptionBuilder();
    }

    /**
     * Builder class.
     */
    public static class EnvelopeExceptionBuilder {
        private ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        private HttpStatus httpStatus;
        private String message;
        private String details;
        private Throwable cause;
        private Object[] args;

        public EnvelopeExceptionBuilder errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public EnvelopeExceptionBuilder httpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public EnvelopeExceptionBuilder message(String message) {
            this.message = message;
            return this;
        }

        public EnvelopeExceptionBuilder details(String details) {
            this.details = details;
            return this;
        }

        public EnvelopeExceptionBuilder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public EnvelopeExceptionBuilder args(Object... args) {
            this.args = args;
            return this;
        }

        public EnvelopeException build() {
            HttpStatus status = httpStatus != null ? httpStatus : errorCode.getHttpStatus();
            String msg = message != null ? message : errorCode.getDefaultMessage();
            return new EnvelopeException(errorCode, status, msg, details, cause, args);
        }
    }
}
