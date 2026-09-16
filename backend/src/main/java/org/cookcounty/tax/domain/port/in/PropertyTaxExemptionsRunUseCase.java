package org.cookcounty.tax.domain.port.in;

import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunResponse;

/// Starts and observes the persisted property-tax exemption batch capability.
///
/// The start operation validates the evidenced homeowner variant before it reserves a run. A
/// matching idempotency replay returns the stored snapshot without executing the batch again. A
/// process restart does not resume an interrupted worker.
public interface PropertyTaxExemptionsRunUseCase {

    /// Returns the latest immutable snapshot or a typed missing-run failure.
    ///
    /// This read has no batch side effects.
    Result<PropertyTaxExemptionsRunResponse, BatchRunFailure> getPropertyTaxExemptionsRun(Long id);

    /// Returns a new reservation, an exact replay, or a typed expected failure.
    ///
    /// A new reservation dispatches asynchronous work. An exact replay does not execute again.
    Result<BatchRunStart<PropertyTaxExemptionsRunResponse>, BatchRunFailure>
            startPropertyTaxExemptionsRun(PropertyTaxExemptionsRunRequest request);
}
