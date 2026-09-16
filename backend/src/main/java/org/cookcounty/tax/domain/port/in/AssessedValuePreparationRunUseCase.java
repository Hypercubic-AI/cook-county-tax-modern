
package org.cookcounty.tax.domain.port.in;

import java.util.Optional;

import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunResponse;

public interface AssessedValuePreparationRunUseCase {

    Optional<AssessedValuePreparationRunResponse> getAssessedValuePreparationRun(Long id);

    AssessedValuePreparationRunResponse startAssessedValuePreparationRun(AssessedValuePreparationRunRequest request);

}
