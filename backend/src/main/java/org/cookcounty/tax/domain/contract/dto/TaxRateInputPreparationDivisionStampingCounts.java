package org.cookcounty.tax.domain.contract.dto;

/// Reconciles source-ordered equalized-value and division stamping operations.
///
/// @param divisionRecordsRead division reference records examined
/// @param divisionRecordsUnmatched displayed compatibility total that remains zero and does not
///   prove that all divisions matched
/// @param equalizedValueRecordsRead equalized-value records examined
/// @param outputRecordsWritten division-aware records produced
/// @param recordsStamped output records assigned a matched division or property fallback
public record TaxRateInputPreparationDivisionStampingCounts(
        Integer divisionRecordsRead,
        Integer divisionRecordsUnmatched,
        Integer equalizedValueRecordsRead,
        Integer outputRecordsWritten,
        Integer recordsStamped) {}
