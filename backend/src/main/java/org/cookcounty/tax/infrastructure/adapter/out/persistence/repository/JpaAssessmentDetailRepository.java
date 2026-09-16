package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Spring Data access to mutable assessment-detail entities.
public interface JpaAssessmentDetailRepository extends JpaRepository<AssessmentDetailEntity, Long> {

    /// Returns all detail entities in stable source occurrence order.
    List<AssessmentDetailEntity> findAllByOrderByIdAsc();
}
