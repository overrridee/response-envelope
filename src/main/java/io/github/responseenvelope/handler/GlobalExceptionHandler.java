package io.github.responseenvelope.handler;

import io.github.responseenvelope.config.EnvelopeProperties;
import io.github.responseenvelope.enums.ErrorCode;
import io.github.responseenvelope.exception.BusinessException;
import io.github.responseenvelope.exception.EntityNotFoundException;
import io.github.responseenvelope.exception.EnvelopeException;
import io.github.responseenvelope.exception.ValidationException;
import io.github.responseenvelope.model.EnvelopeResponse;
import io.github.responseenvelope.util.RequestIdGenerator;
import io.github.responseenvelope.util.TimestampFormatter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Global exception handler.
 *
 * <p>Catches all exceptions and returns in consistent EnvelopeResponse format.</p>
 *
 * @author aedemirsen
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "response-envelope", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "response-envelope.error-handling", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(name = "globalExceptionHandler")
public class GlobalExceptionHandler {

    private final EnvelopeProperties properties;
    private final RequestIdGenerator requestIdGenerator;
    private final TimestampFormatter timestampFormatter;

    /**
     * EnvelopeException handler.
     */
    @ExceptionHandler(EnvelopeException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleEnvelopeException(
            EnvelopeException ex, HttpServletRequest request) {

        log.error("EnvelopeException: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        EnvelopeResponse<Void> response = buildErrorResponse(
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                ex.getDetails(),
                ex,
                request,
                null
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * EntityNotFoundException handler.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        log.warn("EntityNotFoundException: {}", ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.ENTITY_NOT_FOUND.getCode(),
                ex.getMessage(),
                ex.getDetails(),
                ex,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * ValidationException handler.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        log.warn("ValidationException: {}", ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ex.getMessage(),
                null,
                ex,
                request,
                ex.getFieldErrors()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * BusinessException handler.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("BusinessException: {} - {}", ex.getErrorCode(), ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                ex.getDetails(),
                ex,
                request,
                null
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * MethodArgumentNotValidException handler (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());

        List<EnvelopeResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(EnvelopeResponse.FieldError.builder()
                    .field(error.getField())
                    .message(error.getDefaultMessage())
                    .rejectedValue(error.getRejectedValue())
                    .code(error.getCode())
                    .build());
        }

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Validation failed",
                "One or more fields have validation errors",
                ex,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * BindException handler.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleBindException(
            BindException ex, HttpServletRequest request) {

        log.warn("BindException: {}", ex.getMessage());

        List<EnvelopeResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(EnvelopeResponse.FieldError.builder()
                    .field(error.getField())
                    .message(error.getDefaultMessage())
                    .rejectedValue(error.getRejectedValue())
                    .code(error.getCode())
                    .build());
        }

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Binding failed",
                null,
                ex,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * ConstraintViolationException handler.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("ConstraintViolationException: {}", ex.getMessage());

        List<EnvelopeResponse.FieldError> fieldErrors = new ArrayList<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String field = violation.getPropertyPath().toString();
            fieldErrors.add(EnvelopeResponse.FieldError.builder()
                    .field(field)
                    .message(violation.getMessage())
                    .rejectedValue(violation.getInvalidValue())
                    .build());
        }

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Constraint violation",
                null,
                ex,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * HttpMessageNotReadableException handler.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.INVALID_FORMAT.getCode(),
                "Malformed JSON request",
                properties.getErrorConfig().isShowDetailedMessages() ? ex.getMessage() : null,
                ex,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * MissingServletRequestParameterException handler.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("MissingServletRequestParameterException: {}", ex.getMessage());

        List<EnvelopeResponse.FieldError> fieldErrors = List.of(
                EnvelopeResponse.FieldError.builder()
                        .field(ex.getParameterName())
                        .message("Required parameter is missing")
                        .build()
        );

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.MISSING_REQUIRED_FIELD.getCode(),
                "Missing required parameter: " + ex.getParameterName(),
                null,
                ex,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * MethodArgumentTypeMismatchException handler.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("MethodArgumentTypeMismatchException: {}", ex.getMessage());

        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        List<EnvelopeResponse.FieldError> fieldErrors = List.of(
                EnvelopeResponse.FieldError.builder()
                        .field(ex.getName())
                        .message(message)
                        .rejectedValue(ex.getValue())
                        .build()
        );

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.INVALID_INPUT.getCode(),
                message,
                null,
                ex,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * HttpRequestMethodNotSupportedException handler.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("HttpRequestMethodNotSupportedException: {}", ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                "HTTP method not supported: " + ex.getMethod(),
                "Supported methods: " + String.join(", ", ex.getSupportedMethods() != null ?
                        ex.getSupportedMethods() : new String[]{}),
                ex,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * HttpMediaTypeNotSupportedException handler.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("HttpMediaTypeNotSupportedException: {}", ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.BAD_REQUEST.getCode(),
                "Media type not supported: " + ex.getContentType(),
                null,
                ex,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    /**
     * NoHandlerFoundException handler.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("NoHandlerFoundException: {}", ex.getMessage());

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.NOT_FOUND.getCode(),
                "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                null,
                ex,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Generic Exception handler (catch-all).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<EnvelopeResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        String message = properties.getErrorConfig().isShowDetailedMessages() ?
                ex.getMessage() : "An unexpected error occurred";

        EnvelopeResponse<Void> response = buildErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                message,
                null,
                ex,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Creates error response.
     */
    private EnvelopeResponse<Void> buildErrorResponse(
            String code,
            String message,
            String details,
            Exception ex,
            HttpServletRequest request,
            List<EnvelopeResponse.FieldError> fieldErrors) {

        String requestId = getOrGenerateRequestId(request);

        EnvelopeResponse.ErrorDetails.ErrorDetailsBuilder errorBuilder =
                EnvelopeResponse.ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .details(details);

        // Field errors
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            errorBuilder.fieldErrors(fieldErrors);
        }

        // Exception class (debug modda)
        if (properties.getErrorConfig().isIncludeExceptionClass()) {
            errorBuilder.exception(ex.getClass().getName());
        }

        // Stack trace (debug modda)
        if (properties.getErrorConfig().isIncludeStacktrace()) {
            errorBuilder.stackTrace(getStackTrace(ex));
        }

        // Error source
        if (!properties.getErrorConfig().getErrorSource().isEmpty()) {
            errorBuilder.source(properties.getErrorConfig().getErrorSource());
        }

        // Documentation URL
        if (!properties.getErrorConfig().getDocumentationUrlPattern().isEmpty()) {
            String docUrl = properties.getErrorConfig().getDocumentationUrlPattern()
                    .replace("{code}", code);
            errorBuilder.documentationUrl(docUrl);
        }

        return EnvelopeResponse.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(timestampFormatter.format(Instant.now()))
                .requestId(requestId)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errors(errorBuilder.build())
                .build();
    }

    /**
     * Gets or generates request ID.
     */
    private String getOrGenerateRequestId(HttpServletRequest request) {
        String headerName = properties.getDefaultConfig().getRequestIdHeader();
        String requestId = request.getHeader(headerName);
        if (requestId == null || requestId.isEmpty()) {
            requestId = requestIdGenerator.generate();
        }
        return requestId;
    }

    /**
     * Gets stack trace as string.
     */
    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
