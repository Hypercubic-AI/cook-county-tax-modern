
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.SeniorFreezeMaster;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.SeniorFreezeMasterEntity;

public class SeniorFreezeMasterMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static SeniorFreezeMaster toDomain(SeniorFreezeMasterEntity entity) {
        SeniorFreezeMaster model = new SeniorFreezeMaster();
        model.setId(entity.getId());

        model.setBaseValueManualCalculationIndicator(entity.getBaseValueManualCalculationIndicator());

        model.setBaseValueNoCalculationIndicator(entity.getBaseValueNoCalculationIndicator());

        model.setBaseValueYear(entity.getBaseValueYear());

        model.setBaseValueYearClass(entity.getBaseValueYearClass());

        model.setBaseYearEligibleComputedFullAssessedValue(entity.getBaseYearEligibleComputedFullAssessedValue());

        model.setBaseYearEqualizedValue(entity.getBaseYearEqualizedValue());

        model.setBaseYearFullAssessedValue(entity.getBaseYearFullAssessedValue());

        model.setBaseYearTotalEligibleComputedEqualizedValue(entity.getBaseYearTotalEligibleComputedEqualizedValue());

        model.setBuildingShares(entity.getBuildingShares());

        model.setBuildingUnits(entity.getBuildingUnits());

        model.setCalculationType(entity.getCalculationType());

        model.setClass288ExpirationAssessedValue(entity.getClass288ExpirationAssessedValue());

        model.setClass288ExpirationEqualizedValue(entity.getClass288ExpirationEqualizedValue());

        model.setClass288OverLimitAssessedValue(entity.getClass288OverLimitAssessedValue());

        model.setClass288OverLimitEqualizedValue(entity.getClass288OverLimitEqualizedValue());

        model.setCurrentYearClass(entity.getCurrentYearClass());

        model.setCurrentYearEligibleComputedAssessedValue(entity.getCurrentYearEligibleComputedAssessedValue());

        model.setCurrentYearEligibleComputedEqualizedValue(entity.getCurrentYearEligibleComputedEqualizedValue());

        model.setCurrentYearFarmIndicator(entity.getCurrentYearFarmIndicator());

        model.setCurrentYearFinalEqualizedValueDifference(entity.getCurrentYearFinalEqualizedValueDifference());

        model.setCurrentYearFullAssessedValue(entity.getCurrentYearFullAssessedValue());

        model.setCurrentYearFullEqualizedValue(entity.getCurrentYearFullEqualizedValue());

        model.setCurrentYearNotEligibleAssessedValue(entity.getCurrentYearNotEligibleAssessedValue());

        model.setCurrentYearNotEligibleEqualizedValue(entity.getCurrentYearNotEligibleEqualizedValue());

        model.setHomeownerUnits(entity.getHomeownerUnits());

        model.setHomesteadUnits(entity.getHomesteadUnits());

        model.setKeyParcelNumber(entity.getKeyParcelNumber());

        model.setMailingCity(entity.getMailingCity());

        model.setMailingDirection(entity.getMailingDirection());

        model.setMailingHouseNumber(entity.getMailingHouseNumber());

        model.setMailingState(entity.getMailingState());

        model.setMailingStreet(entity.getMailingStreet());

        model.setMailingSuffix(entity.getMailingSuffix());

        model.setMailingZipCode(entity.getMailingZipCode());

        model.setMaintenanceIndicator(entity.getMaintenanceIndicator());

        model.setMasterName(entity.getMasterName());

        model.setOccupancyFactor(entity.getOccupancyFactor());

        model.setOriginalBaseValueYear(entity.getOriginalBaseValueYear());

        model.setOriginalBaseYearEligibleComputedFullAssessedValue(entity.getOriginalBaseYearEligibleComputedFullAssessedValue());

        model.setOriginalBaseYearEqualizedValue(entity.getOriginalBaseYearEqualizedValue());

        model.setOriginalBaseYearFullAssessedValue(entity.getOriginalBaseYearFullAssessedValue());

        model.setOriginalBaseYearTotalEligibleComputedEqualizedValue(entity.getOriginalBaseYearTotalEligibleComputedEqualizedValue());

        model.setOriginalCurrentYearFinalEqualizedValueDifference(entity.getOriginalCurrentYearFinalEqualizedValueDifference());

        model.setOriginalManualCalculationIndicator(entity.getOriginalManualCalculationIndicator());

        model.setPropertyProration(entity.getPropertyProration());

        model.setRecordCode(entity.getRecordCode());

        model.setSeniorFreezeShares(entity.getSeniorFreezeShares());

