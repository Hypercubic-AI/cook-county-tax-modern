package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import java.util.List;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateDivisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

public interface JpaTaxRateDivisionRepository
        extends JpaRepository<TaxRateDivisionEntity, Integer> {

    // GENERATED-METHODS:start
    // GENERATED-METHODS:end

    List<TaxRateDivisionEntity> findAllByOrderBySourceOrderAsc();
}
