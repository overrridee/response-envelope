package io.github.responseenvelope.annotation;

import java.lang.annotation.*;

/**
 * Container for multiple {@link EnvelopeField} annotations.
 *
 * @author aedemirsen
 * @version 1.0.0
 * @see EnvelopeField
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnvelopeFields {

    /**
     * EnvelopeField annotations.
     *
     * @return EnvelopeField array
     */
    EnvelopeField[] value();
}
