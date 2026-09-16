
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationAgencyComparisonCounts {

    // GENERATED-FIELDS:start

    private Integer annexSegments;

    private Integer comparisonRecordsWritten;

    private Integer currentOnlyDivisions;

    private Integer currentRecordsRead;

    private Integer disconnectSegments;

    private Integer priorOnlyDivisions;

    private Integer priorRecordsRead;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationAgencyComparisonCounts() {}

    // GENERATED-ACCESSORS:start

    public Integer getAnnexSegments() {
        return annexSegments;
    }
    public void setAnnexSegments(Integer annexSegments) {
        this.annexSegments = annexSegments;
    }

    public Integer getComparisonRecordsWritten() {
        return comparisonRecordsWritten;
    }
    public void setComparisonRecordsWritten(Integer comparisonRecordsWritten) {
        this.comparisonRecordsWritten = comparisonRecordsWritten;
    }

    public Integer getCurrentOnlyDivisions() {
        return currentOnlyDivisions;
    }
    public void setCurrentOnlyDivisions(Integer currentOnlyDivisions) {
        this.currentOnlyDivisions = currentOnlyDivisions;
    }

    public Integer getCurrentRecordsRead() {
        return currentRecordsRead;
    }
    public void setCurrentRecordsRead(Integer currentRecordsRead) {
        this.currentRecordsRead = currentRecordsRead;
    }

    public Integer getDisconnectSegments() {
        return disconnectSegments;
    }
    public void setDisconnectSegments(Integer disconnectSegments) {
        this.disconnectSegments = disconnectSegments;
    }

    public Integer getPriorOnlyDivisions() {
        return priorOnlyDivisions;
    }
    public void setPriorOnlyDivisions(Integer priorOnlyDivisions) {
        this.priorOnlyDivisions = priorOnlyDivisions;
    }

    public Integer getPriorRecordsRead() {
        return priorRecordsRead;
    }
    public void setPriorRecordsRead(Integer priorRecordsRead) {
        this.priorRecordsRead = priorRecordsRead;
    }

    // GENERATED-ACCESSORS:end
}
