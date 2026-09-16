package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/// Reports the durable snapshot of an asynchronous tax-rate input preparation run.
///
/// Counts are operation totals from completed stages. Stage reconciliation remains authoritative. A
/// failed run can retain outputs and counts from changes made before the failure. A persisted
/// snapshot survives restart, but work that was active at restart does not resume.
///
/// @param businessDate date used by every stage and report header
/// @param businessTime 24-hour time used by every stage
/// @param id positive server-generated run identity
/// @param messages ordered business and worker diagnostics available in this snapshot
/// @param outputs logical server-managed outputs available in this snapshot
/// @param reconciliation stage totals, or `null` before processing produces a result
/// @param recordsRead sum of reported read operations, or `null` before a result is available
/// @param recordsRejected unmatched or omitted inputs, or `null` before a result is available
/// @param recordsUpdated successful stamp and rewrite operations, or `null` before a result is
///   available
/// @param recordsWritten successful output, comparison, and insert operations, or `null` before a
///   result is available
/// @param returnCode final batch return code, or `null` while the run is queued or active
/// @param status `QUEUED`, `RUNNING`, `COMPLETED`, or `FAILED`
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxRateInputPreparationRunResponse(
        LocalDate businessDate,
        String businessTime,
        Long id,
        List<TaxRateInputPreparationMessage> messages,
        List<TaxRateInputPreparationOutput> outputs,
        @Nullable TaxRateInputPreparationReconciliation reconciliation,
        @Nullable Integer recordsRead,
        @Nullable Integer recordsRejected,
        @Nullable Integer recordsUpdated,
        @Nullable Integer recordsWritten,
        @Nullable Integer returnCode,
        String status)
        implements TaxRateInputPreparationApiResponse {
    /// Prevents callers from changing the message and output sequences after construction.
    public TaxRateInputPreparationRunResponse {
        messages = List.copyOf(messages);
        outputs = List.copyOf(outputs);
    }
}
