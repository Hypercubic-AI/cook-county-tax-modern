package org.cookcounty.tax.domain.contract.dto;

import org.jspecify.annotations.Nullable;

import java.util.List;

/// Counts that reconcile one processing stage or logical publication.
///
/// @param message optional explanation for the reconciliation result
/// @param name logical stage or publication name
/// @param recordsMatched number of input records that matched a counterpart
/// @param recordsRead number of input records examined
/// @param recordsRejected number of records rejected by the stage
/// @param recordsWritten number of logical output records produced
/// @param ruleIds nonempty accepted rules that govern the reconciliation
/// @param status final reconciliation disposition
public record PropertyTaxExemptionsReconciliationOutcome(
        @Nullable String message,
        String name,
        Long recordsMatched,
        Long recordsRead,
        Long recordsRejected,
        Long recordsWritten,
        List<String> ruleIds,
        String status) {
    /// Copies rule identities so the reconciliation does not retain a caller-owned mutable list.
    ///
    /// @throws NullPointerException if the rule list or one of its elements is null
    public PropertyTaxExemptionsReconciliationOutcome {
        ruleIds = List.copyOf(ruleIds);
    }
}
