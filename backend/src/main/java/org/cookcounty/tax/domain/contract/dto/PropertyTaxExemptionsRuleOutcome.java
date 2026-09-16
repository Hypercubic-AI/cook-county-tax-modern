package org.cookcounty.tax.domain.contract.dto;

import org.jspecify.annotations.Nullable;

/// Final disposition of one accepted business rule for a run.
///
/// @param message optional explanation when the disposition is not self-explanatory
/// @param outcome `APPLIED`, `NOT_APPLICABLE`, `REJECTED`, `WARNING`, or `FAILED`
/// @param recordsAffected number of records to which the disposition applies
/// @param ruleId stable accepted business-rule identifier
public record PropertyTaxExemptionsRuleOutcome(
        @Nullable String message, String outcome, Long recordsAffected, String ruleId) {}
