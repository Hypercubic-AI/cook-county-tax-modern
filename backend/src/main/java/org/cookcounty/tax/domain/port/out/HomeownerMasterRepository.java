package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.HomeownerMaster;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable homeowner master retained without normalization snapshots.
///
/// Save operations return the database-hydrated replacement, including generated identity and
/// version state.
public interface HomeownerMasterRepository {
    /// Returns all snapshots in ascending generated-identity order.
    List<HomeownerMaster> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated database identity, if it exists.
    Optional<HomeownerMaster> findById(Long id);

    /// Inserts or replaces one complete snapshot in the caller's transaction.
    HomeownerMaster save(HomeownerMaster homeownerMaster);

    /// Deletes the row with the generated database identity in the caller's transaction.
    void deleteById(Long id);

    /// Returns homeowner records in ascending property-number order with generated identity as the
    /// tie-breaker.
    List<HomeownerMaster> findAllInPropertyOrder();
}
