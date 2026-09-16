package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// One ordered land or improvement detail from an assessment parcel.
///
/// Valuation uses whole dollars. Encoded frontage and area use `decimalScale` to place the decimal
/// point. Factor components retain their source decimal representation. Type-specific components
/// are absent when the detail layout does not define them. A questionnaire can immediately follow a
/// qualifying improvement, but the companion is not a separately valued business detail.
///
/// @param id generated persistence identity, or `null` before the first save
/// @param version optimistic-lock version, or `null` before the first save
/// @param age improvement age when the detail layout supplies it
/// @param area encoded improvement area when the detail layout supplies it
/// @param assessmentClass Assessor class used by valuation and parcel-class decisions
/// @param supplementalDetailCode two-character detail code when present. `GR` identifies the
///   observed garage case
/// @param conditionFactor percentage condition factor when present
/// @param cornerFactor multiplicative land corner factor when present
/// @param decimalScale decimal places encoded in frontage or area
/// @param depth encoded land depth when present
/// @param depthFactor multiplicative land depth factor when present
/// @param detailCode one-character subtype code
/// @param detailType one-character land or improvement layout selector
/// @param extraCornerFactor multiplicative extra-corner factor when present
/// @param frontFootage encoded land frontage when present
/// @param improvementYear two-digit improvement year when present
/// @param keyParcelNumber related parcel identifier when present
/// @param landConditionFactor percentage land-condition factor when present
/// @param multicode source multicode for this occurrence
/// @param occupancyFactor percentage occupancy used by Type-5 conversion when present
/// @param occurrenceNumber one-based order within the parcel's detail and questionnaire sequence
/// @param parcelNumber identifier of the parcel that owns this occurrence
/// @param parcelVolumeNumber volume of the parcel that owns this occurrence
/// @param percentAssessed percentage applied to the calculated value when positive
/// @param reproductionCost improvement reproduction cost in whole dollars when present
/// @param splitCode source split indicator when present
/// @param unitMeasure land unit code when present
/// @param unitPrice price per decoded land or area unit when present
/// @param valuation assessed value in whole dollars
public record AssessmentDetail(
        @Nullable Long id,
        @Nullable Long version,
        @Nullable Integer age,
        @Nullable Long area,
        Integer assessmentClass,
        @Nullable String supplementalDetailCode,
        @Nullable BigDecimal conditionFactor,
        @Nullable BigDecimal cornerFactor,
        @Nullable Integer decimalScale,
        @Nullable Long depth,
        @Nullable BigDecimal depthFactor,
        String detailCode,
        String detailType,
        @Nullable BigDecimal extraCornerFactor,
        @Nullable Long frontFootage,
        @Nullable Integer improvementYear,
        @Nullable String keyParcelNumber,
        @Nullable BigDecimal landConditionFactor,
        Integer multicode,
        @Nullable BigDecimal occupancyFactor,
        Integer occurrenceNumber,
        String parcelNumber,
        String parcelVolumeNumber,
        @Nullable BigDecimal percentAssessed,
        @Nullable BigDecimal reproductionCost,
        @Nullable String splitCode,
        @Nullable String unitMeasure,
        @Nullable BigDecimal unitPrice,
        BigDecimal valuation) {

    /// Validates the owning source key and exact whole-unit values.
    public AssessmentDetail {
        parcelNumber = AssessmentNumericBoundary.signedIdentifier(parcelNumber, 15, "parcelNumber");
        parcelVolumeNumber =
                AssessmentNumericBoundary.signedIdentifier(
                        parcelVolumeNumber, 3, "parcelVolumeNumber");
        keyParcelNumber =
                keyParcelNumber == null
                        ? null
                        : AssessmentNumericBoundary.signedIdentifier(
                                keyParcelNumber, 15, "keyParcelNumber");
        reproductionCost =
                reproductionCost == null
                        ? null
                        : AssessmentNumericBoundary.wholeUnits(
                                reproductionCost, 9, "reproductionCost");
        valuation = AssessmentNumericBoundary.wholeUnits(valuation, 9, "valuation");
    }

    /// Returns the complete detail with a recalculated whole-dollar value.
    ///
    /// @param assessedValue replacement assessed value in whole dollars
    /// @return a replacement detail that preserves identity, source fields, and version
    public AssessmentDetail withValuation(BigDecimal assessedValue) {
        return copy(
                supplementalDetailCode,
                detailCode,
                detailType,
                improvementYear,
                reproductionCost,
                assessedValue);
    }

    /// Returns the complete detail with a replacement two-character supplemental code.
    ///
    /// `null` represents a layout without the field. An empty string represents a code that the
    /// processing path cleared.
    ///
    /// @param replacementSupplementalDetailCode replacement code, an empty string when cleared, or
    ///   `null` when absent
    /// @return a replacement detail that preserves every other field and its version
    public AssessmentDetail withSupplementalDetailCode(
            @Nullable String replacementSupplementalDetailCode) {
        return copy(
                replacementSupplementalDetailCode,
                detailCode,
                detailType,
                improvementYear,
                reproductionCost,
                valuation);
    }

    /// Returns the complete Type-3 detail produced from a Type-5 occurrence.
    ///
    /// The operation retains all source business fields except the mapped type, code, year,
    /// reproduction cost, and a supplemental `YR` code. The conversion clears `YR`.
    ///
    /// @param normalizedReproductionCost converted reproduction cost in whole dollars
    /// @return a replacement Type-3 detail with code `2` and year zero
    public AssessmentDetail convertedFromType5(BigDecimal normalizedReproductionCost) {
        String convertedSupplementalDetailCode =
                "YR".equals(supplementalDetailCode) ? "" : supplementalDetailCode;
        return copy(
                convertedSupplementalDetailCode,
                "2",
                "3",
                0,
                normalizedReproductionCost,
                valuation);
    }

    /// Returns the same detail values with a persistence-assigned optimistic-lock version.
    ///
    /// @param persistedVersion version returned by the database
    /// @return a complete replacement detail
    public AssessmentDetail withVersion(Long persistedVersion) {
        return new AssessmentDetail(
                id,
                persistedVersion,
                age,
                area,
                assessmentClass,
                supplementalDetailCode,
                conditionFactor,
                cornerFactor,
                decimalScale,
                depth,
                depthFactor,
                detailCode,
                detailType,
                extraCornerFactor,
                frontFootage,
                improvementYear,
                keyParcelNumber,
                landConditionFactor,
                multicode,
                occupancyFactor,
                occurrenceNumber,
                parcelNumber,
                parcelVolumeNumber,
                percentAssessed,
                reproductionCost,
                splitCode,
                unitMeasure,
                unitPrice,
                valuation);
    }

    /// Copies the detail while replacing fields changed by valuation preparation.
    private AssessmentDetail copy(
            @Nullable String replacementSupplementalDetailCode,
            String replacementCode,
            String replacementType,
            @Nullable Integer replacementYear,
            @Nullable BigDecimal replacementCost,
            BigDecimal replacementValuation) {
        return new AssessmentDetail(
                id,
                version,
                age,
                area,
                assessmentClass,
                replacementSupplementalDetailCode,
                conditionFactor,
                cornerFactor,
                decimalScale,
                depth,
                depthFactor,
                replacementCode,
                replacementType,
                extraCornerFactor,
                frontFootage,
                replacementYear,
                keyParcelNumber,
                landConditionFactor,
                multicode,
                occupancyFactor,
                occurrenceNumber,
                parcelNumber,
                parcelVolumeNumber,
                percentAssessed,
                replacementCost,
                splitCode,
                unitMeasure,
                unitPrice,
                replacementValuation);
    }
}
