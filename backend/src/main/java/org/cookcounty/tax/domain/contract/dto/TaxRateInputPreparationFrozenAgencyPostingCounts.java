package org.cookcounty.tax.domain.contract.dto;

/// Reconciles reads and persistent frozen-agency posting operations.
///
/// Successful mutations remain when a later operation fails. Rewrite and insert totals count
/// operations, not unique tax-code and agency keys.
///
/// @param comparisonRecordsRead comparison records examined
/// @param currentRecordsRead current-year assessment records examined for annex postings
/// @param insertOperations successful new-key writes
/// @param priorRecordsRead prior-year assessment records examined for disconnect postings
/// @param rewriteOperations successful existing-key updates
public record TaxRateInputPreparationFrozenAgencyPostingCounts(
        Integer comparisonRecordsRead,
        Integer currentRecordsRead,
        Integer insertOperations,
        Integer priorRecordsRead,
        Integer rewriteOperations) {}
