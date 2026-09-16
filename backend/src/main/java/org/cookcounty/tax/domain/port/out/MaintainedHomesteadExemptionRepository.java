package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.MaintainedHomesteadExemption;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable maintained homestead exemption retained without normalization
/// snapshots.
///
/// Save operations return the database-hydrated replacement, including generated identity and
/// version state.
public interface MaintainedHomesteadExemptionRepository {
    /// Returns all snapshots in ascending generated-identity order.
    List<MaintainedHomesteadExemption> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated database identity, if it exists.
    Optional<MaintainedHomesteadExemption> findById(Long id);

    /// Inserts or replaces one complete snapshot in the caller's transaction.
    MaintainedHomesteadExemption save(MaintainedHomesteadExemption maintainedHomesteadExemption);

    /// Deletes the row with the generated database identity in the caller's transaction.
    void deleteById(Long id);
}
