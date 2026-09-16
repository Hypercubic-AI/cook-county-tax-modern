package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AssessmentParcel;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable assessment parcels with optimistic locking.
public interface AssessmentParcelRepository {

    /// Returns every parcel in ascending generated-identity order.
    List<AssessmentParcel> findAllInPersistenceOrder();

    /// Returns a parcel by generated identity when it exists.
    Optional<AssessmentParcel> findById(Long id);

    /// Saves every parcel field and returns assigned identity and version state.
    AssessmentParcel save(AssessmentParcel assessmentParcel);

    /// Deletes the parcel with the supplied generated identity.
    void deleteById(Long id);

    /// Returns every parcel in deterministic source ingestion order.
    List<AssessmentParcel> findAllInInputOrder();
}
