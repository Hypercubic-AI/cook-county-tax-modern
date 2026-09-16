package org.cookcounty.tax.domain.port.in;

import java.util.Optional;

import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunResponse;

public interface EifdTifIncrementRunUseCase {

    Optional<EifdTifIncrementRunResponse> getEifdTifIncrementRun(Long id);

    BatchRunStartResult<EifdTifIncrementRunResponse> startEifdTifIncrementRun(
            EifdTifIncrementRunRequest request);
}
