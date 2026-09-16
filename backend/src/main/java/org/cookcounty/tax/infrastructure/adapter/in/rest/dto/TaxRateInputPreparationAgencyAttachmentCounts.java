
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationAgencyAttachmentCounts {

    // GENERATED-FIELDS:start

    private Integer assessmentRecordsRead;

    private Integer assessmentRecordsUnmatched;

    private Integer assessmentRecordsWritten;

    private Boolean normalCompletionBalanced;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationAgencyAttachmentCounts() {}

    // GENERATED-ACCESSORS:start

    public Integer getAssessmentRecordsRead() {
        return assessmentRecordsRead;
    }
    public void setAssessmentRecordsRead(Integer assessmentRecordsRead) {
        this.assessmentRecordsRead = assessmentRecordsRead;
    }

    public Integer getAssessmentRecordsUnmatched() {
        return assessmentRecordsUnmatched;
    }
    public void setAssessmentRecordsUnmatched(Integer assessmentRecordsUnmatched) {
        this.assessmentRecordsUnmatched = assessmentRecordsUnmatched;
    }

    public Integer getAssessmentRecordsWritten() {
        return assessmentRecordsWritten;
    }
    public void setAssessmentRecordsWritten(Integer assessmentRecordsWritten) {
        this.assessmentRecordsWritten = assessmentRecordsWritten;
    }

    public Boolean getNormalCompletionBalanced() {
        return normalCompletionBalanced;
    }
    public void setNormalCompletionBalanced(Boolean normalCompletionBalanced) {
        this.normalCompletionBalanced = normalCompletionBalanced;
    }

    // GENERATED-ACCESSORS:end
}
