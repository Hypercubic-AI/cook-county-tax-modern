package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/// Immutable agency equalized valuation domain snapshot.
///
/// Every valuation is a whole-unit amount unless its component documents a decimal rate or amount.
/// A repository returns a new snapshot after each save. The source object never changes.
///
/// @param id Generated persistence identifier. It is absent before the first successful save.
/// @param version Persistence version used to detect concurrent replacement. It is absent before
///   hydration.
/// @param agencyNumber Agency identifier, including leading zeroes.
/// @param annexedPropertyEqualizedValue Whole-unit valuation amount for this category.
/// @param burdenPercent Exact percentage value, not a fraction.
/// @param connectingAgency1 Agency identifier in this ordered connecting-agency slot.
/// @param connectingAgency2 Agency identifier in this ordered connecting-agency slot.
/// @param connectingAgency3 Agency identifier in this ordered connecting-agency slot.
/// @param connectingAgency4 Agency identifier in this ordered connecting-agency slot.
/// @param cookCountyAirPollutionValue Whole-unit valuation amount for this category.
/// @param cookCountyRailroadValue Whole-unit valuation amount for this category.
/// @param cookCountyRealEstateValue Whole-unit valuation amount for this category.
/// @param cookCountyUseTaxValue Whole-unit valuation amount for this category.
/// @param deKalbCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param disconnectedPropertyEqualizedValue Whole-unit valuation amount for this category.
/// @param disconnectedTifDifference Whole-unit valuation amount for this category.
/// @param duPageCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param grundyCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param kaneCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param kankakeeCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param kendallCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param laSalleCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param lakeCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param limitingTaxRateOverride Exact tax rate used by the source calculation.
/// @param livingstonCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param mcHenryCountyEqualizedValue Whole-unit valuation amount for this category.
/// @param newPropertyEqualizedValue Whole-unit valuation amount for this category.
/// @param overlapAnnexedPropertyEqualizedValue Whole-unit valuation amount for this category.
/// @param overlapDisconnectedPropertyEqualizedValue Whole-unit valuation amount for this category.
/// @param overlapDisconnectedTifDifference Whole-unit valuation amount for this category.
/// @param overlapNewPropertyEqualizedValue Whole-unit valuation amount for this category.
/// @param parentAgency1 Agency identifier in this ordered parent-agency slot.
/// @param parentAgency2 Agency identifier in this ordered parent-agency slot.
/// @param parentAgency3 Agency identifier in this ordered parent-agency slot.
/// @param parentAgency4 Agency identifier in this ordered parent-agency slot.
/// @param parentAgency5 Agency identifier in this ordered parent-agency slot.
/// @param previousTaxYear1 Tax year represented as the stored whole-number year.
/// @param previousTaxYear1Extension Exact prior-year tax-extension amount with two decimal places.
/// @param previousTaxYear2 Tax year represented as the stored whole-number year.
/// @param previousTaxYear2Extension Exact prior-year tax-extension amount with two decimal places.
/// @param previousTaxYear3 Tax year represented as the stored whole-number year.
/// @param previousTaxYear3Extension Exact prior-year tax-extension amount with two decimal places.
/// @param taxCapIndicator Stored tax-cap indicator.
/// @param taxYear Tax year represented as the stored whole-number year.
/// @param willCountyEqualizedValue Whole-unit valuation amount for this category.
public record AgencyEqualizedValuation(
        @Nullable Long id,
        @Nullable Long version,
        String agencyNumber,
        BigDecimal annexedPropertyEqualizedValue,
        BigDecimal burdenPercent,
        String connectingAgency1,
        String connectingAgency2,
        String connectingAgency3,
        String connectingAgency4,
        BigDecimal cookCountyAirPollutionValue,
        BigDecimal cookCountyRailroadValue,
        BigDecimal cookCountyRealEstateValue,
        BigDecimal cookCountyUseTaxValue,
        BigDecimal deKalbCountyEqualizedValue,
        BigDecimal disconnectedPropertyEqualizedValue,
        BigDecimal disconnectedTifDifference,
        BigDecimal duPageCountyEqualizedValue,
        BigDecimal grundyCountyEqualizedValue,
        BigDecimal kaneCountyEqualizedValue,
        BigDecimal kankakeeCountyEqualizedValue,
        BigDecimal kendallCountyEqualizedValue,
        BigDecimal laSalleCountyEqualizedValue,
        BigDecimal lakeCountyEqualizedValue,
        BigDecimal limitingTaxRateOverride,
        BigDecimal livingstonCountyEqualizedValue,
        BigDecimal mcHenryCountyEqualizedValue,
        BigDecimal newPropertyEqualizedValue,
        BigDecimal overlapAnnexedPropertyEqualizedValue,
        BigDecimal overlapDisconnectedPropertyEqualizedValue,
        BigDecimal overlapDisconnectedTifDifference,
        BigDecimal overlapNewPropertyEqualizedValue,
        String parentAgency1,
        String parentAgency2,
        String parentAgency3,
        String parentAgency4,
        String parentAgency5,
        Integer previousTaxYear1,
        BigDecimal previousTaxYear1Extension,
        Integer previousTaxYear2,
        BigDecimal previousTaxYear2Extension,
        Integer previousTaxYear3,
        BigDecimal previousTaxYear3Extension,
        Boolean taxCapIndicator,
        Integer taxYear,
        BigDecimal willCountyEqualizedValue) {
    /// Validates that every source-backed component is present.
    public AgencyEqualizedValuation {
        agencyNumber = EifdNumericBoundary.identifier(agencyNumber, 9, "agencyNumber");
        annexedPropertyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        annexedPropertyEqualizedValue, 11, 0, "annexedPropertyEqualizedValue");
        burdenPercent =
                EifdNumericBoundary.unsignedFixedPoint(burdenPercent, 5, 2, "burdenPercent");
        connectingAgency1 =
                EifdNumericBoundary.identifier(connectingAgency1, 9, "connectingAgency1");
        connectingAgency2 =
                EifdNumericBoundary.identifier(connectingAgency2, 9, "connectingAgency2");
        connectingAgency3 =
                EifdNumericBoundary.identifier(connectingAgency3, 9, "connectingAgency3");
        connectingAgency4 =
                EifdNumericBoundary.identifier(connectingAgency4, 9, "connectingAgency4");
        cookCountyAirPollutionValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        cookCountyAirPollutionValue, 11, 0, "cookCountyAirPollutionValue");
        cookCountyRailroadValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        cookCountyRailroadValue, 11, 0, "cookCountyRailroadValue");
        cookCountyRealEstateValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        cookCountyRealEstateValue, 13, 0, "cookCountyRealEstateValue");
        cookCountyUseTaxValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        cookCountyUseTaxValue, 11, 0, "cookCountyUseTaxValue");
        deKalbCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        deKalbCountyEqualizedValue, 11, 0, "deKalbCountyEqualizedValue");
        disconnectedPropertyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        disconnectedPropertyEqualizedValue,
                        11,
                        0,
                        "disconnectedPropertyEqualizedValue");
        disconnectedTifDifference =
                EifdNumericBoundary.unsignedFixedPoint(
                        disconnectedTifDifference, 11, 0, "disconnectedTifDifference");
        duPageCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        duPageCountyEqualizedValue, 11, 0, "duPageCountyEqualizedValue");
        grundyCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        grundyCountyEqualizedValue, 11, 0, "grundyCountyEqualizedValue");
        kaneCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        kaneCountyEqualizedValue, 11, 0, "kaneCountyEqualizedValue");
        kankakeeCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        kankakeeCountyEqualizedValue, 11, 0, "kankakeeCountyEqualizedValue");
        kendallCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        kendallCountyEqualizedValue, 11, 0, "kendallCountyEqualizedValue");
        laSalleCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        laSalleCountyEqualizedValue, 11, 0, "laSalleCountyEqualizedValue");
        lakeCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        lakeCountyEqualizedValue, 11, 0, "lakeCountyEqualizedValue");
        limitingTaxRateOverride =
                EifdNumericBoundary.fixedPoint(
                        limitingTaxRateOverride, 9, 6, "limitingTaxRateOverride");
        livingstonCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        livingstonCountyEqualizedValue, 11, 0, "livingstonCountyEqualizedValue");
        mcHenryCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        mcHenryCountyEqualizedValue, 11, 0, "mcHenryCountyEqualizedValue");
        newPropertyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        newPropertyEqualizedValue, 11, 0, "newPropertyEqualizedValue");
        overlapAnnexedPropertyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        overlapAnnexedPropertyEqualizedValue,
                        11,
                        0,
                        "overlapAnnexedPropertyEqualizedValue");
        overlapDisconnectedPropertyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        overlapDisconnectedPropertyEqualizedValue,
                        11,
                        0,
                        "overlapDisconnectedPropertyEqualizedValue");
        overlapDisconnectedTifDifference =
                EifdNumericBoundary.unsignedFixedPoint(
                        overlapDisconnectedTifDifference,
                        11,
                        0,
                        "overlapDisconnectedTifDifference");
        overlapNewPropertyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        overlapNewPropertyEqualizedValue,
                        11,
                        0,
                        "overlapNewPropertyEqualizedValue");
        parentAgency1 = EifdNumericBoundary.identifier(parentAgency1, 9, "parentAgency1");
        parentAgency2 = EifdNumericBoundary.identifier(parentAgency2, 9, "parentAgency2");
        parentAgency3 = EifdNumericBoundary.identifier(parentAgency3, 9, "parentAgency3");
        parentAgency4 = EifdNumericBoundary.identifier(parentAgency4, 9, "parentAgency4");
        parentAgency5 = EifdNumericBoundary.identifier(parentAgency5, 9, "parentAgency5");
        Objects.requireNonNull(previousTaxYear1, "previousTaxYear1");
        previousTaxYear1Extension =
                EifdNumericBoundary.unsignedFixedPoint(
                        previousTaxYear1Extension, 13, 2, "previousTaxYear1Extension");
        Objects.requireNonNull(previousTaxYear2, "previousTaxYear2");
        previousTaxYear2Extension =
                EifdNumericBoundary.unsignedFixedPoint(
                        previousTaxYear2Extension, 13, 2, "previousTaxYear2Extension");
        Objects.requireNonNull(previousTaxYear3, "previousTaxYear3");
        previousTaxYear3Extension =
                EifdNumericBoundary.unsignedFixedPoint(
                        previousTaxYear3Extension, 13, 2, "previousTaxYear3Extension");
        Objects.requireNonNull(taxCapIndicator, "taxCapIndicator");
        Objects.requireNonNull(taxYear, "taxYear");
        willCountyEqualizedValue =
                EifdNumericBoundary.unsignedFixedPoint(
                        willCountyEqualizedValue, 11, 0, "willCountyEqualizedValue");
    }
}
