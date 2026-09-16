package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

/// Groups the available stage-level operation totals for one run snapshot.
///
/// @param agencyAttachment attachment-stage totals, or `null` when that stage has no result
/// @param agencyComparison comparison-stage totals, or `null` when that stage has no result
/// @param divisionStamping stamping-stage totals, or `null` when that stage has no result
/// @param frozenAgencyPosting posting-stage totals, or `null` when that stage has no result
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxRateInputPreparationReconciliation(
        @Nullable TaxRateInputPreparationAgencyAttachmentCounts agencyAttachment,
        @Nullable TaxRateInputPreparationAgencyComparisonCounts agencyComparison,
        @Nullable TaxRateInputPreparationDivisionStampingCounts divisionStamping,
        @Nullable TaxRateInputPreparationFrozenAgencyPostingCounts frozenAgencyPosting) {}
