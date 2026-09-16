package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.AssertTrue;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.regex.Pattern;

/// Launch controls for one increment run over the shared assessment and reference populations.
///
/// Missing components remain nullable until Bean Validation reports all structural errors. A
/// successful launch pins the business date and time for all nine ordered steps.
///
/// @param annualEqualizationFactor five-digit factor whose implied scale is four decimal places
/// @param businessDate business date used by every step
/// @param businessTime 24-hour business time used by every step
/// @param idempotencyKey caller-selected replay key for these launch controls
/// @param processingYear two-digit year used by selection and Class 288 calculations
/// @param reassessmentControl twelve-digit value containing the current year and range controls
/// @param reportingYear four-digit year printed and appended by the town-report step
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EifdTifIncrementRunRequest(
        @Nullable String annualEqualizationFactor,
        @Nullable LocalDate businessDate,
        @Nullable String businessTime,
        @Nullable String idempotencyKey,
        @Nullable String processingYear,
        @Nullable String reassessmentControl,
        @Nullable String reportingYear) {

    private static final Pattern BUSINESS_TIME_PATTERN =
            Pattern.compile("^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");
    private static final Pattern REASSESSMENT_CONTROL_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PROCESSING_YEAR_PATTERN = Pattern.compile("^[0-9]{2}$");
    private static final Pattern REPORTING_YEAR_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final Pattern ANNUAL_EQUALIZATION_FACTOR_PATTERN = Pattern.compile("^[0-9]{5}$");

    /// Reports whether the pinned date is present.
    @AssertTrue(message = "businessDate is required.")
    @JsonIgnore
    public boolean isBusinessDatePresent() {
        return businessDate != null;
    }

    /// Reports whether the pinned time is present.
    @AssertTrue(message = "businessTime is required.")
    @JsonIgnore
    public boolean isBusinessTimePresent() {
        return businessTime != null;
    }

    /// Reports whether the pinned time uses 24-hour hours, minutes, and seconds.
    @AssertTrue(message = "businessTime must use HH:mm:ss in 24-hour time.")
    @JsonIgnore
    public boolean isBusinessTimeFormatted() {
        return businessTime == null || BUSINESS_TIME_PATTERN.matcher(businessTime).matches();
    }

    /// Reports whether the replay key contains a non-blank value.
    @AssertTrue(message = "idempotencyKey must not be blank.")
    @JsonIgnore
    public boolean isIdempotencyKeyPresent() {
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }

    /// Reports whether the reassessment control is present.
    @AssertTrue(message = "reassessmentControl is required.")
    @JsonIgnore
    public boolean isReassessmentControlPresent() {
        return reassessmentControl != null;
    }

    /// Reports whether the reassessment control contains twelve decimal digits.
    @AssertTrue(message = "reassessmentControl must contain exactly 12 decimal digits.")
    @JsonIgnore
    public boolean isReassessmentControlFormatted() {
        return reassessmentControl == null
                || REASSESSMENT_CONTROL_PATTERN.matcher(reassessmentControl).matches();
    }

    /// Reports whether the processing year is present.
    @AssertTrue(message = "processingYear is required.")
    @JsonIgnore
    public boolean isProcessingYearPresent() {
        return processingYear != null;
    }

    /// Reports whether the processing year contains two decimal digits.
    @AssertTrue(message = "processingYear must contain exactly two decimal digits.")
    @JsonIgnore
    public boolean isProcessingYearFormatted() {
        return processingYear == null || PROCESSING_YEAR_PATTERN.matcher(processingYear).matches();
    }

    /// Reports whether the reporting year is present.
    @AssertTrue(message = "reportingYear is required.")
    @JsonIgnore
    public boolean isReportingYearPresent() {
        return reportingYear != null;
    }

    /// Reports whether the reporting year contains four decimal digits.
    @AssertTrue(message = "reportingYear must contain exactly four decimal digits.")
    @JsonIgnore
    public boolean isReportingYearFormatted() {
        return reportingYear == null || REPORTING_YEAR_PATTERN.matcher(reportingYear).matches();
    }

    /// Reports whether the equalization factor is present.
    @AssertTrue(message = "annualEqualizationFactor is required.")
    @JsonIgnore
    public boolean isAnnualEqualizationFactorPresent() {
        return annualEqualizationFactor != null;
    }

    /// Reports whether the equalization factor contains five decimal digits.
    @AssertTrue(message = "annualEqualizationFactor must contain exactly five decimal digits.")
    @JsonIgnore
    public boolean isAnnualEqualizationFactorFormatted() {
        return annualEqualizationFactor == null
                || ANNUAL_EQUALIZATION_FACTOR_PATTERN.matcher(annualEqualizationFactor).matches();
    }

    /// Reports whether the implied-scale factor is greater than zero.
    @AssertTrue(message = "annualEqualizationFactor must be greater than 00000.")
    @JsonIgnore
    public boolean isAnnualEqualizationFactorPositive() {
        return annualEqualizationFactor == null || !"00000".equals(annualEqualizationFactor);
    }
}
