package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// Typed HTTP failure for a launch or run lookup.
///
/// @param error stable failure code
/// @param message safe caller-visible explanation
/// @param ruleId governing business-rule identifier when the failure is rule-linked
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EifdTifIncrementErrorResponse(String error, String message, @Nullable String ruleId)
        implements EifdTifIncrementApiResponse {
    /// Requires the stable code and caller-visible explanation.
    public EifdTifIncrementErrorResponse {
        Objects.requireNonNull(error, "error");
        Objects.requireNonNull(message, "message");
    }
}
