package org.cookcounty.tax.domain.contract.dto;

import org.jspecify.annotations.Nullable;

/// A rejected, bypassed, warned, or partially applied input outcome.
///
/// @param message caller-safe explanation of the disposition
/// @param outcome `REJECTED`, `BYPASSED`, `PARTIALLY_APPLIED`, or `WARNING`
/// @param recordKey business key when the source row has one
/// @param ruleId accepted rule that determined the disposition
/// @param source logical server-owned input or processing stage
public record PropertyTaxExemptionsRejectionOutcome(
        String message, String outcome, @Nullable String recordKey, String ruleId, String source) {}
