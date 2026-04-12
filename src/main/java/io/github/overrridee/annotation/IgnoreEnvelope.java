package io.github.overrridee.annotation;

import java.lang.annotation.*;

/**
 * Methods annotated with this will not be wrapped by ResponseEnvelope.
 *
 * <p>Used to exclude specific methods when @ResponseEnvelope is used at class level.</p>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * @RestController
 * @ResponseEnvelope  // All methods wrapped
 * public class UserController {
 *
 *     @GetMapping("/users/{id}")
 *     public User getUser(@PathVariable Long id) {
 *         return userService.findById(id);  // Wrapped
 *     }
 *
 *     @GetMapping("/users/export")
 *     @IgnoreEnvelope  // This method not wrapped
 *     public byte[] exportUsers() {
 *         return userService.exportToCsv();
 *     }
 * }
 * }</pre>
 *
 * @author aedemirsen
 * @version 1.0.0
 * @see ResponseEnvelope
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreEnvelope {

    /**
     * Reason for ignoring.
     *
     * @return reason text
     */
    String reason() default "";
}
