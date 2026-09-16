package org.cookcounty.tax.application.batch;

import java.util.Objects;

/** Raised when a batch launch request cannot be accepted. */
public final class BatchRunInvalidRequestException extends RuntimeException {

    public BatchRunInvalidRequestException(String message) {
        super(Objects.requireNonNull(message, "message"));
    }
}
