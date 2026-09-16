package org.cookcounty.tax.domain.contract.dto;

import org.jspecify.annotations.Nullable;

/// A run-level information, warning, or error message.
///
/// @param message caller-safe description of the observed outcome
/// @param ruleId accepted business-rule identifier when the message has one
/// @param severity `INFO`, `WARNING`, or `ERROR`
public record PropertyTaxExemptionsMessage(
        String message, @Nullable String ruleId, String severity) {}
