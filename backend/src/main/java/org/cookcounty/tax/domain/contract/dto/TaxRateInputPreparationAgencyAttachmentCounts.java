package org.cookcounty.tax.domain.contract.dto;

/// Reconciles tax-code attachment reads with produced and unmatched assessment records.
///
/// @param assessmentRecordsRead divided assessment records examined
/// @param assessmentRecordsUnmatched tax-code misses omitted from agency-assessment output
/// @param assessmentRecordsWritten agency-assessment records produced
/// @param normalCompletionBalanced whether written plus unmatched equals read; only normal
///   processing guarantees this equality
public record TaxRateInputPreparationAgencyAttachmentCounts(
        Integer assessmentRecordsRead,
        Integer assessmentRecordsUnmatched,
        Integer assessmentRecordsWritten,
        Boolean normalCompletionBalanced) {}
