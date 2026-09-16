
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.util.List;

// GENERATED-IMPORTS:end

public class PropertyTaxExemptionsReconciliationOutcome {

    // GENERATED-FIELDS:start

    private String message;

    private String name;

    private Long recordsMatched;

    private Long recordsRead;

    private Long recordsRejected;

    private Long recordsWritten;

    private List<String> ruleIds;

    private String status;

    // GENERATED-FIELDS:end

    public PropertyTaxExemptionsReconciliationOutcome() {}

    // GENERATED-ACCESSORS:start

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Long getRecordsMatched() {
        return recordsMatched;
    }
    public void setRecordsMatched(Long recordsMatched) {
        this.recordsMatched = recordsMatched;
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

    public Long getRecordsWritten() {
        return recordsWritten;
    }
    public void setRecordsWritten(Long recordsWritten) {
        this.recordsWritten = recordsWritten;
    }

    public List<String> getRuleIds() {
        return ruleIds;
    }
    public void setRuleIds(List<String> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // GENERATED-ACCESSORS:end
}
