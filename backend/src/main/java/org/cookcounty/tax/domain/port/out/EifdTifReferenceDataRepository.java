package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AgencyReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TownReference;

import java.util.Optional;

/// Provides persisted, read-only references used by increment grouping and reporting.
///
/// Absence is an expected lookup result. The consuming step decides whether absence stops, omits,
/// or reports the current group.
public interface EifdTifReferenceDataRepository {

    /// Returns the description for a nine-character agency identifier, when present.
    Optional<AgencyReference> findAgency(String agencyNumber);

    /// Returns the report name for a two-character town code, when present.
    Optional<TownReference> findTown(String townNumber);

    /// Returns the exact tax rate and ordered assigned agency positions, when present.
    Optional<TaxCodeMasterReference> findTaxCodeMaster(String taxCode);
}
