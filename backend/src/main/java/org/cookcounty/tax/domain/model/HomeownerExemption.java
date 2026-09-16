package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// An annual homeowner eligibility snapshot published by the roll-forward process.
///
/// @param id Generated database identity. It is absent before the first successful persistence
///   operation.
/// @param version Optimistic-lock version. It is absent before persistence hydrates the record.
/// @param applicationYear Two-digit application year stored by the homeowner process.
/// @param assessedValue Assessed valuation amount in whole dollars.
/// @param assessmentClass Assessment classification selected for the property.
/// @param certificateOfErrorNumber Certificate-of-error reference when the record has one.
/// @param city Mailing city when owner-detail input supplied it.
/// @param clerksClass Clerk classification carried by the annual exemption record.
/// @param cooperativeQuantity Number of cooperative units or shares carried by the exemption
///   record.
/// @param eligibilityIndicator Indicator that records the annual eligibility decision.
/// @param equalizationFactor Factor applied to assessed value. Its stored scale is four fractional
///   digits.
/// @param equalizedValue Equalized valuation amount in whole dollars when the process supplied one.
/// @param exemptionType Exemption category code when the processing path assigned one.
/// @param keyParcelNumber canonical source-width parcel identifier selected from qualifying
///   assessment details
/// @param mailingAddress Mailing street line when owner-detail input supplied it.
/// @param occupancyFactor Occupancy percentage used by exemption eligibility.
/// @param ownerName Owner name when owner-detail input supplied it.
/// @param propertyNumber Canonical 15-digit property identifier used to match homeowner and renewal
///   input.
/// @param proration Eligible property share as a decimal fraction. Its stored scale is six
///   fractional digits.
/// @param recordCode Record category within the Senior Freeze or homeowner output.
/// @param responseStatus Primary homeowner response classification.
/// @param secondaryResponseStatus Secondary homeowner response classification.
/// @param splitCode Property split classification when qualifying detail supplied it.
/// @param state Mailing state code when owner-detail input supplied it.
/// @param taxCode Canonical five-digit taxing-district code carried with the property.
/// @param taxType Tax-type code carried with the property.
/// @param tertiaryStatus Third homeowner response classification.
/// @param volumeNumber canonical three-digit assessment volume that participates in source ordering
/// @param zipCode Canonical nine-digit postal identifier when owner-detail input supplied it.
public record HomeownerExemption(
        @Nullable Long id,
        @Nullable Long version,
        Integer applicationYear,
        BigDecimal assessedValue,
        Integer assessmentClass,
        @Nullable Integer certificateOfErrorNumber,
        @Nullable String city,
        Integer clerksClass,
        @Nullable Integer cooperativeQuantity,
        Integer eligibilityIndicator,
        BigDecimal equalizationFactor,
        @Nullable BigDecimal equalizedValue,
        @Nullable Integer exemptionType,
        String keyParcelNumber,
        @Nullable String mailingAddress,
        BigDecimal occupancyFactor,
        @Nullable String ownerName,
        String propertyNumber,
        BigDecimal proration,
        Integer recordCode,
        Integer responseStatus,
        Integer secondaryResponseStatus,
        @Nullable Integer splitCode,
        @Nullable String state,
        String taxCode,
        @Nullable Integer taxType,
        Integer tertiaryStatus,
        String volumeNumber,
        @Nullable String zipCode) {
    /// Pads property and postal identifiers and fixes decimal scales. Optional values retain
    /// absence.
    ///
    /// @throws IllegalArgumentException if an identifier is empty, contains nondigits, or exceeds
    ///   its width
    /// @throws ArithmeticException if a decimal exceeds its precision or requires rounding
    public HomeownerExemption {
        assessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(assessedValue, 9, 0, "assessedValue");
        equalizationFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        equalizationFactor, 5, 4, "equalizationFactor");
        equalizedValue =
                PropertyTaxExemptionsNumericBoundary.optionalExact(
                        equalizedValue, 9, 0, "equalizedValue");
        keyParcelNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        keyParcelNumber, 15, "keyParcelNumber");
        occupancyFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        occupancyFactor, 5, 1, "occupancyFactor");
        propertyNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        propertyNumber, 15, "propertyNumber");
        proration = PropertyTaxExemptionsNumericBoundary.exact(proration, 7, 6, "proration");
        taxCode = PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(taxCode, 5, "taxCode");
        volumeNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        volumeNumber, 3, "volumeNumber");
        zipCode =
                PropertyTaxExemptionsNumericBoundary.optionalUnsignedIdentifier(
                        zipCode, 9, "zipCode");
    }
}
