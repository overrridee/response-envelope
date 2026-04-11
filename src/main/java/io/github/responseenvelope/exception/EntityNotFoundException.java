package io.github.responseenvelope.exception;

import io.github.responseenvelope.enums.ErrorCode;

/**
 * Exception thrown when entity is not found.
 *
 * @author aedemirsen
 * @version 1.0.0
 */
public class EntityNotFoundException extends EnvelopeException {

    /**
     * Simple constructor.
     *
     * @param message error message
     */
    public EntityNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }

    /**
     * Entity tipi ve ID ile constructor.
     *
     * @param entityType entity tipi
     * @param id         entity ID
     */
    public EntityNotFoundException(String entityType, Object id) {
        super(ErrorCode.ENTITY_NOT_FOUND,
                String.format("%s not found with id: %s", entityType, id),
                String.format("The requested %s with identifier '%s' does not exist in the system", entityType, id));
    }

    /**
     * Entity class ve ID ile constructor.
     *
     * @param entityClass entity class
     * @param id          entity ID
     */
    public EntityNotFoundException(Class<?> entityClass, Object id) {
        this(entityClass.getSimpleName(), id);
    }
}
