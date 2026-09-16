
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface AssessmentParcelRepository {
    // GENERATED-METHODS:start
    Page<AssessmentParcel> findAll(Pageable pageable);
    Optional<AssessmentParcel> findById(Long id);
    AssessmentParcel save(AssessmentParcel assessmentParcel);
    void deleteById(Long id);
    // GENERATED-METHODS:end
    List<AssessmentParcel> findAllInInputOrder();
}
