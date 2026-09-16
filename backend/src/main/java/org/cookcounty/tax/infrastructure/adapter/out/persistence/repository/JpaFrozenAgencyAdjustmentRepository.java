
package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.FrozenAgencyAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end
import java.util.Optional;


public interface JpaFrozenAgencyAdjustmentRepository extends JpaRepository<FrozenAgencyAdjustmentEntity, Long> {
    // GENERATED-METHODS:start

    // GENERATED-METHODS:end
    Optional<FrozenAgencyAdjustmentEntity> findByTaxCodeAndAgencyNumber(
            String taxCode, String agencyNumber);
}
