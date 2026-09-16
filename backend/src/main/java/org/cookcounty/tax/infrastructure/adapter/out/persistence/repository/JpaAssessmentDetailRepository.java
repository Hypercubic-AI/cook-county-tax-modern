
package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

public interface JpaAssessmentDetailRepository extends JpaRepository<AssessmentDetailEntity, Long> {
    // GENERATED-METHODS:start

    // GENERATED-METHODS:end
    List<AssessmentDetailEntity> findAllByOrderByIdAsc();
}
