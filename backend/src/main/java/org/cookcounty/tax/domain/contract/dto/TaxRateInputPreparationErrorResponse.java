package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/// Describes an expected request, replay, capacity, or lookup failure.
///
/// @param error stable machine-readable failure code
/// @param message safe caller-readable explanation
/// @param ruleId accepted business-rule identity when the failure has rule context, otherwise
///   `null`
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxRateInputPreparationErrorResponse(
        String error, String message, @Nullable String ruleId)
        implements TaxRateInputPreparationApiResponse {}
