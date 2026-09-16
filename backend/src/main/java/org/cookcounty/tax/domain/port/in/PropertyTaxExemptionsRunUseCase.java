
// GENERATED-STUB
package org.cookcounty.tax.domain.port.in;

import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PropertyTaxExemptionsRunUseCase {

    Optional<PropertyTaxExemptionsRunResponse> getPropertyTaxExemptionsRun(Long id);

    PropertyTaxExemptionsRunResponse startPropertyTaxExemptionsRun(PropertyTaxExemptionsRunRequest request);

}
