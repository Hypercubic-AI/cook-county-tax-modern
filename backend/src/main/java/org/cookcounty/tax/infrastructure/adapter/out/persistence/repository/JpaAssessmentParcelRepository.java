package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentParcelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Spring Data access to mutable assessment-parcel entities.
public interface JpaAssessmentParcelRepository extends JpaRepository<AssessmentParcelEntity, Long> {

    /// Returns all parcel entities in stable source ingestion order.
    List<AssessmentParcelEntity> findAllByOrderByIdAsc();
}
