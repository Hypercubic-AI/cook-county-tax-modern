package org.cookcounty.tax.domain.contract;

import java.util.Objects;

/// A recoverable failure from a request-driven batch operation.
public sealed interface BatchRunFailure
        permits BatchRunFailure.InvalidRequest,
                BatchRunFailure.IdempotencyConflict,
                BatchRunFailure.RunNotFound,
                BatchRunFailure.AdmissionRejected {

    /// The launch controls failed domain validation before a run was reserved.
    ///
    /// @param message caller-safe validation detail
    record InvalidRequest(String message) implements BatchRunFailure {
        /// Requires a caller-safe explanation of the rejected controls.
        public InvalidRequest {
            Objects.requireNonNull(message, "message");
        }
    }

    /// The idempotency key identifies a request with a different fingerprint.
    ///
    /// @param message caller-safe conflict detail
    record IdempotencyConflict(String message) implements BatchRunFailure {
        /// Requires a caller-safe explanation without exposing the conflicting request.
        public IdempotencyConflict {
            Objects.requireNonNull(message, "message");
        }
    }

    /// No persisted run exists for the supplied server-assigned identifier.
    ///
    /// @param id server-assigned run identifier that was not found
    record RunNotFound(long id) implements BatchRunFailure {}

    /// The configured admission limit rejected the run before its worker started.
    ///
    /// @param message caller-safe admission detail
    record AdmissionRejected(String message) implements BatchRunFailure {
        /// Requires an explanation of the capacity rejection.
        public AdmissionRejected {
            Objects.requireNonNull(message, "message");
        }
    }
}
