
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.time.LocalDate;

// GENERATED-IMPORTS:end
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EifdTifIncrementRunRequest {

    private static final Pattern BUSINESS_TIME_PATTERN =
            Pattern.compile("^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");
    private static final Pattern REASSESSMENT_CONTROL_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PROCESSING_YEAR_PATTERN = Pattern.compile("^[0-9]{2}$");
    private static final Pattern REPORTING_YEAR_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final Pattern ANNUAL_EQUALIZATION_FACTOR_PATTERN = Pattern.compile("^[0-9]{5}$");

    // GENERATED-FIELDS:start

    private String annualEqualizationFactor;

    private LocalDate businessDate;

    private String businessTime;

    private String idempotencyKey;

    private String processingYear;

    private String reassessmentControl;

    private String reportingYear;

    // GENERATED-FIELDS:end

    public EifdTifIncrementRunRequest() {}

    // GENERATED-ACCESSORS:start

    public String getAnnualEqualizationFactor() {
        return annualEqualizationFactor;
    }
    public void setAnnualEqualizationFactor(String annualEqualizationFactor) {
        this.annualEqualizationFactor = annualEqualizationFactor;
    }

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

    public String getProcessingYear() {
        return processingYear;
    }
    public void setProcessingYear(String processingYear) {
        this.processingYear = processingYear;
    }

    public String getReassessmentControl() {
        return reassessmentControl;
    }
    public void setReassessmentControl(String reassessmentControl) {
        this.reassessmentControl = reassessmentControl;
    }

    public String getReportingYear() {
        return reportingYear;
    }
    public void setReportingYear(String reportingYear) {
        this.reportingYear = reportingYear;
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

    @AssertTrue(message = "reassessmentControl is required.")
    @JsonIgnore
    public boolean isReassessmentControlPresent() {
        return reassessmentControl != null;
    }

    @AssertTrue(message = "reassessmentControl must contain exactly 12 decimal digits.")
    @JsonIgnore
    public boolean isReassessmentControlFormatted() {
        return reassessmentControl == null
                || REASSESSMENT_CONTROL_PATTERN.matcher(reassessmentControl).matches();
    }

    @AssertTrue(message = "processingYear is required.")
    @JsonIgnore
    public boolean isProcessingYearPresent() {
        return processingYear != null;
    }

    @AssertTrue(message = "processingYear must contain exactly two decimal digits.")
    @JsonIgnore
    public boolean isProcessingYearFormatted() {
        return processingYear == null || PROCESSING_YEAR_PATTERN.matcher(processingYear).matches();
    }

    @AssertTrue(message = "reportingYear is required.")
    @JsonIgnore
    public boolean isReportingYearPresent() {
        return reportingYear != null;
    }

    @AssertTrue(message = "reportingYear must contain exactly four decimal digits.")
    @JsonIgnore
    public boolean isReportingYearFormatted() {
        return reportingYear == null || REPORTING_YEAR_PATTERN.matcher(reportingYear).matches();
    }

    @AssertTrue(message = "annualEqualizationFactor is required.")
    @JsonIgnore
    public boolean isAnnualEqualizationFactorPresent() {
        return annualEqualizationFactor != null;
    }

    @AssertTrue(message = "annualEqualizationFactor must contain exactly five decimal digits.")
    @JsonIgnore
    public boolean isAnnualEqualizationFactorFormatted() {
        return annualEqualizationFactor == null
                || ANNUAL_EQUALIZATION_FACTOR_PATTERN.matcher(annualEqualizationFactor).matches();
    }

    @AssertTrue(message = "annualEqualizationFactor must be greater than 00000.")
    @JsonIgnore
    public boolean isAnnualEqualizationFactorPositive() {
        return annualEqualizationFactor == null || !"00000".equals(annualEqualizationFactor);
    }
}
