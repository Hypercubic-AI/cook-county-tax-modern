package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.HomeownerExemption;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable homeowner exemption retained without normalization snapshots.
///
/// Save operations return the database-hydrated replacement, including generated identity and
/// version state.
public interface HomeownerExemptionRepository {
    /// Returns all snapshots in ascending generated-identity order.
    List<HomeownerExemption> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated database identity, if it exists.
    Optional<HomeownerExemption> findById(Long id);

    /// Inserts or replaces one complete snapshot in the caller's transaction.
    HomeownerExemption save(HomeownerExemption homeownerExemption);

    /// Deletes the row with the generated database identity in the caller's transaction.
    void deleteById(Long id);
}
