
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.util.List;

// GENERATED-IMPORTS:end

public class PropertyTaxExemptionsOutput {

    // GENERATED-FIELDS:start

    private String kind;

    private String name;

    private String publicationStatus;

    private Long recordCount;

    private List<String> ruleIds;

    // GENERATED-FIELDS:end

    public PropertyTaxExemptionsOutput() {}

    // GENERATED-ACCESSORS:start

    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPublicationStatus() {
        return publicationStatus;
    }
    public void setPublicationStatus(String publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public Long getRecordCount() {
        return recordCount;
    }
    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public List<String> getRuleIds() {
        return ruleIds;
    }
    public void setRuleIds(List<String> ruleIds) {
        this.ruleIds = ruleIds;
    }

    // GENERATED-ACCESSORS:end
}
