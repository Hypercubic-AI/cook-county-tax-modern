
package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentParcelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

public interface JpaAssessmentParcelRepository extends JpaRepository<AssessmentParcelEntity, Long> {
    // GENERATED-METHODS:start

    // GENERATED-METHODS:end
    List<AssessmentParcelEntity> findAllByOrderByIdAsc();
}
