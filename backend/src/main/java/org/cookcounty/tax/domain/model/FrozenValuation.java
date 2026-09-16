package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Immutable frozen valuation domain snapshot.
///
/// Every valuation is a whole-unit amount unless its component documents a decimal rate or amount.
/// A repository returns a new snapshot after each save. The source object never changes.
///
/// @param id Generated persistence identifier. It is absent before the first successful save.
/// @param version Persistence version used to detect concurrent replacement. It is absent before
///   hydration.
/// @param changeActionCurrentImprovementValue Whole-unit valuation amount for this category.
/// @param changeActionCurrentLandValue Whole-unit valuation amount for this category.
/// @param changeActionCurrentParcelCount Whole-unit record count for this category.
/// @param changeActionCurrentTotalValue Whole-unit valuation amount for this category.
/// @param changeActionPriorImprovementValue Whole-unit valuation amount for this category.
/// @param changeActionPriorLandValue Whole-unit valuation amount for this category.
/// @param changeActionPriorParcelCount Whole-unit record count for this category.
/// @param changeActionPriorTotalValue Whole-unit valuation amount for this category.
/// @param currentImprovementValue Whole-unit valuation amount for this category.
/// @param currentLandValue Whole-unit valuation amount for this category.
/// @param currentParcelCount Whole-unit record count for this category.
/// @param currentTotalValue Whole-unit valuation amount for this category.
/// @param divisionNumber Division identifier, including leading zeroes.
/// @param noChangeActionCurrentImprovementValue Whole-unit valuation amount for this category.
/// @param noChangeActionCurrentLandValue Whole-unit valuation amount for this category.
/// @param noChangeActionCurrentParcelCount Whole-unit record count for this category.
/// @param noChangeActionCurrentTotalValue Whole-unit valuation amount for this category.
/// @param noChangeActionPriorImprovementValue Whole-unit valuation amount for this category.
/// @param noChangeActionPriorLandValue Whole-unit valuation amount for this category.
/// @param noChangeActionPriorParcelCount Whole-unit record count for this category.
/// @param noChangeActionPriorTotalValue Whole-unit valuation amount for this category.
/// @param priorImprovementValue Whole-unit valuation amount for this category.
/// @param priorLandValue Whole-unit valuation amount for this category.
/// @param priorParcelCount Whole-unit record count for this category.
/// @param priorTotalValue Whole-unit valuation amount for this category.
/// @param proposedActualValue Whole-unit valuation amount for this category.
/// @param proposedCurrent288Value Whole-unit valuation amount for this category.
/// @param proposedExpired288Value Whole-unit valuation amount for this category.
/// @param proposedImprovementValue Whole-unit valuation amount for this category.
/// @param proposedTotalValue Whole-unit valuation amount for this category.
public record FrozenValuation(
        @Nullable Long id,
        @Nullable Long version,
        BigDecimal changeActionCurrentImprovementValue,
        BigDecimal changeActionCurrentLandValue,
        Long changeActionCurrentParcelCount,
        BigDecimal changeActionCurrentTotalValue,
        BigDecimal changeActionPriorImprovementValue,
        BigDecimal changeActionPriorLandValue,
        Long changeActionPriorParcelCount,
        BigDecimal changeActionPriorTotalValue,
        BigDecimal currentImprovementValue,
        BigDecimal currentLandValue,
        Long currentParcelCount,
        BigDecimal currentTotalValue,
        String divisionNumber,
        BigDecimal noChangeActionCurrentImprovementValue,
        BigDecimal noChangeActionCurrentLandValue,
        Long noChangeActionCurrentParcelCount,
        BigDecimal noChangeActionCurrentTotalValue,
        BigDecimal noChangeActionPriorImprovementValue,
        BigDecimal noChangeActionPriorLandValue,
        Long noChangeActionPriorParcelCount,
        BigDecimal noChangeActionPriorTotalValue,
        BigDecimal priorImprovementValue,
        BigDecimal priorLandValue,
        Long priorParcelCount,
        BigDecimal priorTotalValue,
        BigDecimal proposedActualValue,
        BigDecimal proposedCurrent288Value,
        BigDecimal proposedExpired288Value,
        BigDecimal proposedImprovementValue,
        BigDecimal proposedTotalValue) {
    /// Validates that every source-backed component is present.
    public FrozenValuation {
        changeActionCurrentImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        changeActionCurrentImprovementValue,
                        13,
                        0,
                        "changeActionCurrentImprovementValue");
        changeActionCurrentLandValue =
                EifdNumericBoundary.fixedPoint(
                        changeActionCurrentLandValue, 13, 0, "changeActionCurrentLandValue");
        changeActionCurrentParcelCount =
                EifdNumericBoundary.signedIntegral(
                        changeActionCurrentParcelCount,
                        9_999_999_999_999L,
                        "changeActionCurrentParcelCount");
        changeActionCurrentTotalValue =
                EifdNumericBoundary.fixedPoint(
                        changeActionCurrentTotalValue, 13, 0, "changeActionCurrentTotalValue");
        changeActionPriorImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        changeActionPriorImprovementValue,
                        13,
                        0,
                        "changeActionPriorImprovementValue");
        changeActionPriorLandValue =
                EifdNumericBoundary.fixedPoint(
                        changeActionPriorLandValue, 13, 0, "changeActionPriorLandValue");
        changeActionPriorParcelCount =
                EifdNumericBoundary.signedIntegral(
                        changeActionPriorParcelCount,
                        9_999_999_999_999L,
                        "changeActionPriorParcelCount");
        changeActionPriorTotalValue =
                EifdNumericBoundary.fixedPoint(
                        changeActionPriorTotalValue, 13, 0, "changeActionPriorTotalValue");
        currentImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        currentImprovementValue, 13, 0, "currentImprovementValue");
        currentLandValue =
                EifdNumericBoundary.fixedPoint(currentLandValue, 13, 0, "currentLandValue");
        currentParcelCount =
                EifdNumericBoundary.signedIntegral(
                        currentParcelCount, 9_999_999_999_999L, "currentParcelCount");
        currentTotalValue =
                EifdNumericBoundary.fixedPoint(currentTotalValue, 13, 0, "currentTotalValue");
        divisionNumber = EifdNumericBoundary.identifier(divisionNumber, 14, "divisionNumber");
        noChangeActionCurrentImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        noChangeActionCurrentImprovementValue,
                        13,
                        0,
                        "noChangeActionCurrentImprovementValue");
        noChangeActionCurrentLandValue =
                EifdNumericBoundary.fixedPoint(
                        noChangeActionCurrentLandValue, 13, 0, "noChangeActionCurrentLandValue");
        noChangeActionCurrentParcelCount =
                EifdNumericBoundary.signedIntegral(
                        noChangeActionCurrentParcelCount,
                        9_999_999_999_999L,
                        "noChangeActionCurrentParcelCount");
        noChangeActionCurrentTotalValue =
                EifdNumericBoundary.fixedPoint(
                        noChangeActionCurrentTotalValue, 13, 0, "noChangeActionCurrentTotalValue");
        noChangeActionPriorImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        noChangeActionPriorImprovementValue,
                        13,
                        0,
                        "noChangeActionPriorImprovementValue");
        noChangeActionPriorLandValue =
                EifdNumericBoundary.fixedPoint(
                        noChangeActionPriorLandValue, 13, 0, "noChangeActionPriorLandValue");
        noChangeActionPriorParcelCount =
                EifdNumericBoundary.signedIntegral(
                        noChangeActionPriorParcelCount,
                        9_999_999_999_999L,
                        "noChangeActionPriorParcelCount");
        noChangeActionPriorTotalValue =
                EifdNumericBoundary.fixedPoint(
                        noChangeActionPriorTotalValue, 13, 0, "noChangeActionPriorTotalValue");
        priorImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        priorImprovementValue, 13, 0, "priorImprovementValue");
        priorLandValue = EifdNumericBoundary.fixedPoint(priorLandValue, 13, 0, "priorLandValue");
        priorParcelCount =
                EifdNumericBoundary.signedIntegral(
                        priorParcelCount, 9_999_999_999_999L, "priorParcelCount");
        priorTotalValue = EifdNumericBoundary.fixedPoint(priorTotalValue, 13, 0, "priorTotalValue");
        proposedActualValue =
                EifdNumericBoundary.fixedPoint(proposedActualValue, 13, 0, "proposedActualValue");
        proposedCurrent288Value =
                EifdNumericBoundary.fixedPoint(
                        proposedCurrent288Value, 13, 0, "proposedCurrent288Value");
        proposedExpired288Value =
                EifdNumericBoundary.fixedPoint(
                        proposedExpired288Value, 13, 0, "proposedExpired288Value");
        proposedImprovementValue =
                EifdNumericBoundary.fixedPoint(
                        proposedImprovementValue, 13, 0, "proposedImprovementValue");
        proposedTotalValue =
                EifdNumericBoundary.fixedPoint(proposedTotalValue, 13, 0, "proposedTotalValue");
    }
}
