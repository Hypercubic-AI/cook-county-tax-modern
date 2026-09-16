package org.cookcounty.tax.domain.contract.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/// Immutable operational snapshot of one property-tax-exemption batch run.
///
/// Counters, logical outputs, and outcomes are absent while the run is queued or running. A
/// terminal snapshot supplies the available final values. The snapshot does not promise restart of
/// an interrupted worker.
///
/// @param businessDate date pinned for every stage
/// @param businessTime 24-hour `HH:mm:ss` time pinned for every stage
/// @param homeownerProcessingVariant selected alternative homeowner eligibility policy
/// @param id generated run identity
/// @param messages run-level messages when processing produced them
/// @param outputs logical publications when processing reached publication planning
/// @param reconciliations stage reconciliation outcomes when available
/// @param recordsRead final count of input records examined
/// @param recordsRejected final count of rejected input records
/// @param recordsUpdated final count of persisted records replaced
/// @param recordsWritten final count of logical and persisted records written
/// @param rejections record-level dispositions when available
/// @param returnCode final batch return code when the run is terminal
/// @param ruleOutcomes final dispositions of accepted rules when available
/// @param status `QUEUED`, `RUNNING`, `COMPLETED`, or `FAILED`
public record PropertyTaxExemptionsRunResponse(
        LocalDate businessDate,
        String businessTime,
        String homeownerProcessingVariant,
        Long id,
        @Nullable List<PropertyTaxExemptionsMessage> messages,
        @Nullable List<PropertyTaxExemptionsOutput> outputs,
        @Nullable List<PropertyTaxExemptionsReconciliationOutcome> reconciliations,
        @Nullable Long recordsRead,
        @Nullable Long recordsRejected,
        @Nullable Long recordsUpdated,
        @Nullable Long recordsWritten,
        @Nullable List<PropertyTaxExemptionsRejectionOutcome> rejections,
        @Nullable Integer returnCode,
        @Nullable List<PropertyTaxExemptionsRuleOutcome> ruleOutcomes,
        String status)
        implements PropertyTaxExemptionsApiResponse {
    /// Copies available outcome lists while preserving null for outcomes not yet available.
    ///
    /// @throws NullPointerException if a supplied list contains a null element
    public PropertyTaxExemptionsRunResponse {
        messages = messages == null ? null : List.copyOf(messages);
        outputs = outputs == null ? null : List.copyOf(outputs);
        reconciliations = reconciliations == null ? null : List.copyOf(reconciliations);
        rejections = rejections == null ? null : List.copyOf(rejections);
        ruleOutcomes = ruleOutcomes == null ? null : List.copyOf(ruleOutcomes);
    }
}
