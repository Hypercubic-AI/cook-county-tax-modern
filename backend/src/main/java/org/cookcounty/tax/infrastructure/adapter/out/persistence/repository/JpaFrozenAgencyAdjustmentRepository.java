package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.FrozenAgencyAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end
import java.util.Optional;

/// Spring Data access to versioned frozen agency adjustments.
public interface JpaFrozenAgencyAdjustmentRepository
        extends JpaRepository<FrozenAgencyAdjustmentEntity, Long> {
    // GENERATED-METHODS:start

    // GENERATED-METHODS:end
    /// Returns the adjustment for one tax-code and agency pair, when present.
    Optional<FrozenAgencyAdjustmentEntity> findByTaxCodeAndAgencyNumber(
            String taxCode, String agencyNumber);
}