        model.setSplitCode(entity.getSplitCode());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static SeniorFreezeMasterEntity toEntity(SeniorFreezeMaster model) {
        SeniorFreezeMasterEntity entity = new SeniorFreezeMasterEntity();
        entity.setId(model.getId());

        entity.setBaseValueManualCalculationIndicator(model.getBaseValueManualCalculationIndicator());

        entity.setBaseValueNoCalculationIndicator(model.getBaseValueNoCalculationIndicator());

        entity.setBaseValueYear(model.getBaseValueYear());

        entity.setBaseValueYearClass(model.getBaseValueYearClass());

        entity.setBaseYearEligibleComputedFullAssessedValue(model.getBaseYearEligibleComputedFullAssessedValue());

        entity.setBaseYearEqualizedValue(model.getBaseYearEqualizedValue());

        entity.setBaseYearFullAssessedValue(model.getBaseYearFullAssessedValue());

        entity.setBaseYearTotalEligibleComputedEqualizedValue(model.getBaseYearTotalEligibleComputedEqualizedValue());

        entity.setBuildingShares(model.getBuildingShares());

        entity.setBuildingUnits(model.getBuildingUnits());

        entity.setCalculationType(model.getCalculationType());

        entity.setClass288ExpirationAssessedValue(model.getClass288ExpirationAssessedValue());

        entity.setClass288ExpirationEqualizedValue(model.getClass288ExpirationEqualizedValue());

        entity.setClass288OverLimitAssessedValue(model.getClass288OverLimitAssessedValue());

        entity.setClass288OverLimitEqualizedValue(model.getClass288OverLimitEqualizedValue());

        entity.setCurrentYearClass(model.getCurrentYearClass());

        entity.setCurrentYearEligibleComputedAssessedValue(model.getCurrentYearEligibleComputedAssessedValue());

        entity.setCurrentYearEligibleComputedEqualizedValue(model.getCurrentYearEligibleComputedEqualizedValue());

        entity.setCurrentYearFarmIndicator(model.getCurrentYearFarmIndicator());

        entity.setCurrentYearFinalEqualizedValueDifference(model.getCurrentYearFinalEqualizedValueDifference());

        entity.setCurrentYearFullAssessedValue(model.getCurrentYearFullAssessedValue());

        entity.setCurrentYearFullEqualizedValue(model.getCurrentYearFullEqualizedValue());

        entity.setCurrentYearNotEligibleAssessedValue(model.getCurrentYearNotEligibleAssessedValue());

        entity.setCurrentYearNotEligibleEqualizedValue(model.getCurrentYearNotEligibleEqualizedValue());

        entity.setHomeownerUnits(model.getHomeownerUnits());

        entity.setHomesteadUnits(model.getHomesteadUnits());

        entity.setKeyParcelNumber(model.getKeyParcelNumber());

        entity.setMailingCity(model.getMailingCity());

        entity.setMailingDirection(model.getMailingDirection());

        entity.setMailingHouseNumber(model.getMailingHouseNumber());

        entity.setMailingState(model.getMailingState());

        entity.setMailingStreet(model.getMailingStreet());

        entity.setMailingSuffix(model.getMailingSuffix());

        entity.setMailingZipCode(model.getMailingZipCode());

        entity.setMaintenanceIndicator(model.getMaintenanceIndicator());

        entity.setMasterName(model.getMasterName());

        entity.setOccupancyFactor(model.getOccupancyFactor());

        entity.setOriginalBaseValueYear(model.getOriginalBaseValueYear());

        entity.setOriginalBaseYearEligibleComputedFullAssessedValue(model.getOriginalBaseYearEligibleComputedFullAssessedValue());

        entity.setOriginalBaseYearEqualizedValue(model.getOriginalBaseYearEqualizedValue());

        entity.setOriginalBaseYearFullAssessedValue(model.getOriginalBaseYearFullAssessedValue());

        entity.setOriginalBaseYearTotalEligibleComputedEqualizedValue(model.getOriginalBaseYearTotalEligibleComputedEqualizedValue());

        entity.setOriginalCurrentYearFinalEqualizedValueDifference(model.getOriginalCurrentYearFinalEqualizedValueDifference());

        entity.setOriginalManualCalculationIndicator(model.getOriginalManualCalculationIndicator());

        entity.setPropertyProration(model.getPropertyProration());

        entity.setRecordCode(model.getRecordCode());

        entity.setSeniorFreezeShares(model.getSeniorFreezeShares());

        entity.setSplitCode(model.getSplitCode());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
