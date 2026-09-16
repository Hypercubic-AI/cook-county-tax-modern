package org.cookcounty.tax.domain.contract.dto;

/// Closed HTTP response alternatives for property-tax-exemption run operations.
public sealed interface PropertyTaxExemptionsApiResponse
        permits PropertyTaxExemptionsRunResponse, PropertyTaxExemptionsErrorResponse {}
