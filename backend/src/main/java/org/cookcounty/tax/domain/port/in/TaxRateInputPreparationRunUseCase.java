package org.cookcounty.tax.domain.port.in;

import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunResponse;

/// Starts and observes source-ordered tax-rate input preparation.
///
/// Runs use shared repository state. Successful accumulator writes can remain after a later typed
/// processing failure. Persisted snapshots survive restart, but active work does not resume.
public interface TaxRateInputPreparationRunUseCase {
    /// Returns the latest durable snapshot or a typed missing-run failure.
    ///
    /// @param id positive server-generated run identity
    /// @return the current snapshot or `RunNotFound`
    Result<TaxRateInputPreparationRunResponse, BatchRunFailure> getTaxRateInputPreparationRun(
            Long id);

    /// Reserves asynchronous work, returns an exact replay, or returns a typed expected failure.
    ///
    /// Replaying the same key and business clock makes no new submission. Reusing the key with
    /// different values returns `IdempotencyConflict`.
    ///
    /// @param request business clock and caller-selected replay identity
    /// @return a new reservation, exact replay, invalid request, conflict, or capacity rejection
    Result<BatchRunStart<TaxRateInputPreparationRunResponse>, BatchRunFailure>
            startTaxRateInputPreparationRun(TaxRateInputPreparationRunRequest request);
}
