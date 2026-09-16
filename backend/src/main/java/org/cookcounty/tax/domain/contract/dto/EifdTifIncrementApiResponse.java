package org.cookcounty.tax.domain.contract.dto;

/// Closed HTTP response alternatives for increment launch and observation operations.
public sealed interface EifdTifIncrementApiResponse
        permits EifdTifIncrementRunResponse, EifdTifIncrementErrorResponse {}
