package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/// Persisted snapshot of an assessed-value preparation run.
///
/// Counts and return state are absent while a run is queued or running. Output, message, and stage
/// collections are always immutable. The snapshot survives process restart. An interrupted worker
/// does not resume from an intermediate stage.
///
/// @param businessDate date printed in source-compatible reports
/// @param businessTime accepted 24-hour run time
/// @param id generated run identity
/// @param messages recoverable rule and operational observations
/// @param outputs published artifact descriptors
/// @param partialOutput whether an ordering failure limited output
/// @param processYear two-digit Type-5 process year
/// @param recordsRead total records read across started stages
/// @param recordsRejected total records rejected across started stages
/// @param recordsUpdated total records whose business state changed
/// @param recordsWritten total records published across started stages
/// @param returnCode overall completion code
/// @param stages ordered stage outcomes
/// @param status `QUEUED`, `RUNNING`, `COMPLETED`, or `FAILED`
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessedValuePreparationRunResponse(
        LocalDate businessDate,
        String businessTime,
        Long id,
        List<AssessedValuePreparationMessage> messages,
        List<AssessedValuePreparationOutput> outputs,
        @Nullable Boolean partialOutput,
        String processYear,
        @Nullable Integer recordsRead,
        @Nullable Integer recordsRejected,
        @Nullable Integer recordsUpdated,
        @Nullable Integer recordsWritten,
        @Nullable Integer returnCode,
        List<AssessedValuePreparationStageResult> stages,
        String status)
        implements AssessedValuePreparationApiResponse {

    /// Defensively copies every returned collection.
    public AssessedValuePreparationRunResponse {
        messages = List.copyOf(messages);
        outputs = List.copyOf(outputs);
        stages = List.copyOf(stages);
    }
}
