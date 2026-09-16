package org.cookcounty.tax.domain.contract.dto;

import java.util.List;

/// One logical dataset, report, or staging publication.
///
/// @param kind logical publication category, never a client filesystem type
/// @param name customer-visible logical output name
/// @param publicationStatus `PUBLISHED`, `EMPTY`, or `WITHHELD`
/// @param recordCount number of logical records in the publication
/// @param ruleIds accepted rules that produced the publication when cataloged
public record PropertyTaxExemptionsOutput(
        String kind,
        String name,
        String publicationStatus,
        Long recordCount,
        List<String> ruleIds) {
    /// Copies rule identities so later changes to the caller's list cannot alter the publication.
    ///
    /// @throws NullPointerException if the rule list or one of its elements is null
    public PropertyTaxExemptionsOutput {
        ruleIds = List.copyOf(ruleIds);
    }
}
