package org.cookcounty.tax.application.batch;

import java.util.Objects;

/** Result of accepting or replaying a batch run start request. */
public record BatchRunStartResult<R>(long id, R response, boolean replayed) {

    public BatchRunStartResult {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(response, "response");
    }
}
