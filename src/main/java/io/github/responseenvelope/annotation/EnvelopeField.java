package io.github.responseenvelope.annotation;

import java.lang.annotation.*;

/**
 * Used to add custom fields to response envelope.
 *
 * <p>Can be applied to method parameters or return type fields.</p>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * @GetMapping("/users/{id}")
 * @ResponseEnvelope
 * @EnvelopeField(name = "correlationId", value = "#{correlationId}")
 * public User getUser(@PathVariable Long id) {
 *     return userService.findById(id);
 * }
 * }</pre>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(EnvelopeFields.class)
public @interface EnvelopeField {

    /**
     * Field name.
     *
     * @return field name
     */
    String name();

    /**
     * Field value.
     * <p>Supports SpEL expression: #{expression}</p>
     *
     * @return field value or SpEL expression
     */
    String value();

    /**
     * Whether field should be added to metadata section.
     *
     * @return true if added to metadata, false if to root
     */
    boolean inMetadata() default true;
}
