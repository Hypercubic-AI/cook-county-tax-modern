package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/// Current snapshot of an accepted increment run.
///
/// Activity counts and the return code are absent before the run reaches a terminal state. Outputs
/// and messages preserve partial products when a later step fails.
///
/// @param businessDate pinned date shared by all steps
/// @param businessTime pinned 24-hour time shared by all steps
/// @param id generated run identifier
/// @param messages ordered informational or rule-linked messages observed so far
/// @param outputs ordered logical outputs and their record counts
/// @param recordsRead total records read when terminal
/// @param recordsRejected total records omitted or rejected when terminal
/// @param recordsUpdated total shared-live records replaced when terminal
/// @param recordsWritten total transient and persisted records written when terminal
/// @param returnCode terminal batch return code
/// @param status queued, running, completed, or failed state
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EifdTifIncrementRunResponse(
        LocalDate businessDate,
        String businessTime,
        Long id,
        List<EifdTifIncrementMessage> messages,
        List<EifdTifIncrementOutput> outputs,
        @Nullable Integer recordsRead,
        @Nullable Integer recordsRejected,
        @Nullable Integer recordsUpdated,
        @Nullable Integer recordsWritten,
        @Nullable Integer returnCode,
        String status)
        implements EifdTifIncrementApiResponse {

    /// Copies list inputs so later caller changes cannot alter a persisted run snapshot.
    public EifdTifIncrementRunResponse {
        Objects.requireNonNull(businessDate, "businessDate");
        Objects.requireNonNull(businessTime, "businessTime");
        Objects.requireNonNull(id, "id");
        messages = List.copyOf(messages);
        outputs = List.copyOf(outputs);
        Objects.requireNonNull(status, "status");
    }
}
