package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// The current homeowner exemption state for one property.
///
/// Renewal matching uses `propertyNumber`. A renewal returns a complete replacement record with
/// response status 2.
///
/// @param id generated database identity, absent before the first successful save
/// @param version optimistic-lock version, absent before persistence hydrates the record
/// @param applicationYear two-digit application year stored by the homeowner process
/// @param assessedValue assessed valuation amount in whole dollars
/// @param assessmentClass assessment classification selected for the property
/// @param certificateOfErrorNumber certificate-of-error reference when the record has one
/// @param city mailing city when owner-detail input supplied it
/// @param cooperativeQuantity cooperative units or shares when the processing path supplied them
/// @param equalizationFactor factor applied to assessed value, with four fractional digits
/// @param equalizedValue equalized valuation in whole dollars when supplied
/// @param exemptionType exemption category code when assigned
/// @param mailingAddress mailing street line when owner-detail input supplied it
/// @param baseYearExemptionAmount exemption amount for the base-year group, in whole dollars when
///   maintained
/// @param exemptionBaseYear four-digit year associated with that amount when maintained
/// @param baseYearEstablishmentCode two-character code that records how the process established the
///   base year
/// @param occupancyFactor occupancy percentage used by eligibility
/// @param ownerName owner name when owner-detail input supplied it
/// @param propertyNumber canonical 15-digit property identifier used to match homeowner and renewal
///   input
/// @param proration eligible property share as a decimal fraction with six fractional digits
/// @param responseStatus primary homeowner response classification
/// @param secondaryResponseStatus secondary homeowner response classification
/// @param state mailing state code when owner-detail input supplied it
/// @param taxCode canonical five-digit taxing-district code carried with the property
/// @param taxType tax-type code when the assessment supplied one
/// @param temporaryAssessedValue temporary assessed valuation in whole dollars when maintained
/// @param tertiaryStatus third homeowner response classification
/// @param volumeNumber canonical three-digit assessment volume that participates in source ordering
/// @param zipCode canonical nine-digit postal identifier when owner-detail input supplied it
public record HomeownerMaster(
        @Nullable Long id,
        @Nullable Long version,
        Integer applicationYear,
        BigDecimal assessedValue,
        Integer assessmentClass,
        @Nullable Integer certificateOfErrorNumber,
        @Nullable String city,
        @Nullable Integer cooperativeQuantity,
        BigDecimal equalizationFactor,
        @Nullable BigDecimal equalizedValue,
        @Nullable Integer exemptionType,
        @Nullable String mailingAddress,
        @Nullable BigDecimal baseYearExemptionAmount,
        @Nullable Integer exemptionBaseYear,
        String baseYearEstablishmentCode,
        BigDecimal occupancyFactor,
        @Nullable String ownerName,
        String propertyNumber,
        BigDecimal proration,
        Integer responseStatus,
        Integer secondaryResponseStatus,
        @Nullable String state,
        String taxCode,
        @Nullable Integer taxType,
        @Nullable BigDecimal temporaryAssessedValue,
        Integer tertiaryStatus,
        String volumeNumber,
        @Nullable String zipCode) {
    /// Pads numeric identifiers and fixes decimal scales without rounding. Optional values retain
    /// absence.
    ///
    /// @throws IllegalArgumentException if an identifier is empty, contains nondigits, or exceeds
    ///   its width
    /// @throws ArithmeticException if a decimal exceeds its precision or requires rounding
    public HomeownerMaster {
        assessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(assessedValue, 9, 0, "assessedValue");
        equalizationFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        equalizationFactor, 5, 4, "equalizationFactor");
        equalizedValue =
                PropertyTaxExemptionsNumericBoundary.optionalExact(
                        equalizedValue, 9, 0, "equalizedValue");
        baseYearExemptionAmount =
                PropertyTaxExemptionsNumericBoundary.optionalExact(
                        baseYearExemptionAmount, 9, 0, "baseYearExemptionAmount");
        occupancyFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        occupancyFactor, 5, 1, "occupancyFactor");
        propertyNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        propertyNumber, 15, "propertyNumber");
        proration = PropertyTaxExemptionsNumericBoundary.exact(proration, 7, 6, "proration");
        taxCode = PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(taxCode, 5, "taxCode");
        temporaryAssessedValue =
                PropertyTaxExemptionsNumericBoundary.optionalExact(
                        temporaryAssessedValue, 9, 0, "temporaryAssessedValue");
        volumeNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        volumeNumber, 3, "volumeNumber");
        zipCode =
                PropertyTaxExemptionsNumericBoundary.optionalUnsignedIdentifier(
                        zipCode, 9, "zipCode");
    }

    /// Returns a complete replacement with a new primary response classification.
    public HomeownerMaster withResponseStatus(Integer replacementStatus) {
        return new HomeownerMaster(
                id,
                version,
                applicationYear,
                assessedValue,
                assessmentClass,
                certificateOfErrorNumber,
                city,
                cooperativeQuantity,
                equalizationFactor,
                equalizedValue,
                exemptionType,
                mailingAddress,
                baseYearExemptionAmount,
                exemptionBaseYear,
                baseYearEstablishmentCode,
                occupancyFactor,
                ownerName,
                propertyNumber,
                proration,
                replacementStatus,
                secondaryResponseStatus,
                state,
                taxCode,
                taxType,
                temporaryAssessedValue,
                tertiaryStatus,
                volumeNumber,
                zipCode);
    }

    /// Returns the complete homeowner snapshot after annual eligibility refresh.
    ///
    /// A new application year resets the three response states. It also replaces the base-year
    /// establishment code unless the source-supported `TR` code must remain.
    ///
    /// @param replacementApplicationYear two-digit application year
    /// @param replacementAssessedValue assessed valuation in whole dollars
    /// @param replacementAssessmentClass assessment classification selected for the property
    /// @param replacementOccupancyFactor occupancy percentage used by eligibility
    /// @param replacementProration eligible property share with six fractional digits
    /// @param replacementResponseStatus replacement primary response state
    /// @param replacementSecondaryStatus replacement secondary response state
    /// @param replacementTertiaryStatus replacement tertiary response state
    /// @param replacementBaseYearEstablishmentCode replacement two-character base-year
    ///   establishment code
    /// @param replacementTaxCode taxing-district code carried from the assessment
    /// @param replacementVolumeNumber assessment volume used in source ordering
    /// @return a replacement snapshot that preserves maintained amounts and contact fields
    public HomeownerMaster withEligibility(
            Integer replacementApplicationYear,
            BigDecimal replacementAssessedValue,
            Integer replacementAssessmentClass,
            BigDecimal replacementOccupancyFactor,
            BigDecimal replacementProration,
            Integer replacementResponseStatus,
            Integer replacementSecondaryStatus,
            Integer replacementTertiaryStatus,
            String replacementBaseYearEstablishmentCode,
            String replacementTaxCode,
            String replacementVolumeNumber) {
        return new HomeownerMaster(
                id,
                version,
                replacementApplicationYear,
                replacementAssessedValue,
                replacementAssessmentClass,
                certificateOfErrorNumber,
                city,
                cooperativeQuantity,
                equalizationFactor,
                equalizedValue,
                exemptionType,
                mailingAddress,
                baseYearExemptionAmount,
                exemptionBaseYear,
                replacementBaseYearEstablishmentCode,
                replacementOccupancyFactor,
                ownerName,
                propertyNumber,
                replacementProration,
                replacementResponseStatus,
                replacementSecondaryStatus,
                state,
                replacementTaxCode,
                taxType,
                temporaryAssessedValue,
                replacementTertiaryStatus,
                replacementVolumeNumber,
                zipCode);
    }
}
