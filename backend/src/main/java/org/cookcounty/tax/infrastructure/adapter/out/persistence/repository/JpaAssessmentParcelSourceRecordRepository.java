package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentParcelSourceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Spring Data access to preserved fixed-width parcel prefixes.
public interface JpaAssessmentParcelSourceRecordRepository
        extends JpaRepository<AssessmentParcelSourceRecordEntity, Long> {

    /// Returns every preserved prefix in deterministic source publication order.
    List<AssessmentParcelSourceRecordEntity> findAllByOrderBySourceOrderAsc();
}
