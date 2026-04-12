package io.github.overrridee.exception;

import io.github.overrridee.enums.ErrorCode;
import io.github.overrridee.model.EnvelopeResponse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation error exception.
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Getter
public class ValidationException extends EnvelopeException {

    private final List<EnvelopeResponse.FieldError> fieldErrors;

    /**
     * Simple constructor.
     *
     * @param message error message
     */
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.fieldErrors = new ArrayList<>();
    }

    /**
     * Constructor with field errors.
     *
     * @param message     error message
     * @param fieldErrors field errors
     */
    public ValidationException(String message, List<EnvelopeResponse.FieldError> fieldErrors) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new ArrayList<>();
    }

    /**
     * Constructor with single field error.
     *
     * @param field         field name
     * @param message       error message
     * @param rejectedValue rejected value
     */
    public ValidationException(String field, String message, Object rejectedValue) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.fieldErrors = new ArrayList<>();
        this.fieldErrors.add(EnvelopeResponse.FieldError.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build());
    }

    /**
     * Adds a field error.
     *
     * @param field         field name
     * @param message       error message
     * @param rejectedValue rejected value
     * @return this
     */
    public ValidationException addFieldError(String field, String message, Object rejectedValue) {
        this.fieldErrors.add(EnvelopeResponse.FieldError.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build());
        return this;
    }

    /**
     * Field error ekler.
     *
     * @param fieldError field error
     * @return this
     */
    public ValidationException addFieldError(EnvelopeResponse.FieldError fieldError) {
        this.fieldErrors.add(fieldError);
        return this;
    }

    /**
     * Checks if there are field errors.
     *
     * @return true if has errors
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
