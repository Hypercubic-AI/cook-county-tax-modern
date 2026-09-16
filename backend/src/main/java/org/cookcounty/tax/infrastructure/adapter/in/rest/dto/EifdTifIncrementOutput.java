
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EifdTifIncrementOutput {

    // GENERATED-FIELDS:start

    private Integer generation;

    private String name;

    private Integer recordCount;

    // GENERATED-FIELDS:end

    public EifdTifIncrementOutput() {}

    // GENERATED-ACCESSORS:start

    public Integer getGeneration() {
        return generation;
    }
    public void setGeneration(Integer generation) {
        this.generation = generation;
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
