
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.FrozenAgencyAdjustmentEntity;

public class FrozenAgencyAdjustmentMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static FrozenAgencyAdjustment toDomain(FrozenAgencyAdjustmentEntity entity) {
        FrozenAgencyAdjustment model = new FrozenAgencyAdjustment();
        model.setId(entity.getId());

        model.setAgencyNumber(entity.getAgencyNumber());

        model.setAnnexedAssessedValue(entity.getAnnexedAssessedValue());

        model.setAnnexedEqualizedValue(entity.getAnnexedEqualizedValue());

        model.setCurrent288Value(entity.getCurrent288Value());

        model.setDisconnectedAssessedValue(entity.getDisconnectedAssessedValue());

        model.setDisconnectedEqualizedValue(entity.getDisconnectedEqualizedValue());

        model.setExpired288Value(entity.getExpired288Value());

        model.setExpiredIncentiveEqualizedValue(entity.getExpiredIncentiveEqualizedValue());

        model.setExpiredIncentiveTaxAmount(entity.getExpiredIncentiveTaxAmount());

        model.setExpiredIncentiveValue(entity.getExpiredIncentiveValue());

        model.setFirstTimeValue(entity.getFirstTimeValue());

        model.setFrozenEqualizedValue(entity.getFrozenEqualizedValue());

        model.setFrozenTaxAmount(entity.getFrozenTaxAmount());

        model.setTaxCode(entity.getTaxCode());

        model.setTaxRate(entity.getTaxRate());

        model.setTifCurrentEqualizedValue(entity.getTifCurrentEqualizedValue());

        model.setTifDifferenceEqualizedValue(entity.getTifDifferenceEqualizedValue());

        model.setTifPriorFrozenEqualizedValue(entity.getTifPriorFrozenEqualizedValue());

        model.setTotalFrozenValue(entity.getTotalFrozenValue());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static FrozenAgencyAdjustmentEntity toEntity(FrozenAgencyAdjustment model) {
        FrozenAgencyAdjustmentEntity entity = new FrozenAgencyAdjustmentEntity();
        entity.setId(model.getId());

        entity.setAgencyNumber(model.getAgencyNumber());

        entity.setAnnexedAssessedValue(model.getAnnexedAssessedValue());

        entity.setAnnexedEqualizedValue(model.getAnnexedEqualizedValue());

        entity.setCurrent288Value(model.getCurrent288Value());

        entity.setDisconnectedAssessedValue(model.getDisconnectedAssessedValue());

        entity.setDisconnectedEqualizedValue(model.getDisconnectedEqualizedValue());

        entity.setExpired288Value(model.getExpired288Value());

        entity.setExpiredIncentiveEqualizedValue(model.getExpiredIncentiveEqualizedValue());

        entity.setExpiredIncentiveTaxAmount(model.getExpiredIncentiveTaxAmount());

        entity.setExpiredIncentiveValue(model.getExpiredIncentiveValue());

        entity.setFirstTimeValue(model.getFirstTimeValue());

        entity.setFrozenEqualizedValue(model.getFrozenEqualizedValue());

        entity.setFrozenTaxAmount(model.getFrozenTaxAmount());

        entity.setTaxCode(model.getTaxCode());

        entity.setTaxRate(model.getTaxRate());

        entity.setTifCurrentEqualizedValue(model.getTifCurrentEqualizedValue());

        entity.setTifDifferenceEqualizedValue(model.getTifDifferenceEqualizedValue());

        entity.setTifPriorFrozenEqualizedValue(model.getTifPriorFrozenEqualizedValue());

        entity.setTotalFrozenValue(model.getTotalFrozenValue());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
