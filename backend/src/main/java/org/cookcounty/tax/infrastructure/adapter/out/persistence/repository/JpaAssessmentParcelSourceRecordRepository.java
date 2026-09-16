package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import java.util.List;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentParcelSourceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

public interface JpaAssessmentParcelSourceRecordRepository
        extends JpaRepository<AssessmentParcelSourceRecordEntity, Long> {

    // GENERATED-METHODS:start
    // GENERATED-METHODS:end

    List<AssessmentParcelSourceRecordEntity> findAllByOrderBySourceOrderAsc();
}
