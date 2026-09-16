
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.time.LocalDate;

import java.util.List;

// GENERATED-IMPORTS:end

public class PropertyTaxExemptionsRunResponse {

    // GENERATED-FIELDS:start

    private LocalDate businessDate;

    private String businessTime;

    private String homeownerProcessingVariant;

    private Long id;

    private List<PropertyTaxExemptionsMessage> messages;

    private List<PropertyTaxExemptionsOutput> outputs;

    private List<PropertyTaxExemptionsReconciliationOutcome> reconciliations;

    private Long recordsRead;

    private Long recordsRejected;

    private Long recordsUpdated;

    private Long recordsWritten;

    private List<PropertyTaxExemptionsRejectionOutcome> rejections;

    private Integer returnCode;

    private List<PropertyTaxExemptionsRuleOutcome> ruleOutcomes;

    private String status;

    // GENERATED-FIELDS:end

    public PropertyTaxExemptionsRunResponse() {}

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

    public String getHomeownerProcessingVariant() {
        return homeownerProcessingVariant;
    }
    public void setHomeownerProcessingVariant(String homeownerProcessingVariant) {
        this.homeownerProcessingVariant = homeownerProcessingVariant;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public List<PropertyTaxExemptionsMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<PropertyTaxExemptionsMessage> messages) {
        this.messages = messages;
    }

    public List<PropertyTaxExemptionsOutput> getOutputs() {
        return outputs;
    }
    public void setOutputs(List<PropertyTaxExemptionsOutput> outputs) {
        this.outputs = outputs;
    }

    public List<PropertyTaxExemptionsReconciliationOutcome> getReconciliations() {
        return reconciliations;
    }
    public void setReconciliations(List<PropertyTaxExemptionsReconciliationOutcome> reconciliations) {
        this.reconciliations = reconciliations;
    }

    public Long getRecordsRead() {
        return recordsRead;
    }
    public void setRecordsRead(Long recordsRead) {
        this.recordsRead = recordsRead;
    }

    public Long getRecordsRejected() {
        return recordsRejected;
    }
    public void setRecordsRejected(Long recordsRejected) {
        this.recordsRejected = recordsRejected;
    }

    public Long getRecordsUpdated() {
        return recordsUpdated;
    }
    public void setRecordsUpdated(Long recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public Long getRecordsWritten() {
        return recordsWritten;
    }
    public void setRecordsWritten(Long recordsWritten) {
        this.recordsWritten = recordsWritten;
    }

    public List<PropertyTaxExemptionsRejectionOutcome> getRejections() {
        return rejections;
    }
    public void setRejections(List<PropertyTaxExemptionsRejectionOutcome> rejections) {
        this.rejections = rejections;
    }

    public Integer getReturnCode() {
        return returnCode;
    }
    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public List<PropertyTaxExemptionsRuleOutcome> getRuleOutcomes() {
        return ruleOutcomes;
    }
    public void setRuleOutcomes(List<PropertyTaxExemptionsRuleOutcome> ruleOutcomes) {
        this.ruleOutcomes = ruleOutcomes;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // GENERATED-ACCESSORS:end
}
