
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface AssessmentDetailRepository {
    // GENERATED-METHODS:start
    Page<AssessmentDetail> findAll(Pageable pageable);
    Optional<AssessmentDetail> findById(Long id);
    AssessmentDetail save(AssessmentDetail assessmentDetail);
    void deleteById(Long id);
    // GENERATED-METHODS:end
    List<AssessmentDetail> findAllInInputOrder();
}
