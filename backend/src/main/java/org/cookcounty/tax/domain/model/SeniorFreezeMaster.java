package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// The Senior Freeze valuation and cooperative-share state for one parcel.
///
/// @param id Generated database identity. It is absent before the first successful persistence
///   operation.
/// @param version Optimistic-lock version. It is absent before persistence hydrates the record.
/// @param baseValueManualCalculationIndicator whether base value was calculated manually, using the
///   maintained code
/// @param baseValueNoCalculationIndicator whether base value calculation was withheld, using the
///   maintained code
/// @param baseValueYear four-digit year selected for the frozen base value
/// @param baseValueYearClass property classification in that base year
/// @param baseYearEligibleComputedFullAssessedValue eligible base-year full assessed valuation in
///   whole dollars
/// @param baseYearEqualizedValue base-year equalized valuation in whole dollars
/// @param baseYearFullAssessedValue base-year full assessed valuation in whole dollars
/// @param baseYearTotalEligibleComputedEqualizedValue total eligible base-year computed equalized
///   valuation in whole dollars
/// @param buildingShares number of cooperative building shares
/// @param buildingUnits number of building units
/// @param calculationType maintained calculation-method code
/// @param class288ExpirationAssessedValue assessed valuation in whole dollars when the class
///   expires
/// @param class288ExpirationEqualizedValue equalized valuation in whole dollars when the class
///   expires
/// @param class288OverLimitAssessedValue assessed valuation above the class limit in whole dollars
/// @param class288OverLimitEqualizedValue equalized valuation above the class limit in whole
///   dollars
/// @param currentYearClass current property classification code
/// @param currentYearEligibleComputedAssessedValue current eligible computed assessed valuation in
///   whole dollars
/// @param currentYearEligibleComputedEqualizedValue current eligible computed equalized valuation
///   in whole dollars
/// @param currentYearFarmIndicator whether the current record uses farm treatment, using the
///   maintained code
/// @param currentYearFinalEqualizedValueDifference signed final equalized-value difference in whole
///   dollars
/// @param currentYearFullAssessedValue current full assessed valuation in whole dollars
/// @param currentYearFullEqualizedValue current full equalized valuation in whole dollars
/// @param currentYearNotEligibleAssessedValue current ineligible assessed valuation in whole
///   dollars
/// @param currentYearNotEligibleEqualizedValue current ineligible equalized valuation in whole
///   dollars
/// @param homeownerUnits number of homeowner units
/// @param homesteadUnits number of homestead units
/// @param keyParcelNumber canonical source-width parcel identifier selected from qualifying
///   assessment details
/// @param mailingCity Mailing city retained from the applicant or mailing record.
/// @param mailingDirection Mailing direction retained from the applicant or mailing record.
/// @param mailingHouseNumber house-number characters retained from the mailing record
/// @param mailingState mailing state code
/// @param mailingStreet mailing street name
/// @param mailingSuffix mailing street suffix
/// @param mailingZipCode canonical nine-digit postal identifier
/// @param maintenanceIndicator maintained record-maintenance code
/// @param masterName applicant or owner name retained with the parcel
/// @param occupancyFactor Occupancy percentage used by exemption eligibility.
/// @param originalBaseValueYear base-value year before the current calculation
/// @param originalBaseYearEligibleComputedFullAssessedValue prior eligible base-year full assessed
///   valuation in whole dollars
/// @param originalBaseYearEqualizedValue prior base-year equalized valuation in whole dollars
/// @param originalBaseYearFullAssessedValue prior base-year full assessed valuation in whole
///   dollars
/// @param originalBaseYearTotalEligibleComputedEqualizedValue prior total eligible base-year
///   computed equalized valuation in whole dollars
/// @param originalCurrentYearFinalEqualizedValueDifference prior signed current-year
///   equalized-value difference in whole dollars
/// @param originalManualCalculationIndicator prior manual-calculation code
/// @param propertyProration Eligible parcel share as a decimal fraction with six fractional digits.
/// @param recordCode Record category within the Senior Freeze or homeowner output.
/// @param seniorFreezeShares number of Senior Freeze cooperative shares
/// @param splitCode Property split classification when qualifying detail supplied it.
public record SeniorFreezeMaster(
        @Nullable Long id,
        @Nullable Long version,
        String baseValueManualCalculationIndicator,
        String baseValueNoCalculationIndicator,
        Integer baseValueYear,
        Integer baseValueYearClass,
        BigDecimal baseYearEligibleComputedFullAssessedValue,
        BigDecimal baseYearEqualizedValue,
        BigDecimal baseYearFullAssessedValue,
        BigDecimal baseYearTotalEligibleComputedEqualizedValue,
        Integer buildingShares,
        Integer buildingUnits,
        String calculationType,
        BigDecimal class288ExpirationAssessedValue,
        BigDecimal class288ExpirationEqualizedValue,
        BigDecimal class288OverLimitAssessedValue,
        BigDecimal class288OverLimitEqualizedValue,
        Integer currentYearClass,
        BigDecimal currentYearEligibleComputedAssessedValue,
        BigDecimal currentYearEligibleComputedEqualizedValue,
        String currentYearFarmIndicator,
        BigDecimal currentYearFinalEqualizedValueDifference,
        BigDecimal currentYearFullAssessedValue,
        BigDecimal currentYearFullEqualizedValue,
        BigDecimal currentYearNotEligibleAssessedValue,
        BigDecimal currentYearNotEligibleEqualizedValue,
        Integer homeownerUnits,
        Integer homesteadUnits,
        String keyParcelNumber,
        String mailingCity,
        String mailingDirection,
        String mailingHouseNumber,
        String mailingState,
        String mailingStreet,
        String mailingSuffix,
        String mailingZipCode,
        Integer maintenanceIndicator,
        String masterName,
        BigDecimal occupancyFactor,
        Integer originalBaseValueYear,
        BigDecimal originalBaseYearEligibleComputedFullAssessedValue,
        BigDecimal originalBaseYearEqualizedValue,
        BigDecimal originalBaseYearFullAssessedValue,
        BigDecimal originalBaseYearTotalEligibleComputedEqualizedValue,
        BigDecimal originalCurrentYearFinalEqualizedValueDifference,
        String originalManualCalculationIndicator,
        BigDecimal propertyProration,
        String recordCode,
        Integer seniorFreezeShares,
        Integer splitCode) {
    /// Normalizes parcel and postal identifiers and fixes valuation scales without rounding.
    ///
    /// @throws IllegalArgumentException if an identifier is empty, contains nondigits, or exceeds
    ///   its width
    /// @throws ArithmeticException if a decimal exceeds its precision or requires rounding
    public SeniorFreezeMaster {
        baseYearEligibleComputedFullAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        baseYearEligibleComputedFullAssessedValue,
                        9,
                        0,
                        "baseYearEligibleComputedFullAssessedValue");
        baseYearEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        baseYearEqualizedValue, 9, 0, "baseYearEqualizedValue");
        baseYearFullAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        baseYearFullAssessedValue, 9, 0, "baseYearFullAssessedValue");
        baseYearTotalEligibleComputedEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        baseYearTotalEligibleComputedEqualizedValue,
                        9,
                        0,
                        "baseYearTotalEligibleComputedEqualizedValue");
        class288ExpirationAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        class288ExpirationAssessedValue, 9, 0, "class288ExpirationAssessedValue");
        class288ExpirationEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        class288ExpirationEqualizedValue, 9, 0, "class288ExpirationEqualizedValue");
        class288OverLimitAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        class288OverLimitAssessedValue, 9, 0, "class288OverLimitAssessedValue");
        class288OverLimitEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        class288OverLimitEqualizedValue, 9, 0, "class288OverLimitEqualizedValue");
        currentYearEligibleComputedAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearEligibleComputedAssessedValue,
                        9,
                        0,
                        "currentYearEligibleComputedAssessedValue");
        currentYearEligibleComputedEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearEligibleComputedEqualizedValue,
                        9,
                        0,
                        "currentYearEligibleComputedEqualizedValue");
        currentYearFinalEqualizedValueDifference =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearFinalEqualizedValueDifference,
                        9,
                        0,
                        "currentYearFinalEqualizedValueDifference");
        currentYearFullAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearFullAssessedValue, 9, 0, "currentYearFullAssessedValue");
        currentYearFullEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearFullEqualizedValue, 9, 0, "currentYearFullEqualizedValue");
        currentYearNotEligibleAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearNotEligibleAssessedValue,
                        9,
                        0,
                        "currentYearNotEligibleAssessedValue");
        currentYearNotEligibleEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        currentYearNotEligibleEqualizedValue,
                        9,
                        0,
                        "currentYearNotEligibleEqualizedValue");
        originalBaseYearEligibleComputedFullAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        originalBaseYearEligibleComputedFullAssessedValue,
                        9,
                        0,
                        "originalBaseYearEligibleComputedFullAssessedValue");
        originalBaseYearEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        originalBaseYearEqualizedValue, 9, 0, "originalBaseYearEqualizedValue");
        originalBaseYearFullAssessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        originalBaseYearFullAssessedValue,
                        9,
                        0,
                        "originalBaseYearFullAssessedValue");
        originalBaseYearTotalEligibleComputedEqualizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(
                        originalBaseYearTotalEligibleComputedEqualizedValue,
                        9,
                        0,
                        "originalBaseYearTotalEligibleComputedEqualizedValue");
        originalCurrentYearFinalEqualizedValueDifference =
                PropertyTaxExemptionsNumericBoundary.exact(
                        originalCurrentYearFinalEqualizedValueDifference,
                        9,
                        0,
                        "originalCurrentYearFinalEqualizedValueDifference");
        keyParcelNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        keyParcelNumber, 14, "keyParcelNumber");
        mailingZipCode =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        mailingZipCode, 9, "mailingZipCode");
        occupancyFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        occupancyFactor, 5, 1, "occupancyFactor");
        propertyProration =
                PropertyTaxExemptionsNumericBoundary.exact(
                        propertyProration, 7, 6, "propertyProration");
    }
}
