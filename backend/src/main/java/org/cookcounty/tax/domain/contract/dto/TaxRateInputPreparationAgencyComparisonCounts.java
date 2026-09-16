package org.cookcounty.tax.domain.contract.dto;

/// Reconciles prior-year and current-year agency comparison processing.
///
/// @param annexSegments current-only agency segments produced
/// @param comparisonRecordsWritten matched-division comparison records produced, including
///   zero-segment records
/// @param currentOnlyDivisions current-year division occurrences reported without a prior-year
///   match
/// @param currentRecordsRead current-year agency-assessment records examined
/// @param disconnectSegments prior-only agency segments produced
/// @param priorOnlyDivisions prior-year division occurrences reported without a current-year match
/// @param priorRecordsRead prior-year agency-assessment records examined
public record TaxRateInputPreparationAgencyComparisonCounts(
        Integer annexSegments,
        Integer comparisonRecordsWritten,
        Integer currentOnlyDivisions,
        Integer currentRecordsRead,
        Integer disconnectSegments,
        Integer priorOnlyDivisions,
        Integer priorRecordsRead) {}
