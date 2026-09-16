package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.FrozenValuationEntity;

import java.util.Objects;

/// Canonical, complete mapping between the immutable domain snapshot and its persistence entity.
public final class FrozenValuationMapper {

    private FrozenValuationMapper() {}

    /// Hydrates every domain component, including the persistence identifier and version.
    public static FrozenValuation toDomain(FrozenValuationEntity entity) {
        return new FrozenValuation(
                entity.getId(),
                entity.getVersion(),
                Objects.requireNonNull(
                        entity.getChangeActionCurrentImprovementValue(),
                        "changeActionCurrentImprovementValue"),
                Objects.requireNonNull(
                        entity.getChangeActionCurrentLandValue(), "changeActionCurrentLandValue"),
                Objects.requireNonNull(
                        entity.getChangeActionCurrentParcelCount(),
                        "changeActionCurrentParcelCount"),
                Objects.requireNonNull(
                        entity.getChangeActionCurrentTotalValue(), "changeActionCurrentTotalValue"),
                Objects.requireNonNull(
                        entity.getChangeActionPriorImprovementValue(),
                        "changeActionPriorImprovementValue"),
                Objects.requireNonNull(
                        entity.getChangeActionPriorLandValue(), "changeActionPriorLandValue"),
                Objects.requireNonNull(
                        entity.getChangeActionPriorParcelCount(), "changeActionPriorParcelCount"),
                Objects.requireNonNull(
                        entity.getChangeActionPriorTotalValue(), "changeActionPriorTotalValue"),
                Objects.requireNonNull(
                        entity.getCurrentImprovementValue(), "currentImprovementValue"),
                Objects.requireNonNull(entity.getCurrentLandValue(), "currentLandValue"),
                Objects.requireNonNull(entity.getCurrentParcelCount(), "currentParcelCount"),
                Objects.requireNonNull(entity.getCurrentTotalValue(), "currentTotalValue"),
                Objects.requireNonNull(entity.getDivisionNumber(), "divisionNumber"),
                Objects.requireNonNull(
                        entity.getNoChangeActionCurrentImprovementValue(),
                        "noChangeActionCurrentImprovementValue"),
                Objects.requireNonNull(
                        entity.getNoChangeActionCurrentLandValue(),
                        "noChangeActionCurrentLandValue"),
                Objects.requireNonNull(
                        entity.getNoChangeActionCurrentParcelCount(),
                        "noChangeActionCurrentParcelCount"),
                Objects.requireNonNull(
                        entity.getNoChangeActionCurrentTotalValue(),
                        "noChangeActionCurrentTotalValue"),
                Objects.requireNonNull(
                        entity.getNoChangeActionPriorImprovementValue(),
                        "noChangeActionPriorImprovementValue"),
                Objects.requireNonNull(
                        entity.getNoChangeActionPriorLandValue(), "noChangeActionPriorLandValue"),
                Objects.requireNonNull(
                        entity.getNoChangeActionPriorParcelCount(),
                        "noChangeActionPriorParcelCount"),
                Objects.requireNonNull(
                        entity.getNoChangeActionPriorTotalValue(), "noChangeActionPriorTotalValue"),
                Objects.requireNonNull(entity.getPriorImprovementValue(), "priorImprovementValue"),
                Objects.requireNonNull(entity.getPriorLandValue(), "priorLandValue"),
                Objects.requireNonNull(entity.getPriorParcelCount(), "priorParcelCount"),
                Objects.requireNonNull(entity.getPriorTotalValue(), "priorTotalValue"),
                Objects.requireNonNull(entity.getProposedActualValue(), "proposedActualValue"),
                Objects.requireNonNull(
                        entity.getProposedCurrent288Value(), "proposedCurrent288Value"),
                Objects.requireNonNull(
                        entity.getProposedExpired288Value(), "proposedExpired288Value"),
                Objects.requireNonNull(
                        entity.getProposedImprovementValue(), "proposedImprovementValue"),
                Objects.requireNonNull(entity.getProposedTotalValue(), "proposedTotalValue"));
    }

    /// Copies every domain component to a mutable entity for persistence.
    public static FrozenValuationEntity toEntity(FrozenValuation model) {
        FrozenValuationEntity entity = new FrozenValuationEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setChangeActionCurrentImprovementValue(model.changeActionCurrentImprovementValue());
        entity.setChangeActionCurrentLandValue(model.changeActionCurrentLandValue());
        entity.setChangeActionCurrentParcelCount(model.changeActionCurrentParcelCount());
        entity.setChangeActionCurrentTotalValue(model.changeActionCurrentTotalValue());
        entity.setChangeActionPriorImprovementValue(model.changeActionPriorImprovementValue());
        entity.setChangeActionPriorLandValue(model.changeActionPriorLandValue());
        entity.setChangeActionPriorParcelCount(model.changeActionPriorParcelCount());
        entity.setChangeActionPriorTotalValue(model.changeActionPriorTotalValue());
        entity.setCurrentImprovementValue(model.currentImprovementValue());
        entity.setCurrentLandValue(model.currentLandValue());
        entity.setCurrentParcelCount(model.currentParcelCount());
        entity.setCurrentTotalValue(model.currentTotalValue());
        entity.setDivisionNumber(model.divisionNumber());
        entity.setNoChangeActionCurrentImprovementValue(
                model.noChangeActionCurrentImprovementValue());
        entity.setNoChangeActionCurrentLandValue(model.noChangeActionCurrentLandValue());
        entity.setNoChangeActionCurrentParcelCount(model.noChangeActionCurrentParcelCount());
        entity.setNoChangeActionCurrentTotalValue(model.noChangeActionCurrentTotalValue());
        entity.setNoChangeActionPriorImprovementValue(model.noChangeActionPriorImprovementValue());
        entity.setNoChangeActionPriorLandValue(model.noChangeActionPriorLandValue());
        entity.setNoChangeActionPriorParcelCount(model.noChangeActionPriorParcelCount());
        entity.setNoChangeActionPriorTotalValue(model.noChangeActionPriorTotalValue());
        entity.setPriorImprovementValue(model.priorImprovementValue());
        entity.setPriorLandValue(model.priorLandValue());
        entity.setPriorParcelCount(model.priorParcelCount());
        entity.setPriorTotalValue(model.priorTotalValue());
        entity.setProposedActualValue(model.proposedActualValue());
        entity.setProposedCurrent288Value(model.proposedCurrent288Value());
        entity.setProposedExpired288Value(model.proposedExpired288Value());
        entity.setProposedImprovementValue(model.proposedImprovementValue());
        entity.setProposedTotalValue(model.proposedTotalValue());
        return entity;
    }
}
