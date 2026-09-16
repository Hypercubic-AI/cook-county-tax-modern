
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxRateInputPreparationErrorResponse {

    // GENERATED-FIELDS:start

    private String error;

    private String message;

    private String ruleId;

    // GENERATED-FIELDS:end

    public TaxRateInputPreparationErrorResponse() {}

    // GENERATED-ACCESSORS:start

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getRuleId() {
        return ruleId;
    }
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    // GENERATED-ACCESSORS:end
}
