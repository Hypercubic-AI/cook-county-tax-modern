package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.SeniorFreezeMaster;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable senior freeze master retained without normalization snapshots.
///
/// Save operations return the database-hydrated replacement, including generated identity and
/// version state.
public interface SeniorFreezeMasterRepository {
    /// Returns all snapshots in ascending generated-identity order.
    List<SeniorFreezeMaster> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated database identity, if it exists.
    Optional<SeniorFreezeMaster> findById(Long id);

    /// Inserts or replaces one complete snapshot in the caller's transaction.
    SeniorFreezeMaster save(SeniorFreezeMaster seniorFreezeMaster);

    /// Deletes the row with the generated database identity in the caller's transaction.
    void deleteById(Long id);
}
