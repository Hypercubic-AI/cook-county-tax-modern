package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// One assessment-master parcel and its twelve integer-dollar valuation slots.
///
/// The processing key is township, volume, parcel number, and tax type. The assessed-value stages
/// preserve that source order. Generated persistence identity and version values are absent before
/// the first save. EIFD fields are absent when that separate input has not supplied them.
///
/// @param id generated persistence identity, or `null` before the first save
/// @param version optimistic-lock version, or `null` before the first save
/// @param archivedPreConversionProposedTotal prior proposed total archived by Type-5 conversion, in
///   whole dollars
/// @param assessmentStatus one-character assessment status from the source parcel
/// @param clerkMajorClass one-character Clerk class selected from the Assessor class
/// @param combinedHomeownerNonHomeownerValue combined non-farm bucket, in whole dollars
/// @param currentImprovementValue current improvement value, in whole dollars
/// @param currentLandValue current land value, in whole dollars
/// @param currentTotalValue current parcel total, in whole dollars
/// @param detailQuestionnaireCount number of detail and companion questionnaire occurrences
/// @param farmValue farm bucket, in whole dollars
/// @param overallClass parcel class selected from its ordered details
/// @param parcelNumber source parcel identifier represented by its declared scalar type
/// @param parcelStatus one-character current parcel status
/// @param priorImprovementValue prior improvement value, in whole dollars
/// @param priorLandValue prior land value, in whole dollars
/// @param priorTotalValue prior parcel total, in whole dollars
/// @param proposedImprovementValue proposed improvement value, in whole dollars
/// @param proposedLandValue proposed land value, in whole dollars
/// @param proposedTotalValue proposed parcel total, in whole dollars
/// @param salesSegmentCount number of embedded sale segments
/// @param taxCode code whose leading digits determine township processing order
/// @param taxType one-character source tax type
/// @param volumeNumber source volume within the township
/// @param priorParcelStatus prior parcel status from the separate increment input, when present
/// @param propertyDivisionNumber division key from the separate increment input, when present
/// @param eifdPriorLandValue increment input's prior land value, in whole dollars, when present
/// @param eifdPriorImprovementValue increment input's prior improvement value, in whole dollars,
///   when present
/// @param eifdPriorTotalValue increment input's prior total value, in whole dollars, when present
/// @param eifdCurrentLandValue increment input's current land value, in whole dollars, when present
/// @param eifdCurrentImprovementValue increment input's current improvement value, in whole
///   dollars, when present
/// @param eifdCurrentTotalValue increment input's current total value, in whole dollars, when
///   present
public record AssessmentParcel(
        @Nullable Long id,
        @Nullable Long version,
        BigDecimal archivedPreConversionProposedTotal,
        String assessmentStatus,
        String clerkMajorClass,
        BigDecimal combinedHomeownerNonHomeownerValue,
        BigDecimal currentImprovementValue,
        BigDecimal currentLandValue,
        BigDecimal currentTotalValue,
        Integer detailQuestionnaireCount,
        BigDecimal farmValue,
        Integer overallClass,
        String parcelNumber,
        String parcelStatus,
        BigDecimal priorImprovementValue,
        BigDecimal priorLandValue,
        BigDecimal priorTotalValue,
        BigDecimal proposedImprovementValue,
        BigDecimal proposedLandValue,
        BigDecimal proposedTotalValue,
        Integer salesSegmentCount,
        String taxCode,
        String taxType,
        String volumeNumber,
        @Nullable String priorParcelStatus,
        @Nullable String propertyDivisionNumber,
        @Nullable BigDecimal eifdPriorLandValue,
        @Nullable BigDecimal eifdPriorImprovementValue,
        @Nullable BigDecimal eifdPriorTotalValue,
        @Nullable BigDecimal eifdCurrentLandValue,
        @Nullable BigDecimal eifdCurrentImprovementValue,
        @Nullable BigDecimal eifdCurrentTotalValue) {

    /// Validates canonical source identities and exact whole-unit valuation slots.
    public AssessmentParcel {
        parcelNumber = AssessmentNumericBoundary.signedIdentifier(parcelNumber, 15, "parcelNumber");
        volumeNumber = AssessmentNumericBoundary.signedIdentifier(volumeNumber, 3, "volumeNumber");
        taxCode = AssessmentNumericBoundary.signedIdentifier(taxCode, 5, "taxCode");
        archivedPreConversionProposedTotal =
                wholeUnits(
                        archivedPreConversionProposedTotal, "archivedPreConversionProposedTotal");
        combinedHomeownerNonHomeownerValue =
                wholeUnits(
                        combinedHomeownerNonHomeownerValue, "combinedHomeownerNonHomeownerValue");
        currentImprovementValue = wholeUnits(currentImprovementValue, "currentImprovementValue");
        currentLandValue = wholeUnits(currentLandValue, "currentLandValue");
        currentTotalValue = wholeUnits(currentTotalValue, "currentTotalValue");
        farmValue = wholeUnits(farmValue, "farmValue");
        priorImprovementValue = wholeUnits(priorImprovementValue, "priorImprovementValue");
        priorLandValue = wholeUnits(priorLandValue, "priorLandValue");
        priorTotalValue = wholeUnits(priorTotalValue, "priorTotalValue");
        proposedImprovementValue = wholeUnits(proposedImprovementValue, "proposedImprovementValue");
        proposedLandValue = wholeUnits(proposedLandValue, "proposedLandValue");
        proposedTotalValue = wholeUnits(proposedTotalValue, "proposedTotalValue");
        propertyDivisionNumber =
                propertyDivisionNumber == null
                        ? null
                        : EifdNumericBoundary.identifier(
                                propertyDivisionNumber, 14, "propertyDivisionNumber");
        eifdPriorLandValue = nullableWholeUnits(eifdPriorLandValue, "eifdPriorLandValue");
        eifdPriorImprovementValue =
                nullableWholeUnits(eifdPriorImprovementValue, "eifdPriorImprovementValue");
        eifdPriorTotalValue = nullableWholeUnits(eifdPriorTotalValue, "eifdPriorTotalValue");
        eifdCurrentLandValue = nullableWholeUnits(eifdCurrentLandValue, "eifdCurrentLandValue");
        eifdCurrentImprovementValue =
                nullableWholeUnits(eifdCurrentImprovementValue, "eifdCurrentImprovementValue");
        eifdCurrentTotalValue = nullableWholeUnits(eifdCurrentTotalValue, "eifdCurrentTotalValue");
    }

    /// Returns a complete parcel with a newly selected overall class.
    ///
    /// This operation has no persistence effect.
    ///
    /// @param selectedOverallClass class selected from the parcel's ordered details
    /// @return a replacement parcel that preserves every other value and its version
    public AssessmentParcel withOverallClass(Integer selectedOverallClass) {
        return copy(
                archivedPreConversionProposedTotal,
                clerkMajorClass,
                combinedHomeownerNonHomeownerValue,
                currentLandValue,
                farmValue,
                priorTotalValue,
                selectedOverallClass,
                proposedLandValue,
                proposedImprovementValue,
                proposedTotalValue);
    }

    /// Returns the complete parcel after valuation bucketing.
    ///
    /// The operation clears the source slots required by the accepted batch behavior. It stores
    /// farm value separately and combines the homeowner and non-homeowner values.
    ///
    /// @param selectedClerkMajorClass Clerk major digit selected from the Assessor class
    /// @param selectedFarmValue accumulated farm value in whole dollars
    /// @param selectedCombinedValue homeowner plus non-homeowner value in whole dollars
    /// @return a replacement parcel that preserves unrelated values and its version
    public AssessmentParcel withBucketing(
            String selectedClerkMajorClass,
            BigDecimal selectedFarmValue,
            BigDecimal selectedCombinedValue) {
        return copy(
                BigDecimal.ZERO,
                selectedClerkMajorClass,
                selectedCombinedValue,
                BigDecimal.ZERO,
                selectedFarmValue,
                BigDecimal.ZERO,
                overallClass,
                proposedLandValue,
                proposedImprovementValue,
                proposedTotalValue);
    }

    /// Returns the complete parcel after Type-5 conversion revalues its details.
    ///
    /// @param landValue recomputed proposed land value in whole dollars
    /// @param improvementValue recomputed proposed improvement value in whole dollars
    /// @return a replacement parcel with the prior proposed total archived in slot twelve
    public AssessmentParcel withProposedValuation(
            BigDecimal landValue, BigDecimal improvementValue) {
        return copy(
                proposedTotalValue,
                clerkMajorClass,
                combinedHomeownerNonHomeownerValue,
                currentLandValue,
                farmValue,
                priorTotalValue,
                overallClass,
                landValue,
                improvementValue,
                landValue.add(improvementValue));
    }

    /// Returns the same parcel values with a persistence-assigned optimistic-lock version.
    ///
    /// @param persistedVersion version returned by the database
    /// @return a complete replacement parcel
    public AssessmentParcel withVersion(Long persistedVersion) {
        return new AssessmentParcel(
                id,
                persistedVersion,
                archivedPreConversionProposedTotal,
                assessmentStatus,
                clerkMajorClass,
                combinedHomeownerNonHomeownerValue,
                currentImprovementValue,
                currentLandValue,
                currentTotalValue,
                detailQuestionnaireCount,
                farmValue,
                overallClass,
                parcelNumber,
                parcelStatus,
                priorImprovementValue,
                priorLandValue,
                priorTotalValue,
                proposedImprovementValue,
                proposedLandValue,
                proposedTotalValue,
                salesSegmentCount,
                taxCode,
                taxType,
                volumeNumber,
                priorParcelStatus,
                propertyDivisionNumber,
                eifdPriorLandValue,
                eifdPriorImprovementValue,
                eifdPriorTotalValue,
                eifdCurrentLandValue,
                eifdCurrentImprovementValue,
                eifdCurrentTotalValue);
    }

    /// Copies the parcel while replacing the fields changed by assessed-value stages.
    private AssessmentParcel copy(
            BigDecimal archivedTotal,
            String clerkClass,
            BigDecimal combinedValue,
            BigDecimal currentLand,
            BigDecimal farm,
            BigDecimal replacementPriorTotal,
            Integer selectedOverallClass,
            BigDecimal proposedLand,
            BigDecimal proposedImprovement,
            BigDecimal proposedTotal) {
        return new AssessmentParcel(
                id,
                version,
                archivedTotal,
                assessmentStatus,
                clerkClass,
                combinedValue,
                currentImprovementValue,
                currentLand,
                currentTotalValue,
                detailQuestionnaireCount,
                farm,
                selectedOverallClass,
                parcelNumber,
                parcelStatus,
                priorImprovementValue,
                priorLandValue,
                replacementPriorTotal,
                proposedImprovement,
                proposedLand,
                proposedTotal,
                salesSegmentCount,
                taxCode,
                taxType,
                volumeNumber,
                priorParcelStatus,
                propertyDivisionNumber,
                eifdPriorLandValue,
                eifdPriorImprovementValue,
                eifdPriorTotalValue,
                eifdCurrentLandValue,
                eifdCurrentImprovementValue,
                eifdCurrentTotalValue);
    }

    /// Applies the signed nine-digit whole-unit source contract to a required value.
    private static BigDecimal wholeUnits(BigDecimal value, String name) {
        return AssessmentNumericBoundary.wholeUnits(value, 9, name);
    }

    /// Applies the whole-unit source contract while preserving an absent EIFD value.
    private static @Nullable BigDecimal nullableWholeUnits(
            @Nullable BigDecimal value, String name) {
        return value == null ? null : wholeUnits(value, name);
    }
}
