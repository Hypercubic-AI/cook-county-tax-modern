package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// One ordered observation from increment processing.
///
/// @param ruleId governing business-rule identifier when the observation is rule-linked
/// @param severity informational or error severity
/// @param text safe caller-visible description of the observation
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EifdTifIncrementMessage(@Nullable String ruleId, String severity, String text) {
    /// Requires caller-visible severity and text.
    public EifdTifIncrementMessage {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(text, "text");
    }
}
