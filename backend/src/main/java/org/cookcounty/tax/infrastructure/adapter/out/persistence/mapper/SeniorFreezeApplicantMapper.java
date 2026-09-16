
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.SeniorFreezeApplicantEntity;

public class SeniorFreezeApplicantMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static SeniorFreezeApplicant toDomain(SeniorFreezeApplicantEntity entity) {
        SeniorFreezeApplicant model = new SeniorFreezeApplicant();
        model.setId(entity.getId());

        model.setAge(entity.getAge());

        model.setApplicantAddress(entity.getApplicantAddress());

        model.setApplicantCity(entity.getApplicantCity());

        model.setApplicantFirstName(entity.getApplicantFirstName());

        model.setApplicantLastName(entity.getApplicantLastName());

        model.setApplicantMiddleInitial(entity.getApplicantMiddleInitial());

        model.setApplicantOldName(entity.getApplicantOldName());

        model.setApplicantState(entity.getApplicantState());

        model.setApplicantTitle(entity.getApplicantTitle());

        model.setApplicantZipCode(entity.getApplicantZipCode());

        model.setBaseYear(entity.getBaseYear());

        model.setBaseYearEligibleEqualizedValue(entity.getBaseYearEligibleEqualizedValue());

        model.setBaseYearIndicator(entity.getBaseYearIndicator());

        model.setBatchNumber(entity.getBatchNumber());

        model.setBirthDate(entity.getBirthDate());

        model.setCivilServiceBenefits(entity.getCivilServiceBenefits());

        model.setCooperativeSeniorShares(entity.getCooperativeSeniorShares());

        model.setDenialDate(entity.getDenialDate());

        model.setFirstApplicationDate(entity.getFirstApplicationDate());

        model.setHomeownerBaseYear(entity.getHomeownerBaseYear());

        model.setHomeownerBaseYearAssessedValue(entity.getHomeownerBaseYearAssessedValue());

        model.setHomeownerBaseYearEqualizationFactor(entity.getHomeownerBaseYearEqualizationFactor());

        model.setHomeownerBaseYearEqualizedValue(entity.getHomeownerBaseYearEqualizedValue());

        model.setHomeownerEligibilityIndicator(entity.getHomeownerEligibilityIndicator());

        model.setHomeownerStatus(entity.getHomeownerStatus());

        model.setHomesteadBatchNumber(entity.getHomesteadBatchNumber());

        model.setHomesteadPercentShares(entity.getHomesteadPercentShares());

        model.setHomesteadShares(entity.getHomesteadShares());

        model.setHomesteadStatus(entity.getHomesteadStatus());

        model.setHomesteadYearApplied(entity.getHomesteadYearApplied());

        model.setInterestIncome(entity.getInterestIncome());

        model.setLastApplicationDate(entity.getLastApplicationDate());

        model.setLifeCareFacilityIndicator(entity.getLifeCareFacilityIndicator());

        model.setMaintenanceIndicator(entity.getMaintenanceIndicator());

        model.setNameMaintenanceIndicator(entity.getNameMaintenanceIndicator());

        model.setNetCapitalGain(entity.getNetCapitalGain());

        model.setNetRentalIncome(entity.getNetRentalIncome());

        model.setNoIncomeIndicator(entity.getNoIncomeIndicator());

        model.setNotarizedIndicator(entity.getNotarizedIndicator());

        model.setOtherBenefits(entity.getOtherBenefits());

        model.setOtherIncome(entity.getOtherIncome());

        model.setPercentSeniorShares(entity.getPercentSeniorShares());

        model.setPhoneNumber(entity.getPhoneNumber());

        model.setPublicAid(entity.getPublicAid());

        model.setQualificationDate(entity.getQualificationDate());

        model.setRailroadBenefits(entity.getRailroadBenefits());

        model.setReturnedDate(entity.getReturnedDate());

        model.setSeniorFreezePercent(entity.getSeniorFreezePercent());

        model.setSeniorFreezeStatus(entity.getSeniorFreezeStatus());

        model.setSignedIndicator(entity.getSignedIndicator());

        model.setSocialSecurityIncome(entity.getSocialSecurityIncome());

        model.setSocialSecurityNumber(entity.getSocialSecurityNumber());

        model.setTotalIncome(entity.getTotalIncome());

