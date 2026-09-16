
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentDetailEntity;

public class AssessmentDetailMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static AssessmentDetail toDomain(AssessmentDetailEntity entity) {
        AssessmentDetail model = new AssessmentDetail();
        model.setId(entity.getId());

        model.setAge(entity.getAge());

        model.setArea(entity.getArea());

        model.setAssessmentClass(entity.getAssessmentClass());

        model.setCdu(entity.getCdu());

        model.setConditionFactor(entity.getConditionFactor());

        model.setCornerFactor(entity.getCornerFactor());

        model.setDecimalScale(entity.getDecimalScale());

        model.setDepth(entity.getDepth());

        model.setDepthFactor(entity.getDepthFactor());

        model.setDetailCode(entity.getDetailCode());

        model.setDetailType(entity.getDetailType());

        model.setExtraCornerFactor(entity.getExtraCornerFactor());

        model.setFrontFootage(entity.getFrontFootage());

        model.setImprovementYear(entity.getImprovementYear());

        model.setKeyParcelNumber(entity.getKeyParcelNumber());

        model.setLandConditionFactor(entity.getLandConditionFactor());

        model.setMulticode(entity.getMulticode());

        model.setOccupancyFactor(entity.getOccupancyFactor());

        model.setOccurrenceNumber(entity.getOccurrenceNumber());

        model.setParcelNumber(entity.getParcelNumber());

        model.setParcelVolumeNumber(entity.getParcelVolumeNumber());

        model.setPercentAssessed(entity.getPercentAssessed());

        model.setReproductionCost(entity.getReproductionCost());

        model.setSplitCode(entity.getSplitCode());

        model.setUnitMeasure(entity.getUnitMeasure());

        model.setUnitPrice(entity.getUnitPrice());

        model.setValuation(entity.getValuation());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static AssessmentDetailEntity toEntity(AssessmentDetail model) {
        AssessmentDetailEntity entity = new AssessmentDetailEntity();
        entity.setId(model.getId());

        entity.setAge(model.getAge());

        entity.setArea(model.getArea());

        entity.setAssessmentClass(model.getAssessmentClass());

        entity.setCdu(model.getCdu());

        entity.setConditionFactor(model.getConditionFactor());

        entity.setCornerFactor(model.getCornerFactor());

        entity.setDecimalScale(model.getDecimalScale());

        entity.setDepth(model.getDepth());

        entity.setDepthFactor(model.getDepthFactor());

        entity.setDetailCode(model.getDetailCode());

        entity.setDetailType(model.getDetailType());

        entity.setExtraCornerFactor(model.getExtraCornerFactor());

        entity.setFrontFootage(model.getFrontFootage());

        entity.setImprovementYear(model.getImprovementYear());

        entity.setKeyParcelNumber(model.getKeyParcelNumber());

        entity.setLandConditionFactor(model.getLandConditionFactor());

        entity.setMulticode(model.getMulticode());

        entity.setOccupancyFactor(model.getOccupancyFactor());

        entity.setOccurrenceNumber(model.getOccurrenceNumber());

        entity.setParcelNumber(model.getParcelNumber());

        entity.setParcelVolumeNumber(model.getParcelVolumeNumber());

        entity.setPercentAssessed(model.getPercentAssessed());

        entity.setReproductionCost(model.getReproductionCost());

        entity.setSplitCode(model.getSplitCode());

        entity.setUnitMeasure(model.getUnitMeasure());

        entity.setUnitPrice(model.getUnitPrice());

        entity.setValuation(model.getValuation());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
