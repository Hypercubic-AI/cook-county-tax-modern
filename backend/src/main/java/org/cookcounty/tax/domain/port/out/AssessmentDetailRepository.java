package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AssessmentDetail;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable assessment details with optimistic locking.
public interface AssessmentDetailRepository {

    /// Returns every detail in ascending generated-identity order.
    List<AssessmentDetail> findAllInPersistenceOrder();

    /// Returns a detail by generated identity when it exists.
    Optional<AssessmentDetail> findById(Long id);

    /// Saves every detail field and returns assigned identity and version state.
    AssessmentDetail save(AssessmentDetail assessmentDetail);

    /// Deletes the detail with the supplied generated identity.
    void deleteById(Long id);

    /// Returns every detail in deterministic source ingestion order.
    List<AssessmentDetail> findAllInInputOrder();
}
