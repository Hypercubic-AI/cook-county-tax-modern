package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Immutable frozen agency adjustment domain snapshot.
///
/// Every valuation is a whole-unit amount unless its component documents a decimal rate or amount.
/// A repository returns a new snapshot after each save. The source object never changes.
///
/// @param id Generated persistence identifier. It is absent before the first successful save.
/// @param version Persistence version used to detect concurrent replacement. It is absent before
///   hydration.
/// @param agencyNumber Agency identifier, including leading zeroes.
/// @param annexedAssessedValue Whole-unit valuation amount for this category.
/// @param annexedEqualizedValue Whole-unit valuation amount for this category.
/// @param current288Value Whole-unit valuation amount for this category.
/// @param disconnectedAssessedValue Whole-unit valuation amount for this category.
/// @param disconnectedEqualizedValue Whole-unit valuation amount for this category.
/// @param expired288Value Whole-unit valuation amount for this category.
/// @param expiredIncentiveEqualizedValue Whole-unit valuation amount for this category.
/// @param expiredIncentiveTaxAmount exact tax amount when calculated, otherwise absent
/// @param expiredIncentiveValue Whole-unit valuation amount for this category.
/// @param firstTimeValue Whole-unit valuation amount for this category.
/// @param frozenEqualizedValue Whole-unit valuation amount for this category.
/// @param frozenTaxAmount Exact monetary amount in the source calculation.
/// @param taxCode Five-character tax-code identifier, including leading zeroes.
/// @param taxRate Exact tax rate used by the source calculation.
/// @param tifCurrentEqualizedValue Whole-unit valuation amount for this category.
/// @param tifDifferenceEqualizedValue Whole-unit valuation amount for this category.
/// @param tifPriorFrozenEqualizedValue Whole-unit valuation amount for this category.
/// @param totalFrozenValue Whole-unit valuation amount for this category.
public record FrozenAgencyAdjustment(
        @Nullable Long id,
        @Nullable Long version,
        String agencyNumber,
        BigDecimal annexedAssessedValue,
        BigDecimal annexedEqualizedValue,
        BigDecimal current288Value,
        BigDecimal disconnectedAssessedValue,
        BigDecimal disconnectedEqualizedValue,
        BigDecimal expired288Value,
        BigDecimal expiredIncentiveEqualizedValue,
        @Nullable BigDecimal expiredIncentiveTaxAmount,
        BigDecimal expiredIncentiveValue,
        BigDecimal firstTimeValue,
        BigDecimal frozenEqualizedValue,
        BigDecimal frozenTaxAmount,
        String taxCode,
        BigDecimal taxRate,
        BigDecimal tifCurrentEqualizedValue,
        BigDecimal tifDifferenceEqualizedValue,
        BigDecimal tifPriorFrozenEqualizedValue,
        BigDecimal totalFrozenValue) {
    /// Validates that every source-backed component is present.
    public FrozenAgencyAdjustment {
        agencyNumber = TaxRateNumericBoundary.identifier(agencyNumber, 9, "agencyNumber");
        annexedAssessedValue =
                TaxRateNumericBoundary.fixedPoint(
                        annexedAssessedValue, 13, 0, "annexedAssessedValue");
        annexedEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        annexedEqualizedValue, 13, 0, "annexedEqualizedValue");
        current288Value =
                TaxRateNumericBoundary.fixedPoint(current288Value, 13, 0, "current288Value");
        disconnectedAssessedValue =
                TaxRateNumericBoundary.fixedPoint(
                        disconnectedAssessedValue, 13, 0, "disconnectedAssessedValue");
        disconnectedEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        disconnectedEqualizedValue, 13, 0, "disconnectedEqualizedValue");
        expired288Value =
                TaxRateNumericBoundary.fixedPoint(expired288Value, 13, 0, "expired288Value");
        expiredIncentiveEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        expiredIncentiveEqualizedValue, 11, 0, "expiredIncentiveEqualizedValue");
        expiredIncentiveValue =
                TaxRateNumericBoundary.fixedPoint(
                        expiredIncentiveValue, 11, 0, "expiredIncentiveValue");
        firstTimeValue = TaxRateNumericBoundary.fixedPoint(firstTimeValue, 13, 0, "firstTimeValue");
        frozenEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        frozenEqualizedValue, 13, 0, "frozenEqualizedValue");
        frozenTaxAmount =
                TaxRateNumericBoundary.fixedPoint(frozenTaxAmount, 15, 2, "frozenTaxAmount");
        taxCode = TaxRateNumericBoundary.identifier(taxCode, 5, "taxCode");
        taxRate = TaxRateNumericBoundary.fixedPoint(taxRate, 6, 3, "taxRate");
        tifCurrentEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        tifCurrentEqualizedValue, 13, 0, "tifCurrentEqualizedValue");
        tifDifferenceEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        tifDifferenceEqualizedValue, 13, 0, "tifDifferenceEqualizedValue");
        tifPriorFrozenEqualizedValue =
                TaxRateNumericBoundary.fixedPoint(
                        tifPriorFrozenEqualizedValue, 13, 0, "tifPriorFrozenEqualizedValue");
        totalFrozenValue =
                TaxRateNumericBoundary.fixedPoint(totalFrozenValue, 13, 0, "totalFrozenValue");
        expiredIncentiveTaxAmount =
                expiredIncentiveTaxAmount == null
                        ? null
                        : TaxRateNumericBoundary.fixedPoint(
                                expiredIncentiveTaxAmount, 11, 2, "expiredIncentiveTaxAmount");
    }
}
