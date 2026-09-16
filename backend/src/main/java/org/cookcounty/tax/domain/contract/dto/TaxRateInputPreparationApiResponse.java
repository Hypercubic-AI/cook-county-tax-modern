package org.cookcounty.tax.domain.contract.dto;

/// Closed HTTP response alternatives for tax-rate input preparation operations.
///
/// Controllers exhaustively map a run snapshot or a typed error body. The interface does not hide
/// expected failures behind exceptions.
public sealed interface TaxRateInputPreparationApiResponse
        permits TaxRateInputPreparationRunResponse, TaxRateInputPreparationErrorResponse {}
