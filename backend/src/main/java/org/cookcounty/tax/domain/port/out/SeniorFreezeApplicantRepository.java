package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;

import java.util.List;
import java.util.Optional;

/// Persists complete immutable senior freeze applicant retained without normalization snapshots.
///
/// Save operations return the database-hydrated replacement, including generated identity and
/// version state.
public interface SeniorFreezeApplicantRepository {
    /// Returns all snapshots in ascending generated-identity order.
    List<SeniorFreezeApplicant> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated database identity, if it exists.
    Optional<SeniorFreezeApplicant> findById(Long id);

    /// Inserts or replaces one complete snapshot in the caller's transaction.
    SeniorFreezeApplicant save(SeniorFreezeApplicant seniorFreezeApplicant);

    /// Deletes the row with the generated database identity in the caller's transaction.
    void deleteById(Long id);
}
