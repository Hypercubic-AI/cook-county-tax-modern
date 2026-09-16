package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/// One recoverable assessed-value rule observation.
///
/// @param code stable machine-readable observation code
/// @param message safe explanation for an operator
/// @param recordKey ordered parcel key, or `null` for a run-wide observation
/// @param ruleId accepted business-rule identifier, or `null` for an operational failure
/// @param severity `WARNING` or `ERROR` as determined by the stage
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessedValuePreparationMessage(
        String code,
        String message,
        @Nullable String recordKey,
        @Nullable String ruleId,
        String severity) {}
