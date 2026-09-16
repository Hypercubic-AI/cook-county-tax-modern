
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationReconciliation {

    // GENERATED-FIELDS:start

    private TaxRateInputPreparationAgencyAttachmentCounts agencyAttachment;

    private TaxRateInputPreparationAgencyComparisonCounts agencyComparison;

    private TaxRateInputPreparationDivisionStampingCounts divisionStamping;

    private TaxRateInputPreparationFrozenAgencyPostingCounts frozenAgencyPosting;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationReconciliation() {}

    // GENERATED-ACCESSORS:start

    public TaxRateInputPreparationAgencyAttachmentCounts getAgencyAttachment() {
        return agencyAttachment;
    }
    public void setAgencyAttachment(TaxRateInputPreparationAgencyAttachmentCounts agencyAttachment) {
        this.agencyAttachment = agencyAttachment;
    }

    public TaxRateInputPreparationAgencyComparisonCounts getAgencyComparison() {
        return agencyComparison;
    }
    public void setAgencyComparison(TaxRateInputPreparationAgencyComparisonCounts agencyComparison) {
        this.agencyComparison = agencyComparison;
    }

    public TaxRateInputPreparationDivisionStampingCounts getDivisionStamping() {
        return divisionStamping;
    }
    public void setDivisionStamping(TaxRateInputPreparationDivisionStampingCounts divisionStamping) {
        this.divisionStamping = divisionStamping;
    }

    public TaxRateInputPreparationFrozenAgencyPostingCounts getFrozenAgencyPosting() {
        return frozenAgencyPosting;
    }
    public void setFrozenAgencyPosting(TaxRateInputPreparationFrozenAgencyPostingCounts frozenAgencyPosting) {
        this.frozenAgencyPosting = frozenAgencyPosting;
    }

    // GENERATED-ACCESSORS:end
}
