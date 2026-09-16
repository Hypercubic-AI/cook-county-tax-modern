package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AssessmentDetailEntity;

/// Maps every assessment-detail field across the mutable persistence boundary.
public final class AssessmentDetailMapper {

    private AssessmentDetailMapper() {}

    /// Creates an immutable detail from one fully hydrated persistence entity.
    ///
    /// @param entity hydrated database entity
    /// @return a detail that preserves identity, optimistic-lock version, and all valuation fields
    public static AssessmentDetail toDomain(AssessmentDetailEntity entity) {
        return new AssessmentDetail(
                entity.getId(),
                entity.getVersion(),
                entity.getAge(),
                entity.getArea(),
                requireNonNull(entity.getAssessmentClass(), "assessment detail class"),
                entity.getSupplementalDetailCode(),
                entity.getConditionFactor(),
                entity.getCornerFactor(),
                entity.getDecimalScale(),
                entity.getDepth(),
                entity.getDepthFactor(),
                requireNonNull(entity.getDetailCode(), "assessment detail code"),
                requireNonNull(entity.getDetailType(), "assessment detail type"),
                entity.getExtraCornerFactor(),
                entity.getFrontFootage(),
                entity.getImprovementYear(),
                entity.getKeyParcelNumber(),
                entity.getLandConditionFactor(),
                requireNonNull(entity.getMulticode(), "assessment detail multicode"),
                entity.getOccupancyFactor(),
                requireNonNull(entity.getOccurrenceNumber(), "assessment detail occurrence"),
                requireNonNull(entity.getParcelNumber(), "assessment detail parcel number"),
                requireNonNull(entity.getParcelVolumeNumber(), "assessment detail parcel volume"),
                entity.getPercentAssessed(),
                entity.getReproductionCost(),
                entity.getSplitCode(),
                entity.getUnitMeasure(),
                entity.getUnitPrice(),
                requireNonNull(entity.getValuation(), "assessment detail valuation"));
    }

    /// Creates a mutable entity for one immutable detail.
    ///
    /// @param model detail to persist
    /// @return an entity that preserves identity, optimistic-lock version, and all valuation fields
    public static AssessmentDetailEntity toEntity(AssessmentDetail model) {
        var entity = new AssessmentDetailEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setAge(model.age());
        entity.setArea(model.area());
        entity.setAssessmentClass(model.assessmentClass());
        entity.setSupplementalDetailCode(model.supplementalDetailCode());
        entity.setConditionFactor(model.conditionFactor());
        entity.setCornerFactor(model.cornerFactor());
        entity.setDecimalScale(model.decimalScale());
        entity.setDepth(model.depth());
        entity.setDepthFactor(model.depthFactor());
        entity.setDetailCode(model.detailCode());
        entity.setDetailType(model.detailType());
        entity.setExtraCornerFactor(model.extraCornerFactor());
        entity.setFrontFootage(model.frontFootage());
        entity.setImprovementYear(model.improvementYear());
        entity.setKeyParcelNumber(model.keyParcelNumber());
        entity.setLandConditionFactor(model.landConditionFactor());
        entity.setMulticode(model.multicode());
        entity.setOccupancyFactor(model.occupancyFactor());
        entity.setOccurrenceNumber(model.occurrenceNumber());
        entity.setParcelNumber(model.parcelNumber());
        entity.setParcelVolumeNumber(model.parcelVolumeNumber());
        entity.setPercentAssessed(model.percentAssessed());
        entity.setReproductionCost(model.reproductionCost());
        entity.setSplitCode(model.splitCode());
        entity.setUnitMeasure(model.unitMeasure());
        entity.setUnitPrice(model.unitPrice());
        entity.setValuation(model.valuation());
        return entity;
    }
}
