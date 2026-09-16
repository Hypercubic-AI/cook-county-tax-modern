package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.SeniorFreezeMaster;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.SeniorFreezeMasterEntity;

/// Preserves every Senior Freeze master field, identity, and version across persistence.
public final class SeniorFreezeMasterMapper {
    private SeniorFreezeMasterMapper() {}

    /// Creates an immutable domain snapshot from one hydrated persistence entity.
    public static SeniorFreezeMaster toDomain(SeniorFreezeMasterEntity entity) {
        return new SeniorFreezeMaster(
                entity.getId(),
                entity.getVersion(),
                entity.getBaseValueManualCalculationIndicator(),
                entity.getBaseValueNoCalculationIndicator(),
                entity.getBaseValueYear(),
                entity.getBaseValueYearClass(),
                entity.getBaseYearEligibleComputedFullAssessedValue(),
                entity.getBaseYearEqualizedValue(),
                entity.getBaseYearFullAssessedValue(),
                entity.getBaseYearTotalEligibleComputedEqualizedValue(),
                entity.getBuildingShares(),
                entity.getBuildingUnits(),
                entity.getCalculationType(),
                entity.getClass288ExpirationAssessedValue(),
                entity.getClass288ExpirationEqualizedValue(),
                entity.getClass288OverLimitAssessedValue(),
                entity.getClass288OverLimitEqualizedValue(),
                entity.getCurrentYearClass(),
                entity.getCurrentYearEligibleComputedAssessedValue(),
                entity.getCurrentYearEligibleComputedEqualizedValue(),
                entity.getCurrentYearFarmIndicator(),
                entity.getCurrentYearFinalEqualizedValueDifference(),
                entity.getCurrentYearFullAssessedValue(),
                entity.getCurrentYearFullEqualizedValue(),
                entity.getCurrentYearNotEligibleAssessedValue(),
                entity.getCurrentYearNotEligibleEqualizedValue(),
                entity.getHomeownerUnits(),
                entity.getHomesteadUnits(),
                entity.getKeyParcelNumber(),
                entity.getMailingCity(),
                entity.getMailingDirection(),
                entity.getMailingHouseNumber(),
                entity.getMailingState(),
                entity.getMailingStreet(),
                entity.getMailingSuffix(),
                entity.getMailingZipCode(),
                entity.getMaintenanceIndicator(),
                entity.getMasterName(),
                entity.getOccupancyFactor(),
                entity.getOriginalBaseValueYear(),
                entity.getOriginalBaseYearEligibleComputedFullAssessedValue(),
                entity.getOriginalBaseYearEqualizedValue(),
                entity.getOriginalBaseYearFullAssessedValue(),
                entity.getOriginalBaseYearTotalEligibleComputedEqualizedValue(),
                entity.getOriginalCurrentYearFinalEqualizedValueDifference(),
                entity.getOriginalManualCalculationIndicator(),
                entity.getPropertyProration(),
                entity.getRecordCode(),
                entity.getSeniorFreezeShares(),
                entity.getSplitCode());
    }

    /// Creates a mutable persistence entity without dropping identity or version state.
    public static SeniorFreezeMasterEntity toEntity(SeniorFreezeMaster model) {
        SeniorFreezeMasterEntity entity = new SeniorFreezeMasterEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setBaseValueManualCalculationIndicator(model.baseValueManualCalculationIndicator());
        entity.setBaseValueNoCalculationIndicator(model.baseValueNoCalculationIndicator());
        entity.setBaseValueYear(model.baseValueYear());
        entity.setBaseValueYearClass(model.baseValueYearClass());
        entity.setBaseYearEligibleComputedFullAssessedValue(
                model.baseYearEligibleComputedFullAssessedValue());
        entity.setBaseYearEqualizedValue(model.baseYearEqualizedValue());
        entity.setBaseYearFullAssessedValue(model.baseYearFullAssessedValue());
        entity.setBaseYearTotalEligibleComputedEqualizedValue(
                model.baseYearTotalEligibleComputedEqualizedValue());
        entity.setBuildingShares(model.buildingShares());
        entity.setBuildingUnits(model.buildingUnits());
        entity.setCalculationType(model.calculationType());
        entity.setClass288ExpirationAssessedValue(model.class288ExpirationAssessedValue());
        entity.setClass288ExpirationEqualizedValue(model.class288ExpirationEqualizedValue());
        entity.setClass288OverLimitAssessedValue(model.class288OverLimitAssessedValue());
        entity.setClass288OverLimitEqualizedValue(model.class288OverLimitEqualizedValue());
        entity.setCurrentYearClass(model.currentYearClass());
        entity.setCurrentYearEligibleComputedAssessedValue(
                model.currentYearEligibleComputedAssessedValue());
        entity.setCurrentYearEligibleComputedEqualizedValue(
                model.currentYearEligibleComputedEqualizedValue());
        entity.setCurrentYearFarmIndicator(model.currentYearFarmIndicator());
        entity.setCurrentYearFinalEqualizedValueDifference(
                model.currentYearFinalEqualizedValueDifference());
        entity.setCurrentYearFullAssessedValue(model.currentYearFullAssessedValue());
        entity.setCurrentYearFullEqualizedValue(model.currentYearFullEqualizedValue());
        entity.setCurrentYearNotEligibleAssessedValue(model.currentYearNotEligibleAssessedValue());
        entity.setCurrentYearNotEligibleEqualizedValue(
                model.currentYearNotEligibleEqualizedValue());
        entity.setHomeownerUnits(model.homeownerUnits());
        entity.setHomesteadUnits(model.homesteadUnits());
        entity.setKeyParcelNumber(model.keyParcelNumber());
        entity.setMailingCity(model.mailingCity());
        entity.setMailingDirection(model.mailingDirection());
        entity.setMailingHouseNumber(model.mailingHouseNumber());
        entity.setMailingState(model.mailingState());
        entity.setMailingStreet(model.mailingStreet());
        entity.setMailingSuffix(model.mailingSuffix());
        entity.setMailingZipCode(model.mailingZipCode());
        entity.setMaintenanceIndicator(model.maintenanceIndicator());
        entity.setMasterName(model.masterName());
        entity.setOccupancyFactor(model.occupancyFactor());
        entity.setOriginalBaseValueYear(model.originalBaseValueYear());
        entity.setOriginalBaseYearEligibleComputedFullAssessedValue(
                model.originalBaseYearEligibleComputedFullAssessedValue());
        entity.setOriginalBaseYearEqualizedValue(model.originalBaseYearEqualizedValue());
        entity.setOriginalBaseYearFullAssessedValue(model.originalBaseYearFullAssessedValue());
        entity.setOriginalBaseYearTotalEligibleComputedEqualizedValue(
                model.originalBaseYearTotalEligibleComputedEqualizedValue());
        entity.setOriginalCurrentYearFinalEqualizedValueDifference(
                model.originalCurrentYearFinalEqualizedValueDifference());
        entity.setOriginalManualCalculationIndicator(model.originalManualCalculationIndicator());
        entity.setPropertyProration(model.propertyProration());
        entity.setRecordCode(model.recordCode());
        entity.setSeniorFreezeShares(model.seniorFreezeShares());
        entity.setSplitCode(model.splitCode());
        return entity;
    }
}
