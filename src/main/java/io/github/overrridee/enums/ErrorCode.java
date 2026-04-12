package io.github.overrridee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Standard error codes.
 *
 * <p>Provides consistent error codes in API.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 4xx Client Errors
    BAD_REQUEST("ERR_400", "Bad Request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("ERR_401", "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ERR_403", "Forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("ERR_404", "Resource Not Found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("ERR_405", "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT("ERR_409", "Conflict", HttpStatus.CONFLICT),
    GONE("ERR_410", "Resource Gone", HttpStatus.GONE),
    UNPROCESSABLE_ENTITY("ERR_422", "Unprocessable Entity", HttpStatus.UNPROCESSABLE_ENTITY),
    TOO_MANY_REQUESTS("ERR_429", "Too Many Requests", HttpStatus.TOO_MANY_REQUESTS),

    // Validation Errors
    VALIDATION_ERROR("ERR_VAL_001", "Validation Error", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("ERR_VAL_002", "Invalid Input", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("ERR_VAL_003", "Missing Required Field", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("ERR_VAL_004", "Invalid Format", HttpStatus.BAD_REQUEST),

    // Business Logic Errors
    BUSINESS_ERROR("ERR_BIZ_001", "Business Logic Error", HttpStatus.UNPROCESSABLE_ENTITY),
    ENTITY_NOT_FOUND("ERR_BIZ_002", "Entity Not Found", HttpStatus.NOT_FOUND),
    DUPLICATE_ENTITY("ERR_BIZ_003", "Duplicate Entity", HttpStatus.CONFLICT),
    OPERATION_NOT_ALLOWED("ERR_BIZ_004", "Operation Not Allowed", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS("ERR_BIZ_005", "Insufficient Permissions", HttpStatus.FORBIDDEN),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR("ERR_500", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("ERR_503", "Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    GATEWAY_TIMEOUT("ERR_504", "Gateway Timeout", HttpStatus.GATEWAY_TIMEOUT),

    // Integration Errors
    EXTERNAL_SERVICE_ERROR("ERR_EXT_001", "External Service Error", HttpStatus.BAD_GATEWAY),
    TIMEOUT_ERROR("ERR_EXT_002", "Timeout Error", HttpStatus.GATEWAY_TIMEOUT),
    CONNECTION_ERROR("ERR_EXT_003", "Connection Error", HttpStatus.SERVICE_UNAVAILABLE),

    // Unknown
    UNKNOWN_ERROR("ERR_999", "Unknown Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    /**
     * Returns ErrorCode based on HTTP status code.
     *
     * @param status HTTP status
     * @return related ErrorCode
     */
    public static ErrorCode fromHttpStatus(HttpStatus status) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.httpStatus == status) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }

    /**
     * Returns ErrorCode based on code string.
     *
     * @param code error code string
     * @return related ErrorCode or UNKNOWN_ERROR
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}
