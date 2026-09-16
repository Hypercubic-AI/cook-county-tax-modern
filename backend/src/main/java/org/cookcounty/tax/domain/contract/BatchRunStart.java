package org.cookcounty.tax.domain.contract;

import java.util.Objects;

/// A newly accepted batch run or the persisted snapshot for an exact replay.
///
/// @param id positive server-assigned run identifier
/// @param response complete persisted response snapshot
/// @param replayed whether the idempotency key matched an existing run
/// @param <R> public run snapshot type
public record BatchRunStart<R>(long id, R response, boolean replayed) {

    /// Enforces the persisted identity and response invariants.
    public BatchRunStart {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(response, "response");
    }
}
