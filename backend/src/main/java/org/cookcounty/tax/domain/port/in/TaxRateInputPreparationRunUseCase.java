
package org.cookcounty.tax.domain.port.in;

import java.util.Optional;

import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunResponse;

public interface TaxRateInputPreparationRunUseCase {

    Optional<TaxRateInputPreparationRunResponse> getTaxRateInputPreparationRun(Long id);

    BatchRunStartResult<TaxRateInputPreparationRunResponse> startTaxRateInputPreparationRun(
            TaxRateInputPreparationRunRequest request);
}
