
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class AssessedValuePreparationStageResult {

    // GENERATED-FIELDS:start

    private Boolean outputPublished;

    private Boolean partialOutput;

    private Integer recordsRead;

    private Integer recordsRejected;

    private Integer recordsUpdated;

    private Integer recordsWritten;

    private Integer returnCode;

    private String stage;

    private String status;

    // GENERATED-FIELDS:end

    public AssessedValuePreparationStageResult() {}

    // GENERATED-ACCESSORS:start

    public Boolean getOutputPublished() {
        return outputPublished;
    }
    public void setOutputPublished(Boolean outputPublished) {
        this.outputPublished = outputPublished;
    }

    public Boolean getPartialOutput() {
        return partialOutput;
    }
    public void setPartialOutput(Boolean partialOutput) {
        this.partialOutput = partialOutput;
    }

    public Integer getRecordsRead() {
        return recordsRead;
    }
    public void setRecordsRead(Integer recordsRead) {
        this.recordsRead = recordsRead;
    }

    public Integer getRecordsRejected() {
        return recordsRejected;
    }
    public void setRecordsRejected(Integer recordsRejected) {
        this.recordsRejected = recordsRejected;
    }

    public Integer getRecordsUpdated() {
        return recordsUpdated;
    }
    public void setRecordsUpdated(Integer recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public Integer getRecordsWritten() {
        return recordsWritten;
    }
    public void setRecordsWritten(Integer recordsWritten) {
        this.recordsWritten = recordsWritten;
    }

    public Integer getReturnCode() {
        return returnCode;
    }
    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getStage() {
        return stage;
    }
    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // GENERATED-ACCESSORS:end
}
