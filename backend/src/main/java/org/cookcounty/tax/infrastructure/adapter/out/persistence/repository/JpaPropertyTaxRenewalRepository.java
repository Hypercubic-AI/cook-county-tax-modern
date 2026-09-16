package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.PropertyTaxRenewalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Provides deterministic source-order reads for maintained renewal input.
public interface JpaPropertyTaxRenewalRepository
        extends JpaRepository<PropertyTaxRenewalEntity, Integer> {
    /// Returns renewal rows in recorded source order without sorting or deduplicating their
    /// property keys.
    List<PropertyTaxRenewalEntity> findAllByOrderBySourceOrderAsc();
}
