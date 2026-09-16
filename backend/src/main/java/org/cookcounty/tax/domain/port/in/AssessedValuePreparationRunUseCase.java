package org.cookcounty.tax.domain.port.in;

import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunResponse;

/// Starts and observes assessed-value preparation for persisted parcel data.
///
/// Parcel values use whole dollars and preserve source rounding. The implementation reserves a
/// durable run identity before dispatch. An interrupted worker does not resume from an intermediate
/// stage. Every expected request, admission, conflict, and lookup failure uses the typed result.
public interface AssessedValuePreparationRunUseCase {

    /// Returns the latest persisted snapshot for one run.
    ///
    /// @param id positive generated run identity
    /// @return the snapshot or a typed missing-run failure
    Result<AssessedValuePreparationRunResponse, BatchRunFailure> getAssessedValuePreparationRun(
            Long id);

    /// Reserves a run or returns the existing snapshot for an exact idempotent replay.
    ///
    /// @param request structurally validated run controls
    /// @return a reservation, exact replay, or typed expected failure
    Result<BatchRunStart<AssessedValuePreparationRunResponse>, BatchRunFailure>
            startAssessedValuePreparationRun(AssessedValuePreparationRunRequest request);
}
