package org.cookcounty.tax.domain.contract.dto;

/// Identifies one logical output produced or retained by a run.
///
/// Names are stable logical names, not local paths. Record counts include retained partial output
/// after a later failure.
///
/// @param available whether the deployment can retrieve this server-managed output
/// @param kind `DIVIDED_VALUE`, `AGENCY_ASSESSMENT`, `ANNEX_DISCONNECT`, `FROZEN_AGENCY`, or
///   `REPORT`
/// @param name stable logical output name
/// @param recordCount number of records currently represented by the output
public record TaxRateInputPreparationOutput(
        Boolean available, String kind, String name, Integer recordCount) {}
