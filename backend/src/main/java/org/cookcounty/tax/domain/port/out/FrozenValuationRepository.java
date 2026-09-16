package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.FrozenValuation;

import java.util.List;
import java.util.Optional;

/// Persists immutable frozen valuation snapshots.
public interface FrozenValuationRepository {

    /// Returns all snapshots in stable persistence order.
    List<FrozenValuation> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated persistence identifier, when present.
    Optional<FrozenValuation> findById(Long id);

    /// Persists the complete snapshot and returns its hydrated identifier and version.
    FrozenValuation save(FrozenValuation frozenValuation);

    /// Deletes the snapshot for the generated persistence identifier, when present.
    void deleteById(Long id);
}
