
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.HomeownerExemptionEntity;

public class HomeownerExemptionMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static HomeownerExemption toDomain(HomeownerExemptionEntity entity) {
        HomeownerExemption model = new HomeownerExemption();
        model.setId(entity.getId());

        model.setApplicationYear(entity.getApplicationYear());

        model.setAssessedValue(entity.getAssessedValue());

        model.setAssessmentClass(entity.getAssessmentClass());

        model.setCertificateOfErrorNumber(entity.getCertificateOfErrorNumber());

        model.setCity(entity.getCity());

        model.setClerksClass(entity.getClerksClass());

        model.setCooperativeQuantity(entity.getCooperativeQuantity());

        model.setEligibilityIndicator(entity.getEligibilityIndicator());

        model.setEqualizationFactor(entity.getEqualizationFactor());

        model.setEqualizedValue(entity.getEqualizedValue());

        model.setExemptionType(entity.getExemptionType());

        model.setKeyParcelNumber(entity.getKeyParcelNumber());

        model.setMailingAddress(entity.getMailingAddress());

        model.setOccupancyFactor(entity.getOccupancyFactor());

        model.setOwnerName(entity.getOwnerName());

        model.setPropertyNumber(entity.getPropertyNumber());

        model.setProration(entity.getProration());

        model.setRecordCode(entity.getRecordCode());

        model.setResponseStatus(entity.getResponseStatus());

        model.setSecondaryResponseStatus(entity.getSecondaryResponseStatus());

        model.setSplitCode(entity.getSplitCode());

        model.setState(entity.getState());

        model.setTaxCode(entity.getTaxCode());

        model.setTaxType(entity.getTaxType());

        model.setTertiaryStatus(entity.getTertiaryStatus());

        model.setVolumeNumber(entity.getVolumeNumber());

        model.setZipCode(entity.getZipCode());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static HomeownerExemptionEntity toEntity(HomeownerExemption model) {
        HomeownerExemptionEntity entity = new HomeownerExemptionEntity();
        entity.setId(model.getId());

        entity.setApplicationYear(model.getApplicationYear());

        entity.setAssessedValue(model.getAssessedValue());

        entity.setAssessmentClass(model.getAssessmentClass());

        entity.setCertificateOfErrorNumber(model.getCertificateOfErrorNumber());

        entity.setCity(model.getCity());

        entity.setClerksClass(model.getClerksClass());

        entity.setCooperativeQuantity(model.getCooperativeQuantity());

        entity.setEligibilityIndicator(model.getEligibilityIndicator());

        entity.setEqualizationFactor(model.getEqualizationFactor());

        entity.setEqualizedValue(model.getEqualizedValue());

        entity.setExemptionType(model.getExemptionType());

        entity.setKeyParcelNumber(model.getKeyParcelNumber());

        entity.setMailingAddress(model.getMailingAddress());

        entity.setOccupancyFactor(model.getOccupancyFactor());

        entity.setOwnerName(model.getOwnerName());

        entity.setPropertyNumber(model.getPropertyNumber());

        entity.setProration(model.getProration());

        entity.setRecordCode(model.getRecordCode());

        entity.setResponseStatus(model.getResponseStatus());

        entity.setSecondaryResponseStatus(model.getSecondaryResponseStatus());

        entity.setSplitCode(model.getSplitCode());

        entity.setState(model.getState());

        entity.setTaxCode(model.getTaxCode());

        entity.setTaxType(model.getTaxType());

        entity.setTertiaryStatus(model.getTertiaryStatus());

        entity.setVolumeNumber(model.getVolumeNumber());

        entity.setZipCode(model.getZipCode());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
