package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// A maintained homestead exemption record used by manual exemption processing.
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
/// @param equalizationFactor Factor applied to assessed value. Its stored scale is four fractional
///   digits.
/// @param equalizedValue Equalized valuation amount in whole dollars when the process supplied one.
/// @param exemptionType Exemption category code when the processing path assigned one.
/// @param mailingAddress Mailing street line when owner-detail input supplied it.
/// @param baseYearExemptionAmount Exemption amount for the base-year group, in whole dollars.
/// @param exemptionBaseYear Four-digit year associated with the maintained amount.
/// @param baseYearEstablishmentCode Two-character code that records how the process established the
///   base year.
/// @param occupancyFactor Occupancy percentage used by exemption eligibility.
/// @param ownerName Owner name when owner-detail input supplied it.
/// @param propertyNumber Canonical 15-digit property identifier used to match homeowner and renewal
///   input.
/// @param proration Eligible property share as a decimal fraction. Its stored scale is six
///   fractional digits.
/// @param responseStatus Primary homeowner response classification.
/// @param secondaryResponseStatus Secondary homeowner response classification.
/// @param state Mailing state code when owner-detail input supplied it.
/// @param taxCode Canonical five-digit taxing-district code carried with the property.
/// @param taxType Tax-type code carried with the property.
/// @param tertiaryStatus Third homeowner response classification.
/// @param volumeNumber canonical three-digit assessment volume that participates in source ordering
/// @param zipCode Canonical nine-digit postal identifier when owner-detail input supplied it.
public record MaintainedHomesteadExemption(
        @Nullable Long id,
        @Nullable Long version,
        Integer applicationYear,
        BigDecimal assessedValue,
        Integer assessmentClass,
        Integer certificateOfErrorNumber,
        String city,
        Integer clerksClass,
        Integer cooperativeQuantity,
        BigDecimal equalizationFactor,
        BigDecimal equalizedValue,
        Integer exemptionType,
        String mailingAddress,
        BigDecimal baseYearExemptionAmount,
        Integer exemptionBaseYear,
        String baseYearEstablishmentCode,
        BigDecimal occupancyFactor,
        String ownerName,
        String propertyNumber,
        BigDecimal proration,
        Integer responseStatus,
        Integer secondaryResponseStatus,
        String state,
        String taxCode,
        String taxType,
        Integer tertiaryStatus,
        String volumeNumber,
        String zipCode) {
    /// Pads numeric identifiers and fixes decimal scales before the maintained snapshot reaches
    /// persistence.
    ///
    /// @throws IllegalArgumentException if an identifier is empty, contains nondigits, or exceeds
    ///   its width
    /// @throws ArithmeticException if a decimal exceeds its precision or requires rounding
    public MaintainedHomesteadExemption {
        assessedValue =
                PropertyTaxExemptionsNumericBoundary.exact(assessedValue, 9, 0, "assessedValue");
        equalizationFactor =
                PropertyTaxExemptionsNumericBoundary.exact(
                        equalizationFactor, 5, 4, "equalizationFactor");
        equalizedValue =
                PropertyTaxExemptionsNumericBoundary.exact(equalizedValue, 9, 0, "equalizedValue");
        baseYearExemptionAmount =
                PropertyTaxExemptionsNumericBoundary.exact(
                        baseYearExemptionAmount, 9, 0, "baseYearExemptionAmount");
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
        zipCode = PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(zipCode, 9, "zipCode");
    }
}
