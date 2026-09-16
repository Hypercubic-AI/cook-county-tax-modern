package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/// Observable outcome of one ordered assessed-value stage.
///
/// @param outputPublished whether the stage published at least one output record
/// @param partialOutput whether the stage stopped or continued with an ordering failure
/// @param recordsRead source records inspected by the stage
/// @param recordsRejected records that caused a recoverable stage failure
/// @param recordsUpdated records whose persisted business state changed
/// @param recordsWritten records published to the stage handoff
/// @param returnCode stage return code, or `null` when the stage did not start
/// @param stage stable stage name
/// @param status `COMPLETED`, `FAILED`, or `NOT_STARTED`
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessedValuePreparationStageResult(
        Boolean outputPublished,
        Boolean partialOutput,
        Integer recordsRead,
        Integer recordsRejected,
        Integer recordsUpdated,
        Integer recordsWritten,
        @Nullable Integer returnCode,
        String stage,
        String status) {}
