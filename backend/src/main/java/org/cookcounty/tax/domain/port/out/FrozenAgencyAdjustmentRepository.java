package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;

import java.util.List;
import java.util.Optional;

/// Persists immutable frozen agency adjustment snapshots.
public interface FrozenAgencyAdjustmentRepository {

    /// Returns all snapshots in stable persistence order.
    List<FrozenAgencyAdjustment> findAllInPersistenceOrder();

    /// Returns the snapshot for a generated persistence identifier, when present.
    Optional<FrozenAgencyAdjustment> findById(Long id);

    /// Persists the complete snapshot and returns its hydrated identifier and version.
    FrozenAgencyAdjustment save(FrozenAgencyAdjustment frozenAgencyAdjustment);

    /// Deletes the snapshot for the generated persistence identifier, when present.
    void deleteById(Long id);

    /// Returns the adjustment for the tax-code and agency pair, when present.
    Optional<FrozenAgencyAdjustment> findByTaxCodeAndAgencyNumber(
            String taxCode, String agencyNumber);
}
