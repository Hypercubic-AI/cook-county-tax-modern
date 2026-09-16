
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.FrozenValuationEntity;

public class FrozenValuationMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static FrozenValuation toDomain(FrozenValuationEntity entity) {
        FrozenValuation model = new FrozenValuation();
        model.setId(entity.getId());

        model.setChangeActionCurrentImprovementValue(entity.getChangeActionCurrentImprovementValue());

        model.setChangeActionCurrentLandValue(entity.getChangeActionCurrentLandValue());

        model.setChangeActionCurrentParcelCount(entity.getChangeActionCurrentParcelCount());

        model.setChangeActionCurrentTotalValue(entity.getChangeActionCurrentTotalValue());

        model.setChangeActionPriorImprovementValue(entity.getChangeActionPriorImprovementValue());

        model.setChangeActionPriorLandValue(entity.getChangeActionPriorLandValue());

        model.setChangeActionPriorParcelCount(entity.getChangeActionPriorParcelCount());

        model.setChangeActionPriorTotalValue(entity.getChangeActionPriorTotalValue());

        model.setCurrentImprovementValue(entity.getCurrentImprovementValue());

        model.setCurrentLandValue(entity.getCurrentLandValue());

        model.setCurrentParcelCount(entity.getCurrentParcelCount());

        model.setCurrentTotalValue(entity.getCurrentTotalValue());

        model.setDivisionNumber(entity.getDivisionNumber());

        model.setNoChangeActionCurrentImprovementValue(entity.getNoChangeActionCurrentImprovementValue());

        model.setNoChangeActionCurrentLandValue(entity.getNoChangeActionCurrentLandValue());

        model.setNoChangeActionCurrentParcelCount(entity.getNoChangeActionCurrentParcelCount());

        model.setNoChangeActionCurrentTotalValue(entity.getNoChangeActionCurrentTotalValue());

        model.setNoChangeActionPriorImprovementValue(entity.getNoChangeActionPriorImprovementValue());

        model.setNoChangeActionPriorLandValue(entity.getNoChangeActionPriorLandValue());

        model.setNoChangeActionPriorParcelCount(entity.getNoChangeActionPriorParcelCount());

        model.setNoChangeActionPriorTotalValue(entity.getNoChangeActionPriorTotalValue());

        model.setPriorImprovementValue(entity.getPriorImprovementValue());

        model.setPriorLandValue(entity.getPriorLandValue());

        model.setPriorParcelCount(entity.getPriorParcelCount());

        model.setPriorTotalValue(entity.getPriorTotalValue());

        model.setProposedActualValue(entity.getProposedActualValue());

        model.setProposedCurrent288Value(entity.getProposedCurrent288Value());

        model.setProposedExpired288Value(entity.getProposedExpired288Value());

        model.setProposedImprovementValue(entity.getProposedImprovementValue());

        model.setProposedTotalValue(entity.getProposedTotalValue());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static FrozenValuationEntity toEntity(FrozenValuation model) {
        FrozenValuationEntity entity = new FrozenValuationEntity();
        entity.setId(model.getId());

        entity.setChangeActionCurrentImprovementValue(model.getChangeActionCurrentImprovementValue());

        entity.setChangeActionCurrentLandValue(model.getChangeActionCurrentLandValue());

        entity.setChangeActionCurrentParcelCount(model.getChangeActionCurrentParcelCount());

        entity.setChangeActionCurrentTotalValue(model.getChangeActionCurrentTotalValue());

        entity.setChangeActionPriorImprovementValue(model.getChangeActionPriorImprovementValue());

        entity.setChangeActionPriorLandValue(model.getChangeActionPriorLandValue());

        entity.setChangeActionPriorParcelCount(model.getChangeActionPriorParcelCount());

        entity.setChangeActionPriorTotalValue(model.getChangeActionPriorTotalValue());

        entity.setCurrentImprovementValue(model.getCurrentImprovementValue());

        entity.setCurrentLandValue(model.getCurrentLandValue());

        entity.setCurrentParcelCount(model.getCurrentParcelCount());

        entity.setCurrentTotalValue(model.getCurrentTotalValue());

        entity.setDivisionNumber(model.getDivisionNumber());

        entity.setNoChangeActionCurrentImprovementValue(model.getNoChangeActionCurrentImprovementValue());

        entity.setNoChangeActionCurrentLandValue(model.getNoChangeActionCurrentLandValue());

        entity.setNoChangeActionCurrentParcelCount(model.getNoChangeActionCurrentParcelCount());

        entity.setNoChangeActionCurrentTotalValue(model.getNoChangeActionCurrentTotalValue());

        entity.setNoChangeActionPriorImprovementValue(model.getNoChangeActionPriorImprovementValue());

        entity.setNoChangeActionPriorLandValue(model.getNoChangeActionPriorLandValue());

        entity.setNoChangeActionPriorParcelCount(model.getNoChangeActionPriorParcelCount());

        entity.setNoChangeActionPriorTotalValue(model.getNoChangeActionPriorTotalValue());

        entity.setPriorImprovementValue(model.getPriorImprovementValue());

        entity.setPriorLandValue(model.getPriorLandValue());

        entity.setPriorParcelCount(model.getPriorParcelCount());

        entity.setPriorTotalValue(model.getPriorTotalValue());

        entity.setProposedActualValue(model.getProposedActualValue());

        entity.setProposedCurrent288Value(model.getProposedCurrent288Value());

        entity.setProposedExpired288Value(model.getProposedExpired288Value());

        entity.setProposedImprovementValue(model.getProposedImprovementValue());

        entity.setProposedTotalValue(model.getProposedTotalValue());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
