package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;

import java.util.List;
import java.util.Optional;

/// Persists immutable agency equalized valuation snapshots.
public interface AgencyEqualizedValuationRepository {

    /// Returns all snapshots in stable persistence order.
    List<AgencyEqualizedValuation> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated persistence identifier, when present.
    Optional<AgencyEqualizedValuation> findById(Long id);

    /// Persists the complete snapshot and returns its hydrated identifier and version.
    AgencyEqualizedValuation save(AgencyEqualizedValuation agencyEqualizedValuation);

    /// Deletes the snapshot for the generated persistence identifier, when present.
    void deleteById(Long id);
}
