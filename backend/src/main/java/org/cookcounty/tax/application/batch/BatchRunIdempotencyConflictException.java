package org.cookcounty.tax.application.batch;

import java.util.Objects;

/** Raised when an idempotency key is reused for different launch controls. */
public final class BatchRunIdempotencyConflictException extends RuntimeException {

    public BatchRunIdempotencyConflictException(String message) {
        super(Objects.requireNonNull(message, "message"));
    }
}
