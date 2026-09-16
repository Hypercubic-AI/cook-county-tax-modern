
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationFrozenAgencyPostingCounts {

    // GENERATED-FIELDS:start

    private Integer comparisonRecordsRead;

    private Integer currentRecordsRead;

    private Integer insertOperations;

    private Integer priorRecordsRead;

    private Integer rewriteOperations;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationFrozenAgencyPostingCounts() {}

    // GENERATED-ACCESSORS:start

    public Integer getComparisonRecordsRead() {
        return comparisonRecordsRead;
    }
    public void setComparisonRecordsRead(Integer comparisonRecordsRead) {
        this.comparisonRecordsRead = comparisonRecordsRead;
    }

    public Integer getCurrentRecordsRead() {
        return currentRecordsRead;
    }
    public void setCurrentRecordsRead(Integer currentRecordsRead) {
        this.currentRecordsRead = currentRecordsRead;
    }

    public Integer getInsertOperations() {
        return insertOperations;
    }
    public void setInsertOperations(Integer insertOperations) {
        this.insertOperations = insertOperations;
    }

    public Integer getPriorRecordsRead() {
        return priorRecordsRead;
    }
    public void setPriorRecordsRead(Integer priorRecordsRead) {
        this.priorRecordsRead = priorRecordsRead;
    }

    public Integer getRewriteOperations() {
        return rewriteOperations;
    }
    public void setRewriteOperations(Integer rewriteOperations) {
        this.rewriteOperations = rewriteOperations;
    }

    // GENERATED-ACCESSORS:end
}
