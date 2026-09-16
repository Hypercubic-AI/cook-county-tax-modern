package org.cookcounty.tax.domain.port.out;

import org.jspecify.annotations.Nullable;

/// Signals that another transaction persisted the same capability-scoped key.
public final class DuplicateBatchRunException extends RuntimeException {
    /// Retains the uniqueness violation without changing its original diagnostic cause.
    ///
    /// @param cause underlying store failure, or null for a store without a native exception
    public DuplicateBatchRunException(@Nullable Throwable cause) {
        super("The batch run key already exists", cause);
    }
}
