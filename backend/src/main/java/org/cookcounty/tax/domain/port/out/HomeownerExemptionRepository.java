
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface HomeownerExemptionRepository {
    // GENERATED-METHODS:start
    Page<HomeownerExemption> findAll(Pageable pageable);
    Optional<HomeownerExemption> findById(Long id);
    HomeownerExemption save(HomeownerExemption homeownerExemption);
    void deleteById(Long id);
    // GENERATED-METHODS:end
}
