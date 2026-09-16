
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationOutput {

    // GENERATED-FIELDS:start

    private Boolean available;

    private String kind;

    private String name;

    private Integer recordCount;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationOutput() {}

    // GENERATED-ACCESSORS:start

    public Boolean getAvailable() {
        return available;
    }
    public void setAvailable(Boolean available) {
        this.available = available;
    }

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

    public Integer getRecordCount() {
        return recordCount;
    }
    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    // GENERATED-ACCESSORS:end
}
