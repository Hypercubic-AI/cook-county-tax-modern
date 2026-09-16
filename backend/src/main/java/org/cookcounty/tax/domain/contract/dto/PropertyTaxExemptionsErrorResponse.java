package org.cookcounty.tax.domain.contract.dto;

import org.jspecify.annotations.Nullable;

/// A caller-safe expected failure returned by the property-tax-exemption API.
///
/// @param error stable machine-readable failure category
/// @param message caller-safe explanation without infrastructure diagnostics
/// @param ruleId accepted business-rule identifier when one rule caused the failure
public record PropertyTaxExemptionsErrorResponse(
        String error, String message, @Nullable String ruleId)
        implements PropertyTaxExemptionsApiResponse {}
