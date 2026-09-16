
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationDivisionStampingCounts {

    // GENERATED-FIELDS:start

    private Integer divisionRecordsRead;

    private Integer divisionRecordsUnmatched;

    private Integer equalizedValueRecordsRead;

    private Integer outputRecordsWritten;

    private Integer recordsStamped;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationDivisionStampingCounts() {}

    // GENERATED-ACCESSORS:start

    public Integer getDivisionRecordsRead() {
        return divisionRecordsRead;
    }
    public void setDivisionRecordsRead(Integer divisionRecordsRead) {
        this.divisionRecordsRead = divisionRecordsRead;
    }

    public Integer getDivisionRecordsUnmatched() {
        return divisionRecordsUnmatched;
    }
    public void setDivisionRecordsUnmatched(Integer divisionRecordsUnmatched) {
        this.divisionRecordsUnmatched = divisionRecordsUnmatched;
    }

    public Integer getEqualizedValueRecordsRead() {
        return equalizedValueRecordsRead;
    }
    public void setEqualizedValueRecordsRead(Integer equalizedValueRecordsRead) {
        this.equalizedValueRecordsRead = equalizedValueRecordsRead;
    }

    public Integer getOutputRecordsWritten() {
        return outputRecordsWritten;
    }
    public void setOutputRecordsWritten(Integer outputRecordsWritten) {
        this.outputRecordsWritten = outputRecordsWritten;
    }

    public Integer getRecordsStamped() {
        return recordsStamped;
    }
    public void setRecordsStamped(Integer recordsStamped) {
        this.recordsStamped = recordsStamped;
    }

    // GENERATED-ACCESSORS:end
}