        model.setVeteransBenefits(entity.getVeteransBenefits());

        model.setWages(entity.getWages());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static SeniorFreezeApplicantEntity toEntity(SeniorFreezeApplicant model) {
        SeniorFreezeApplicantEntity entity = new SeniorFreezeApplicantEntity();
        entity.setId(model.getId());

        entity.setAge(model.getAge());

        entity.setApplicantAddress(model.getApplicantAddress());

        entity.setApplicantCity(model.getApplicantCity());

        entity.setApplicantFirstName(model.getApplicantFirstName());

        entity.setApplicantLastName(model.getApplicantLastName());

        entity.setApplicantMiddleInitial(model.getApplicantMiddleInitial());

        entity.setApplicantOldName(model.getApplicantOldName());

        entity.setApplicantState(model.getApplicantState());

        entity.setApplicantTitle(model.getApplicantTitle());

        entity.setApplicantZipCode(model.getApplicantZipCode());

        entity.setBaseYear(model.getBaseYear());

        entity.setBaseYearEligibleEqualizedValue(model.getBaseYearEligibleEqualizedValue());

        entity.setBaseYearIndicator(model.getBaseYearIndicator());

        entity.setBatchNumber(model.getBatchNumber());

        entity.setBirthDate(model.getBirthDate());

        entity.setCivilServiceBenefits(model.getCivilServiceBenefits());

        entity.setCooperativeSeniorShares(model.getCooperativeSeniorShares());

        entity.setDenialDate(model.getDenialDate());

        entity.setFirstApplicationDate(model.getFirstApplicationDate());

        entity.setHomeownerBaseYear(model.getHomeownerBaseYear());

        entity.setHomeownerBaseYearAssessedValue(model.getHomeownerBaseYearAssessedValue());

        entity.setHomeownerBaseYearEqualizationFactor(model.getHomeownerBaseYearEqualizationFactor());

        entity.setHomeownerBaseYearEqualizedValue(model.getHomeownerBaseYearEqualizedValue());

        entity.setHomeownerEligibilityIndicator(model.getHomeownerEligibilityIndicator());

        entity.setHomeownerStatus(model.getHomeownerStatus());

        entity.setHomesteadBatchNumber(model.getHomesteadBatchNumber());

        entity.setHomesteadPercentShares(model.getHomesteadPercentShares());

        entity.setHomesteadShares(model.getHomesteadShares());

        entity.setHomesteadStatus(model.getHomesteadStatus());

        entity.setHomesteadYearApplied(model.getHomesteadYearApplied());

        entity.setInterestIncome(model.getInterestIncome());

        entity.setLastApplicationDate(model.getLastApplicationDate());

        entity.setLifeCareFacilityIndicator(model.getLifeCareFacilityIndicator());

        entity.setMaintenanceIndicator(model.getMaintenanceIndicator());

        entity.setNameMaintenanceIndicator(model.getNameMaintenanceIndicator());

        entity.setNetCapitalGain(model.getNetCapitalGain());

        entity.setNetRentalIncome(model.getNetRentalIncome());

        entity.setNoIncomeIndicator(model.getNoIncomeIndicator());

        entity.setNotarizedIndicator(model.getNotarizedIndicator());

        entity.setOtherBenefits(model.getOtherBenefits());

        entity.setOtherIncome(model.getOtherIncome());

        entity.setPercentSeniorShares(model.getPercentSeniorShares());

        entity.setPhoneNumber(model.getPhoneNumber());

        entity.setPublicAid(model.getPublicAid());

        entity.setQualificationDate(model.getQualificationDate());

        entity.setRailroadBenefits(model.getRailroadBenefits());

        entity.setReturnedDate(model.getReturnedDate());

        entity.setSeniorFreezePercent(model.getSeniorFreezePercent());

        entity.setSeniorFreezeStatus(model.getSeniorFreezeStatus());

        entity.setSignedIndicator(model.getSignedIndicator());

        entity.setSocialSecurityIncome(model.getSocialSecurityIncome());

        entity.setSocialSecurityNumber(model.getSocialSecurityNumber());

        entity.setTotalIncome(model.getTotalIncome());

        entity.setVeteransBenefits(model.getVeteransBenefits());

        entity.setWages(model.getWages());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
