package org.cookcounty.tax.domain.port.in;

import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunResponse;

/// Starts and observes the nine-step increment capability.
///
/// A run uses one pinned business clock and the current shared-live populations. Each mutating step
/// owns one whole-step transaction. A later failure does not reverse an earlier committed step.
/// Persisted snapshots and replay keys survive restart, but active processing does not resume.
public interface EifdTifIncrementRunUseCase {

    /// Returns the latest durable snapshot or a typed missing-run failure.
    ///
    /// @param id positive generated identifier returned by the launch operation
    /// @return the immutable snapshot or the expected missing-run failure
    Result<EifdTifIncrementRunResponse, BatchRunFailure> getEifdTifIncrementRun(Long id);

    /// Reserves one asynchronous execution for validated launch controls.
    ///
    /// An exact replay returns the existing resource without another execution. Reusing the key
    /// with different controls returns a conflict. Admission rejection creates no run.
    ///
    /// @param request nullable-at-input fields that Bean Validation checks as one set
    /// @return a new reservation, an exact replay, or a typed expected failure
    Result<BatchRunStart<EifdTifIncrementRunResponse>, BatchRunFailure> startEifdTifIncrementRun(
            EifdTifIncrementRunRequest request);
}
