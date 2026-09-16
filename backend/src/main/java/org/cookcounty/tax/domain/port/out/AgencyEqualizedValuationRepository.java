
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface AgencyEqualizedValuationRepository {
    // GENERATED-METHODS:start
    Page<AgencyEqualizedValuation> findAll(Pageable pageable);
    Optional<AgencyEqualizedValuation> findById(Long id);
    AgencyEqualizedValuation save(AgencyEqualizedValuation agencyEqualizedValuation);
    void deleteById(Long id);
    // GENERATED-METHODS:end
}
