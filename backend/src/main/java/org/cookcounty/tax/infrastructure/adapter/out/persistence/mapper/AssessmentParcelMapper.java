package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentParcelEntity;

/// Maps every parcel field across the mutable persistence boundary.
public final class AssessmentParcelMapper {

    private AssessmentParcelMapper() {}

    /// Creates an immutable parcel from one fully hydrated persistence entity.
    ///
    /// @param entity hydrated database entity
    /// @return a parcel that preserves all twelve value slots, extended values, and version
    public static AssessmentParcel toDomain(AssessmentParcelEntity entity) {
        return new AssessmentParcel(
                entity.getId(),
                entity.getVersion(),
                requireNonNull(
                        entity.getArchivedPreConversionProposedTotal(), "archived proposed total"),
                requireNonNull(entity.getAssessmentStatus(), "assessment status"),
                requireNonNull(entity.getClerkMajorClass(), "Clerk major class"),
                requireNonNull(
                        entity.getCombinedHomeownerNonHomeownerValue(), "combined valuation"),
                requireNonNull(entity.getCurrentImprovementValue(), "current improvement value"),
                requireNonNull(entity.getCurrentLandValue(), "current land value"),
                requireNonNull(entity.getCurrentTotalValue(), "current total value"),
                requireNonNull(entity.getDetailQuestionnaireCount(), "detail count"),
                requireNonNull(entity.getFarmValue(), "farm value"),
                requireNonNull(entity.getOverallClass(), "overall class"),
                requireNonNull(entity.getParcelNumber(), "parcel number"),
                requireNonNull(entity.getParcelStatus(), "parcel status"),
                requireNonNull(entity.getPriorImprovementValue(), "prior improvement value"),
                requireNonNull(entity.getPriorLandValue(), "prior land value"),
                requireNonNull(entity.getPriorTotalValue(), "prior total value"),
                requireNonNull(entity.getProposedImprovementValue(), "proposed improvement value"),
                requireNonNull(entity.getProposedLandValue(), "proposed land value"),
                requireNonNull(entity.getProposedTotalValue(), "proposed total value"),
                requireNonNull(entity.getSalesSegmentCount(), "sales segment count"),
                requireNonNull(entity.getTaxCode(), "tax code"),
                requireNonNull(entity.getTaxType(), "tax type"),
                requireNonNull(entity.getVolumeNumber(), "volume number"),
                entity.getPriorParcelStatus(),
                entity.getPropertyDivisionNumber(),
                entity.getEifdPriorLandValue(),
                entity.getEifdPriorImprovementValue(),
                entity.getEifdPriorTotalValue(),
                entity.getEifdCurrentLandValue(),
                entity.getEifdCurrentImprovementValue(),
                entity.getEifdCurrentTotalValue());
    }

    /// Creates a mutable entity for one immutable parcel.
    ///
    /// @param model parcel to persist
    /// @return an entity that preserves all twelve value slots, extended values, and version
    public static AssessmentParcelEntity toEntity(AssessmentParcel model) {
        var entity = new AssessmentParcelEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setArchivedPreConversionProposedTotal(model.archivedPreConversionProposedTotal());
        entity.setAssessmentStatus(model.assessmentStatus());
        entity.setClerkMajorClass(model.clerkMajorClass());
        entity.setCombinedHomeownerNonHomeownerValue(model.combinedHomeownerNonHomeownerValue());
        entity.setCurrentImprovementValue(model.currentImprovementValue());
        entity.setCurrentLandValue(model.currentLandValue());
        entity.setCurrentTotalValue(model.currentTotalValue());
        entity.setDetailQuestionnaireCount(model.detailQuestionnaireCount());
        entity.setFarmValue(model.farmValue());
        entity.setOverallClass(model.overallClass());
        entity.setParcelNumber(model.parcelNumber());
        entity.setParcelStatus(model.parcelStatus());
        entity.setPriorImprovementValue(model.priorImprovementValue());
        entity.setPriorLandValue(model.priorLandValue());
        entity.setPriorTotalValue(model.priorTotalValue());
        entity.setProposedImprovementValue(model.proposedImprovementValue());
        entity.setProposedLandValue(model.proposedLandValue());
        entity.setProposedTotalValue(model.proposedTotalValue());
        entity.setSalesSegmentCount(model.salesSegmentCount());
        entity.setTaxCode(model.taxCode());
        entity.setTaxType(model.taxType());
        entity.setVolumeNumber(model.volumeNumber());
        entity.setPriorParcelStatus(model.priorParcelStatus());
        entity.setPropertyDivisionNumber(model.propertyDivisionNumber());
        entity.setEifdPriorLandValue(model.eifdPriorLandValue());
        entity.setEifdPriorImprovementValue(model.eifdPriorImprovementValue());
        entity.setEifdPriorTotalValue(model.eifdPriorTotalValue());
        entity.setEifdCurrentLandValue(model.eifdCurrentLandValue());
        entity.setEifdCurrentImprovementValue(model.eifdCurrentImprovementValue());
        entity.setEifdCurrentTotalValue(model.eifdCurrentTotalValue());
        return entity;
    }
}
