package org.cookcounty.tax.infrastructure.adapter.in.rest;

import java.util.Optional;

import jakarta.validation.ConstraintViolationException;

import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public final class BatchRunExceptionHandler {

    @ExceptionHandler(BatchRunInvalidRequestException.class)
    public ResponseEntity<ErrorEnvelope> handleInvalidRequest(BatchRunInvalidRequestException exception) {
        return invalidRequest(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> validationMessage(error.getField(), error.getDefaultMessage()))
                .sorted()
                .findFirst()
                .orElseGet(() -> exception.getBindingResult().getAllErrors().stream()
                        .map(error -> validationMessage(null, error.getDefaultMessage()))
                        .sorted()
                        .findFirst()
                        .orElse("Request validation failed"));
        return invalidRequest(message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorEnvelope> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        String message = exception.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String parameterName = Optional.ofNullable(
                                    result.getMethodParameter().getParameterName())
                            .orElse("request parameter");
                    return result.getResolvableErrors().stream()
                            .map(error -> validationMessage(
                                    parameterName, error.getDefaultMessage()));
                })
                .sorted()
                .findFirst()
                .orElse("Request parameter validation failed");
        return invalidRequest(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(
            ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> validationMessage(
                        finalPathSegment(violation.getPropertyPath().toString()),
                        violation.getMessage()))
                .sorted()
                .findFirst()
                .orElse("Request constraint validation failed");
        return invalidRequest(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorEnvelope> handleUnreadableRequest(
            HttpMessageNotReadableException exception) {
        return invalidRequest("Request body is malformed or contains an invalid value");
    }

    @ExceptionHandler(BatchRunIdempotencyConflictException.class)
    public ResponseEntity<ErrorEnvelope> handleIdempotencyConflict(
            BatchRunIdempotencyConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorEnvelope("IDEMPOTENCY_CONFLICT", exception.getMessage()));
    }

    private static ResponseEntity<ErrorEnvelope> invalidRequest(String message) {
        return ResponseEntity.badRequest()
                .body(new ErrorEnvelope("INVALID_REQUEST", message));
    }

    private static String validationMessage(String subject, String detail) {
        if (detail == null || detail.isBlank()) {
            return subject == null ? "Request validation failed" : subject + " is invalid";
        }
        if (subject == null || detail.startsWith(subject) || namesRequestField(detail)) {
            return detail;
        }
        return subject + " " + detail;
    }

    private static boolean namesRequestField(String detail) {
        int separator = detail.indexOf(' ');
        if (separator < 1 || separator == detail.length() - 1) {
            return false;
        }
        String remainder = detail.substring(separator + 1);
        return remainder.startsWith("is ") || remainder.startsWith("must ");
    }

    private static String finalPathSegment(String path) {
        int separator = path.lastIndexOf('.');
        return separator < 0 ? path : path.substring(separator + 1);
    }

    public record ErrorEnvelope(String error, String message) {}
}
