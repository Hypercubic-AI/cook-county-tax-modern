
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.time.LocalDate;

// GENERATED-IMPORTS:end
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class AssessedValuePreparationRunRequest {

    private static final Pattern BUSINESS_TIME_PATTERN =
            Pattern.compile("^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");
    private static final Pattern PROCESS_YEAR_PATTERN = Pattern.compile("^[0-9]{2}$");

    // GENERATED-FIELDS:start

    private LocalDate businessDate;

    private String businessTime;

    private String idempotencyKey;

    private String processYear;

    // GENERATED-FIELDS:end

    public AssessedValuePreparationRunRequest() {}

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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getProcessYear() {
        return processYear;
    }
    public void setProcessYear(String processYear) {
        this.processYear = processYear;
    }

    // GENERATED-ACCESSORS:end

    @AssertTrue(message = "businessDate is required.")
    @JsonIgnore
    public boolean isBusinessDatePresent() {
        return businessDate != null;
    }

    @AssertTrue(message = "businessTime is required.")
    @JsonIgnore
    public boolean isBusinessTimePresent() {
        return businessTime != null;
    }

    @AssertTrue(message = "businessTime must use HH:mm:ss in 24-hour time.")
    @JsonIgnore
    public boolean isBusinessTimeFormatted() {
        return businessTime == null || BUSINESS_TIME_PATTERN.matcher(businessTime).matches();
    }

    @AssertTrue(message = "idempotencyKey must not be blank.")
    @JsonIgnore
    public boolean isIdempotencyKeyPresent() {
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }

    @AssertTrue(message = "idempotencyKey must contain at most 128 characters.")
    @JsonIgnore
    public boolean isIdempotencyKeyWithinMaximumLength() {
        return idempotencyKey == null || idempotencyKey.length() <= 128;
    }

    @AssertTrue(message = "processYear is required.")
    @JsonIgnore
    public boolean isProcessYearPresent() {
        return processYear != null;
    }

    @AssertTrue(message = "processYear must contain exactly two decimal digits.")
    @JsonIgnore
    public boolean isProcessYearFormatted() {
        return processYear == null || PROCESS_YEAR_PATTERN.matcher(processYear).matches();
    }
}
