package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/// Reports one ordered diagnostic from a batch stage or worker boundary.
///
/// @param code stable machine-readable diagnostic code
/// @param ruleId accepted business-rule identity, or `null` for operational and I/O diagnostics
/// @param severity `INFO`, `WARNING`, or `ERROR`
/// @param text safe caller-readable diagnostic text
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxRateInputPreparationMessage(
        String code, @Nullable String ruleId, String severity, String text) {}
