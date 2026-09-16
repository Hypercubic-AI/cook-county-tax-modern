
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentParcelEntity;

public class AssessmentParcelMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static AssessmentParcel toDomain(AssessmentParcelEntity entity) {
        AssessmentParcel model = new AssessmentParcel();
        model.setId(entity.getId());

        model.setArchivedPreConversionProposedTotal(entity.getArchivedPreConversionProposedTotal());

        model.setAssessmentStatus(entity.getAssessmentStatus());

        model.setClerkMajorClass(entity.getClerkMajorClass());

        model.setCombinedHomeownerNonHomeownerValue(entity.getCombinedHomeownerNonHomeownerValue());

        model.setCurrentImprovementValue(entity.getCurrentImprovementValue());

        model.setCurrentLandValue(entity.getCurrentLandValue());

        model.setCurrentTotalValue(entity.getCurrentTotalValue());

        model.setDetailQuestionnaireCount(entity.getDetailQuestionnaireCount());

        model.setFarmValue(entity.getFarmValue());

        model.setOverallClass(entity.getOverallClass());

        model.setParcelNumber(entity.getParcelNumber());

        model.setParcelStatus(entity.getParcelStatus());

        model.setPriorImprovementValue(entity.getPriorImprovementValue());

        model.setPriorLandValue(entity.getPriorLandValue());

        model.setPriorTotalValue(entity.getPriorTotalValue());


        model.setProposedImprovementValue(entity.getProposedImprovementValue());

        model.setProposedLandValue(entity.getProposedLandValue());

        model.setProposedTotalValue(entity.getProposedTotalValue());

        model.setSalesSegmentCount(entity.getSalesSegmentCount());

        model.setTaxCode(entity.getTaxCode());

        model.setTaxType(entity.getTaxType());

        model.setVolumeNumber(entity.getVolumeNumber());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static AssessmentParcelEntity toEntity(AssessmentParcel model) {
        AssessmentParcelEntity entity = new AssessmentParcelEntity();
        entity.setId(model.getId());

        entity.setArchivedPreConversionProposedTotal(model.getArchivedPreConversionProposedTotal());

        entity.setAssessmentStatus(model.getAssessmentStatus());

        entity.setClerkMajorClass(model.getClerkMajorClass());

        entity.setCombinedHomeownerNonHomeownerValue(model.getCombinedHomeownerNonHomeownerValue());

        entity.setCurrentImprovementValue(model.getCurrentImprovementValue());

        entity.setCurrentLandValue(model.getCurrentLandValue());

        entity.setCurrentTotalValue(model.getCurrentTotalValue());

        entity.setDetailQuestionnaireCount(model.getDetailQuestionnaireCount());

        entity.setFarmValue(model.getFarmValue());

        entity.setOverallClass(model.getOverallClass());

        entity.setParcelNumber(model.getParcelNumber());

        entity.setParcelStatus(model.getParcelStatus());

        entity.setPriorImprovementValue(model.getPriorImprovementValue());

        entity.setPriorLandValue(model.getPriorLandValue());

        entity.setPriorTotalValue(model.getPriorTotalValue());


        entity.setProposedImprovementValue(model.getProposedImprovementValue());

        entity.setProposedLandValue(model.getProposedLandValue());

        entity.setProposedTotalValue(model.getProposedTotalValue());

        entity.setSalesSegmentCount(model.getSalesSegmentCount());

        entity.setTaxCode(model.getTaxCode());

        entity.setTaxType(model.getTaxType());

        entity.setVolumeNumber(model.getVolumeNumber());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end

    public static AssessmentParcel toDomainWithPriorParcelStatus(AssessmentParcelEntity entity) {
        AssessmentParcel model = toDomain(entity);
        model.setPriorParcelStatus(entity.getPriorParcelStatus());
        model.setEifdPriorLandValue(entity.getEifdPriorLandValue());
        model.setEifdPriorImprovementValue(entity.getEifdPriorImprovementValue());
        model.setEifdPriorTotalValue(entity.getEifdPriorTotalValue());
        model.setEifdCurrentLandValue(entity.getEifdCurrentLandValue());
        model.setEifdCurrentImprovementValue(entity.getEifdCurrentImprovementValue());
        model.setEifdCurrentTotalValue(entity.getEifdCurrentTotalValue());
        return model;
    }
}
