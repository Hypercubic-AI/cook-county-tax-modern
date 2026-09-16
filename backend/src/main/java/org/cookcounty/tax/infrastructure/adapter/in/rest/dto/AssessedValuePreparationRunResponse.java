
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.time.LocalDate;

import java.util.List;

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class AssessedValuePreparationRunResponse {

    // GENERATED-FIELDS:start

    private LocalDate businessDate;

    private String businessTime;

    private Long id;

    private List<AssessedValuePreparationMessage> messages;

    private List<AssessedValuePreparationOutput> outputs;

    private Boolean partialOutput;

    private String processYear;

    private Integer recordsRead;

    private Integer recordsRejected;

    private Integer recordsUpdated;

    private Integer recordsWritten;

    private Integer returnCode;

    private List<AssessedValuePreparationStageResult> stages;

    private String status;

    // GENERATED-FIELDS:end

    public AssessedValuePreparationRunResponse() {}

    // GENERATED-ACCESSORS:start

    public LocalDate getBusinessDate() {
        return businessDate;
    }
    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getBusinessTime() {
        return businessTime;
    }
    public void setBusinessTime(String businessTime) {
        this.businessTime = businessTime;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public List<AssessedValuePreparationMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<AssessedValuePreparationMessage> messages) {
        this.messages = messages;
    }

    public List<AssessedValuePreparationOutput> getOutputs() {
        return outputs;
    }
    public void setOutputs(List<AssessedValuePreparationOutput> outputs) {
        this.outputs = outputs;
    }

    public Boolean getPartialOutput() {
        return partialOutput;
    }
    public void setPartialOutput(Boolean partialOutput) {
        this.partialOutput = partialOutput;
    }

    public String getProcessYear() {
        return processYear;
    }
    public void setProcessYear(String processYear) {
        this.processYear = processYear;
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

    public List<AssessedValuePreparationStageResult> getStages() {
        return stages;
    }
    public void setStages(List<AssessedValuePreparationStageResult> stages) {
        this.stages = stages;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // GENERATED-ACCESSORS:end
}
