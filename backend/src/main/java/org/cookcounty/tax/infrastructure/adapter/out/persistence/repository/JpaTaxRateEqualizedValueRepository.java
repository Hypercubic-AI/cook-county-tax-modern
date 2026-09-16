package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateEqualizedValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Reads maintained equalized-value rows through the persistence provider.
public interface JpaTaxRateEqualizedValueRepository
        extends JpaRepository<TaxRateEqualizedValueEntity, Integer> {
    /// Returns every row in ascending source order for deterministic batch processing.
    List<TaxRateEqualizedValueEntity> findAllByOrderBySourceOrderAsc();
}
