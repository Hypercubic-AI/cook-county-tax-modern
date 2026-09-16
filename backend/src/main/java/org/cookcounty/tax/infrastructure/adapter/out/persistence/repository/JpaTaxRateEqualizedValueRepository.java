package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import java.util.List;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateEqualizedValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

public interface JpaTaxRateEqualizedValueRepository
        extends JpaRepository<TaxRateEqualizedValueEntity, Integer> {

    // GENERATED-METHODS:start
    // GENERATED-METHODS:end

    List<TaxRateEqualizedValueEntity> findAllByOrderBySourceOrderAsc();
}
