package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateDivisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Reads maintained parcel-to-division rows through the persistence provider.
public interface JpaTaxRateDivisionRepository
        extends JpaRepository<TaxRateDivisionEntity, Integer> {
    /// Returns every row in ascending source order for deterministic batch processing.
    List<TaxRateDivisionEntity> findAllByOrderBySourceOrderAsc();
}
