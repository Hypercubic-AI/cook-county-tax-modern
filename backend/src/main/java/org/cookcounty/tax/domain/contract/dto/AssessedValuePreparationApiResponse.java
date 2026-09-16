package org.cookcounty.tax.domain.contract.dto;

/// Closed response alternatives for assessed-value preparation requests.
public sealed interface AssessedValuePreparationApiResponse
        permits AssessedValuePreparationRunResponse, AssessedValuePreparationErrorResponse {}
