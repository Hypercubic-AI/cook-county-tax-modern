package org.cookcounty.tax.infrastructure.adapter.in.rest;

import jakarta.validation.ConstraintViolationException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

/// Keeps validation and unexpected failures in the same caller-safe response shape.
///
/// Structural validation reports all violations. Business-rule precedence remains the
/// responsibility of the capability service. Field names come from validation metadata, never from
/// message text.
@RestControllerAdvice
public final class BatchRunExceptionHandler {
    /// Retains unexpected exception details in server diagnostics, not the response body.
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRunExceptionHandler.class);

    /// Reports every rejected body field and object-level constraint in deterministic order.
    ///
    /// @param exception structured Bean Validation results for the request body
    /// @return 400 with all violations in the shared error envelope
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<String> messages =
                exception.getBindingResult().getAllErrors().stream()
                        .map(
                                error ->
                                        validationMessage(
                                                error instanceof FieldError fieldError
                                                        ? fieldError.getField()
                                                        : null,
                                                error.getDefaultMessage()))
                        .sorted()
                        .toList();
        return invalidRequest(messages);
    }

    /// Reports every invalid request parameter using its structured parameter name.
    ///
    /// @param exception validation results for method parameters
    /// @return 400 with all parameter violations in the shared error envelope
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorEnvelope> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        List<String> messages =
                exception.getParameterValidationResults().stream()
                        .flatMap(
                                result ->
                                        result.getResolvableErrors().stream()
                                                .map(
                                                        error ->
                                                                validationMessage(
                                                                        result.getMethodParameter()
                                                                                .getParameterName(),
                                                                        error.getDefaultMessage())))
                        .sorted()
                        .toList();
        return invalidRequest(messages);
    }

    /// Reports all constraints rejected outside request-body binding.
    ///
    /// @param exception violations with structured property paths
    /// @return 400 with all property violations in the shared error envelope
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(
            ConstraintViolationException exception) {
        List<String> messages =
                exception.getConstraintViolations().stream()
                        .map(
                                violation ->
                                        validationMessage(
                                                finalPathSegment(
                                                        violation.getPropertyPath().toString()),
                                                violation.getMessage()))
                        .sorted()
                        .toList();
        return invalidRequest(messages);
    }

    /// Rejects an unreadable body without exposing parser or deserialization details.
    ///
    /// @return 400 in the shared error envelope
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorEnvelope> handleUnreadableRequest() {
        return invalidRequest(List.of("Request body is malformed or contains an invalid value"));
    }

    /// Logs an unexpected exception and returns a safe, consistent server-error response.
    ///
    /// @param exception unexpected failure retained with its stack trace in server diagnostics
    /// @return 500 without internal exception details
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected request failure", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorEnvelope(
                                "INTERNAL_ERROR", "The service could not complete the request."));
    }

    /// Combines every structural violation without changing the existing response schema.
    private static ResponseEntity<ErrorEnvelope> invalidRequest(List<String> messages) {
        String message =
                messages.isEmpty() ? "Request validation failed" : String.join("\n", messages);
        return ResponseEntity.badRequest().body(new ErrorEnvelope("INVALID_REQUEST", message));
    }

    /// Combines the metadata path and diagnostic without inferring a field name from prose.
    private static String validationMessage(@Nullable String subject, @Nullable String detail) {
        String description = detail == null || detail.isBlank() ? "is invalid" : detail;
        return subject == null || subject.isBlank() ? description : subject + ": " + description;
    }

    /// Removes method-path prefixes while preserving the validation provider's property name.
    private static String finalPathSegment(String path) {
        int separator = path.lastIndexOf('.');
        return separator < 0 ? path : path.substring(separator + 1);
    }

    /// Shared safe error body for structural validation and unexpected request failures.
    ///
    /// @param error stable machine-readable failure category
    /// @param message caller-safe detail, with one structural violation per line when applicable
    public record ErrorEnvelope(String error, String message) {}
}
