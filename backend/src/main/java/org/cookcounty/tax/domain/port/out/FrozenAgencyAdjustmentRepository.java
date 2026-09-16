
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface FrozenAgencyAdjustmentRepository {
    // GENERATED-METHODS:start
    Page<FrozenAgencyAdjustment> findAll(Pageable pageable);
    Optional<FrozenAgencyAdjustment> findById(Long id);
    FrozenAgencyAdjustment save(FrozenAgencyAdjustment frozenAgencyAdjustment);
    void deleteById(Long id);
    // GENERATED-METHODS:end
    Optional<FrozenAgencyAdjustment> findByTaxCodeAndAgencyNumber(
            String taxCode, String agencyNumber);
}
