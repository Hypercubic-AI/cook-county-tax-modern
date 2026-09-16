
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class AssessedValuePreparationMessage {

    // GENERATED-FIELDS:start

    private String code;

    private String message;

    private String recordKey;

    private String ruleId;

    private String severity;

    // GENERATED-FIELDS:end

    public AssessedValuePreparationMessage() {}

    // GENERATED-ACCESSORS:start

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecordKey() {
        return recordKey;
    }
    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public String getRuleId() {
        return ruleId;
    }
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    // GENERATED-ACCESSORS:end
}
