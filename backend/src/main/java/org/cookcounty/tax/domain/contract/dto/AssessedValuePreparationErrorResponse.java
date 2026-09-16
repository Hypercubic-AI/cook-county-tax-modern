package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/// Expected request or run lookup failure returned by the assessed-value controller.
///
/// @param error stable machine-readable failure code
/// @param message safe explanation for the caller
/// @param ruleId business-rule identifier when the failure belongs to one rule, otherwise `null`
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessedValuePreparationErrorResponse(
        String error, String message, @Nullable String ruleId)
        implements AssessedValuePreparationApiResponse {}
