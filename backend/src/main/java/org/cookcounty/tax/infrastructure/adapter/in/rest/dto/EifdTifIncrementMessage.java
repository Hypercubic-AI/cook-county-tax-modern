
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EifdTifIncrementMessage {

    // GENERATED-FIELDS:start

    private String ruleId;

    private String severity;

    private String text;

    // GENERATED-FIELDS:end

    public EifdTifIncrementMessage() {}

    // GENERATED-ACCESSORS:start

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

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    // GENERATED-ACCESSORS:end
}
