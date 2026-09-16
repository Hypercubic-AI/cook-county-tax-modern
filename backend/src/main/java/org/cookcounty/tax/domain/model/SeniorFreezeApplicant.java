package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// One Senior Freeze applicant and the facts used to determine eligibility.
///
/// The income values use exact decimal dollars. The date fields retain their source layouts.
/// Maintained input includes nonzero values that are not valid calendar dates. Converting those
/// fields to `LocalDate` would reject states that the source accepts.
///
/// @param id Generated database identity. It is absent before the first successful persistence
///   operation.
/// @param version Optimistic-lock version. It is absent before persistence hydrates the record.
/// @param age applicant age in whole years
/// @param applicantAddress Applicant address retained from the applicant or mailing record.
/// @param applicantCity Applicant city retained from the applicant or mailing record.
/// @param applicantFirstName Applicant first name retained from the applicant or mailing record.
/// @param applicantLastName Applicant last name retained from the applicant or mailing record.
/// @param applicantMiddleInitial Applicant middle initial retained from the applicant or mailing
///   record.
/// @param applicantOldName Applicant old name retained from the applicant or mailing record.
/// @param applicantState Applicant state retained from the applicant or mailing record.
/// @param applicantTitle Applicant title retained from the applicant or mailing record.
/// @param applicantZipCode canonical nine-digit postal identifier
/// @param baseYear year selected for the frozen base value
/// @param baseYearEligibleEqualizedValue eligible base-year equalized valuation in whole dollars
/// @param baseYearIndicator maintained code that identifies base-year treatment
/// @param batchNumber processing batch identifier
/// @param birthDate birth-date key in the `MMDDCCYY` character layout; accepted noncalendar text
///   remains unchanged
/// @param civilServiceBenefits Civil-service benefit portion of household income in dollars with
///   two fractional digits.
/// @param cooperativeSeniorShares number of cooperative shares claimed by the applicant
/// @param denialDate denial date in numeric `CCYYMMDD` layout; zero is absent and other source
///   values remain unchanged
/// @param firstApplicationDate first received date in numeric `CCYYMMDD` layout; zero is absent and
///   other source values remain unchanged
/// @param homeownerBaseYear year selected for the applicant's homeowner base
/// @param homeownerBaseYearAssessedValue Applicant homeowner base-year assessed valuation in whole
///   dollars.
/// @param homeownerBaseYearEqualizationFactor Applicant homeowner base-year factor with four
///   fractional digits.
/// @param homeownerBaseYearEqualizedValue Applicant homeowner base-year equalized valuation in
///   whole dollars.
/// @param homeownerEligibilityIndicator maintained code for homeowner eligibility
/// @param homeownerStatus maintained homeowner disposition code
/// @param homesteadBatchNumber homestead processing batch identifier
/// @param homesteadPercentShares homestead share percentage with three fractional digits
/// @param homesteadShares number of homestead shares
/// @param homesteadStatus maintained homestead disposition code
/// @param homesteadYearApplied year associated with the homestead application
/// @param interestIncome Interest portion of household income in dollars with two fractional
///   digits.
/// @param lastApplicationDate latest received date in numeric `CCYYMMDD` layout; zero is absent and
///   other source values remain unchanged
/// @param lifeCareFacilityIndicator maintained code for life-care-facility status
/// @param maintenanceIndicator maintained record-maintenance code
/// @param nameMaintenanceIndicator maintained name-maintenance code
/// @param netCapitalGain Net-capital-gain portion of household income in dollars with two
///   fractional digits.
/// @param netRentalIncome Net-rental portion of household income in dollars with two fractional
///   digits.
/// @param noIncomeIndicator maintained code that declares no household income
/// @param notarizedIndicator maintained code that records notarization
/// @param otherBenefits Other-benefit portion of household income in dollars with two fractional
///   digits.
/// @param otherIncome Other portion of household income in dollars with two fractional digits.
/// @param percentSeniorShares Applicant share as a decimal fraction with six fractional digits.
/// @param phoneNumber canonical ten-digit telephone identifier
/// @param publicAid Public-aid portion of household income in dollars with two fractional digits.
/// @param qualificationDate qualification date in numeric `CCYYMMDD` layout; zero is absent and
///   other source values remain unchanged
/// @param railroadBenefits Railroad-benefit portion of household income in dollars with two
///   fractional digits.
/// @param returnedDate returned date in numeric `CCYYMMDD` layout; zero is absent and other source
///   values remain unchanged
/// @param seniorFreezePercent Senior Freeze percentage with one fractional digit.
/// @param seniorFreezeStatus maintained application disposition code
/// @param signedIndicator maintained code that records the applicant signature
/// @param socialSecurityIncome Social Security portion of household income in dollars with two
///   fractional digits.
/// @param socialSecurityNumber canonical 11-digit source identifier
/// @param totalIncome Applicant household income total in dollars with two fractional digits.
/// @param veteransBenefits Veterans-benefit portion of household income in dollars with two
///   fractional digits.
/// @param wages Wage portion of household income in dollars with two fractional digits.
public record SeniorFreezeApplicant(
        @Nullable Long id,
        @Nullable Long version,
        Integer age,
        String applicantAddress,
        String applicantCity,
        String applicantFirstName,
        String applicantLastName,
        String applicantMiddleInitial,
        String applicantOldName,
        String applicantState,
        String applicantTitle,
        String applicantZipCode,
        Integer baseYear,
        BigDecimal baseYearEligibleEqualizedValue,
        String baseYearIndicator,
        Integer batchNumber,
        String birthDate,
        BigDecimal civilServiceBenefits,
        Integer cooperativeSeniorShares,
        Integer denialDate,
        Integer firstApplicationDate,
        Integer homeownerBaseYear,
        BigDecimal homeownerBaseYearAssessedValue,
        BigDecimal homeownerBaseYearEqualizationFactor,
        BigDecimal homeownerBaseYearEqualizedValue,
        Integer homeownerEligibilityIndicator,
        String homeownerStatus,
        Integer homesteadBatchNumber,
        BigDecimal homesteadPercentShares,
        Integer homesteadShares,
        String homesteadStatus,
        Integer homesteadYearApplied,
        BigDecimal interestIncome,
        Integer lastApplicationDate,
        String lifeCareFacilityIndicator,
        Integer maintenanceIndicator,
        Integer nameMaintenanceIndicator,
        BigDecimal netCapitalGain,
        BigDecimal netRentalIncome,
        String noIncomeIndicator,
        String notarizedIndicator,
        BigDecimal otherBenefits,
        BigDecimal otherIncome,
        BigDecimal percentSeniorShares,
        String phoneNumber,
        BigDecimal publicAid,
        Integer qualificationDate,
        BigDecimal railroadBenefits,
        Integer returnedDate,
        BigDecimal seniorFreezePercent,
        String seniorFreezeStatus,
        String signedIndicator,
        BigDecimal socialSecurityIncome,
        String socialSecurityNumber,
        BigDecimal totalIncome,
        BigDecimal veteransBenefits,
        BigDecimal wages) {
    /// Normalizes identifiers and exact decimal amounts without interpreting historical raw date
    /// fields.
    ///
    /// @throws IllegalArgumentException if an identifier is empty, contains nondigits, or exceeds
    ///   its width
    /// @throws ArithmeticException if a decimal exceeds its precision or requires rounding
    public SeniorFreezeApplicant {
        applicantZipCode =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        applicantZipCode, 9, "applicantZipCode");
        baseYearEligibleEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        baseYearEligibleEqualizedValue, 9, 0, "baseYearEligibleEqualizedValue");
        civilServiceBenefits =
                PropertyTaxExemptionsNumericBoundary.exact(
                        civilServiceBenefits, 9, 2, "civilServiceBenefits");
        homeownerBaseYearAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        homeownerBaseYearAssessedValue, 9, 0, "homeownerBaseYearAssessedValue");
        homeownerBaseYearEqualizationFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        homeownerBaseYearEqualizationFactor,
                        5,
                        4,
                        "homeownerBaseYearEqualizationFactor");
        homeownerBaseYearEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        homeownerBaseYearEqualizedValue, 9, 0, "homeownerBaseYearEqualizedValue");
        homesteadPercentShares =
                PropertyTaxExemptionsNumericBoundary.exact(
                        homesteadPercentShares, 6, 3, "homesteadPercentShares");
        interestIncome =
                PropertyTaxExemptionsNumericBoundary.exact(interestIncome, 9, 2, "interestIncome");
        netCapitalGain =
                PropertyTaxExemptionsNumericBoundary.exact(netCapitalGain, 9, 2, "netCapitalGain");
        netRentalIncome =
                PropertyTaxExemptionsNumericBoundary.exact(
                        netRentalIncome, 9, 2, "netRentalIncome");
        otherBenefits =
                PropertyTaxExemptionsNumericBoundary.exact(otherBenefits, 9, 2, "otherBenefits");
        otherIncome = PropertyTaxExemptionsNumericBoundary.exact(otherIncome, 9, 2, "otherIncome");
        percentSeniorShares =
                PropertyTaxExemptionsNumericBoundary.exact(
                        percentSeniorShares, 6, 6, "percentSeniorShares");
        phoneNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        phoneNumber, 10, "phoneNumber");
        publicAid = PropertyTaxExemptionsNumericBoundary.exact(publicAid, 9, 2, "publicAid");
        railroadBenefits =
                PropertyTaxExemptionsNumericBoundary.exact(
                        railroadBenefits, 9, 2, "railroadBenefits");
        seniorFreezePercent =
                PropertyTaxExemptionsNumericBoundary.exact(
                        seniorFreezePercent, 2, 1, "seniorFreezePercent");
        socialSecurityIncome =
                PropertyTaxExemptionsNumericBoundary.exact(
                        socialSecurityIncome, 9, 2, "socialSecurityIncome");
        socialSecurityNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        socialSecurityNumber, 11, "socialSecurityNumber");
        totalIncome = PropertyTaxExemptionsNumericBoundary.exact(totalIncome, 9, 2, "totalIncome");
        veteransBenefits =
                PropertyTaxExemptionsNumericBoundary.exact(
                        veteransBenefits, 9, 2, "veteransBenefits");
        wages = PropertyTaxExemptionsNumericBoundary.exact(wages, 9, 2, "wages");
    }
}
