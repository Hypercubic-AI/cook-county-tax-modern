package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.SeniorFreezeApplicantEntity;

/// Preserves every Senior Freeze applicant field, identity, and version across persistence.
public final class SeniorFreezeApplicantMapper {
    private SeniorFreezeApplicantMapper() {}

    /// Creates an immutable domain snapshot from one hydrated persistence entity.
    public static SeniorFreezeApplicant toDomain(SeniorFreezeApplicantEntity entity) {
        return new SeniorFreezeApplicant(
                entity.getId(),
                entity.getVersion(),
                entity.getAge(),
                entity.getApplicantAddress(),
                entity.getApplicantCity(),
                entity.getApplicantFirstName(),
                entity.getApplicantLastName(),
                entity.getApplicantMiddleInitial(),
                entity.getApplicantOldName(),
                entity.getApplicantState(),
                entity.getApplicantTitle(),
                entity.getApplicantZipCode(),
                entity.getBaseYear(),
                entity.getBaseYearEligibleEqualizedValue(),
                entity.getBaseYearIndicator(),
                entity.getBatchNumber(),
                entity.getBirthDate(),
                entity.getCivilServiceBenefits(),
                entity.getCooperativeSeniorShares(),
                entity.getDenialDate(),
                entity.getFirstApplicationDate(),
                entity.getHomeownerBaseYear(),
                entity.getHomeownerBaseYearAssessedValue(),
                entity.getHomeownerBaseYearEqualizationFactor(),
                entity.getHomeownerBaseYearEqualizedValue(),
                entity.getHomeownerEligibilityIndicator(),
                entity.getHomeownerStatus(),
                entity.getHomesteadBatchNumber(),
                entity.getHomesteadPercentShares(),
                entity.getHomesteadShares(),
                entity.getHomesteadStatus(),
                entity.getHomesteadYearApplied(),
                entity.getInterestIncome(),
                entity.getLastApplicationDate(),
                entity.getLifeCareFacilityIndicator(),
                entity.getMaintenanceIndicator(),
                entity.getNameMaintenanceIndicator(),
                entity.getNetCapitalGain(),
                entity.getNetRentalIncome(),
                entity.getNoIncomeIndicator(),
                entity.getNotarizedIndicator(),
                entity.getOtherBenefits(),
                entity.getOtherIncome(),
                entity.getPercentSeniorShares(),
                entity.getPhoneNumber(),
                entity.getPublicAid(),
                entity.getQualificationDate(),
                entity.getRailroadBenefits(),
                entity.getReturnedDate(),
                entity.getSeniorFreezePercent(),
                entity.getSeniorFreezeStatus(),
                entity.getSignedIndicator(),
                entity.getSocialSecurityIncome(),
                entity.getSocialSecurityNumber(),
                entity.getTotalIncome(),
                entity.getVeteransBenefits(),
                entity.getWages());
    }

    /// Creates a mutable persistence entity without dropping identity or version state.
    public static SeniorFreezeApplicantEntity toEntity(SeniorFreezeApplicant model) {
        SeniorFreezeApplicantEntity entity = new SeniorFreezeApplicantEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setAge(model.age());
        entity.setApplicantAddress(model.applicantAddress());
        entity.setApplicantCity(model.applicantCity());
        entity.setApplicantFirstName(model.applicantFirstName());
        entity.setApplicantLastName(model.applicantLastName());
        entity.setApplicantMiddleInitial(model.applicantMiddleInitial());
        entity.setApplicantOldName(model.applicantOldName());
        entity.setApplicantState(model.applicantState());
        entity.setApplicantTitle(model.applicantTitle());
        entity.setApplicantZipCode(model.applicantZipCode());
        entity.setBaseYear(model.baseYear());
        entity.setBaseYearEligibleEqualizedValue(model.baseYearEligibleEqualizedValue());
        entity.setBaseYearIndicator(model.baseYearIndicator());
        entity.setBatchNumber(model.batchNumber());
        entity.setBirthDate(model.birthDate());
        entity.setCivilServiceBenefits(model.civilServiceBenefits());
        entity.setCooperativeSeniorShares(model.cooperativeSeniorShares());
        entity.setDenialDate(model.denialDate());
        entity.setFirstApplicationDate(model.firstApplicationDate());
        entity.setHomeownerBaseYear(model.homeownerBaseYear());
        entity.setHomeownerBaseYearAssessedValue(model.homeownerBaseYearAssessedValue());
        entity.setHomeownerBaseYearEqualizationFactor(model.homeownerBaseYearEqualizationFactor());
        entity.setHomeownerBaseYearEqualizedValue(model.homeownerBaseYearEqualizedValue());
        entity.setHomeownerEligibilityIndicator(model.homeownerEligibilityIndicator());
        entity.setHomeownerStatus(model.homeownerStatus());
        entity.setHomesteadBatchNumber(model.homesteadBatchNumber());
        entity.setHomesteadPercentShares(model.homesteadPercentShares());
        entity.setHomesteadShares(model.homesteadShares());
        entity.setHomesteadStatus(model.homesteadStatus());
        entity.setHomesteadYearApplied(model.homesteadYearApplied());
        entity.setInterestIncome(model.interestIncome());
        entity.setLastApplicationDate(model.lastApplicationDate());
        entity.setLifeCareFacilityIndicator(model.lifeCareFacilityIndicator());
        entity.setMaintenanceIndicator(model.maintenanceIndicator());
        entity.setNameMaintenanceIndicator(model.nameMaintenanceIndicator());
        entity.setNetCapitalGain(model.netCapitalGain());
        entity.setNetRentalIncome(model.netRentalIncome());
        entity.setNoIncomeIndicator(model.noIncomeIndicator());
        entity.setNotarizedIndicator(model.notarizedIndicator());
        entity.setOtherBenefits(model.otherBenefits());
        entity.setOtherIncome(model.otherIncome());
        entity.setPercentSeniorShares(model.percentSeniorShares());
        entity.setPhoneNumber(model.phoneNumber());
        entity.setPublicAid(model.publicAid());
        entity.setQualificationDate(model.qualificationDate());
        entity.setRailroadBenefits(model.railroadBenefits());
        entity.setReturnedDate(model.returnedDate());
        entity.setSeniorFreezePercent(model.seniorFreezePercent());
        entity.setSeniorFreezeStatus(model.seniorFreezeStatus());
        entity.setSignedIndicator(model.signedIndicator());
        entity.setSocialSecurityIncome(model.socialSecurityIncome());
        entity.setSocialSecurityNumber(model.socialSecurityNumber());
        entity.setTotalIncome(model.totalIncome());
        entity.setVeteransBenefits(model.veteransBenefits());
        entity.setWages(model.wages());
        return entity;
    }
}
