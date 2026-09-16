
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.HomeownerMasterEntity;

public class HomeownerMasterMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static HomeownerMaster toDomain(HomeownerMasterEntity entity) {
        HomeownerMaster model = new HomeownerMaster();
        model.setId(entity.getId());

        model.setApplicationYear(entity.getApplicationYear());

        model.setAssessedValue(entity.getAssessedValue());

        model.setAssessmentClass(entity.getAssessmentClass());

        model.setCertificateOfErrorNumber(entity.getCertificateOfErrorNumber());

        model.setCity(entity.getCity());

        model.setCooperativeQuantity(entity.getCooperativeQuantity());

        model.setEqualizationFactor(entity.getEqualizationFactor());

        model.setEqualizedValue(entity.getEqualizedValue());

        model.setExemptionType(entity.getExemptionType());

        model.setMailingAddress(entity.getMailingAddress());

        model.setNpheAmount(entity.getNpheAmount());

        model.setNpheBaseYear(entity.getNpheBaseYear());

        model.setNpheStatus(entity.getNpheStatus());

        model.setOccupancyFactor(entity.getOccupancyFactor());

        model.setOwnerName(entity.getOwnerName());

        model.setPropertyNumber(entity.getPropertyNumber());

        model.setProration(entity.getProration());

        model.setResponseStatus(entity.getResponseStatus());

        model.setSecondaryResponseStatus(entity.getSecondaryResponseStatus());

        model.setState(entity.getState());

        model.setTaxCode(entity.getTaxCode());

        model.setTaxType(entity.getTaxType());

        model.setTemporaryAssessedValue(entity.getTemporaryAssessedValue());

        model.setTertiaryStatus(entity.getTertiaryStatus());

        model.setVolumeNumber(entity.getVolumeNumber());

        model.setZipCode(entity.getZipCode());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static HomeownerMasterEntity toEntity(HomeownerMaster model) {
        HomeownerMasterEntity entity = new HomeownerMasterEntity();
        entity.setId(model.getId());

        entity.setApplicationYear(model.getApplicationYear());

        entity.setAssessedValue(model.getAssessedValue());

        entity.setAssessmentClass(model.getAssessmentClass());

        entity.setCertificateOfErrorNumber(model.getCertificateOfErrorNumber());

        entity.setCity(model.getCity());

        entity.setCooperativeQuantity(model.getCooperativeQuantity());

        entity.setEqualizationFactor(model.getEqualizationFactor());

        entity.setEqualizedValue(model.getEqualizedValue());

        entity.setExemptionType(model.getExemptionType());

        entity.setMailingAddress(model.getMailingAddress());

        entity.setNpheAmount(model.getNpheAmount());

        entity.setNpheBaseYear(model.getNpheBaseYear());

        entity.setNpheStatus(model.getNpheStatus());

        entity.setOccupancyFactor(model.getOccupancyFactor());

        entity.setOwnerName(model.getOwnerName());

        entity.setPropertyNumber(model.getPropertyNumber());

        entity.setProration(model.getProration());

        entity.setResponseStatus(model.getResponseStatus());

        entity.setSecondaryResponseStatus(model.getSecondaryResponseStatus());

        entity.setState(model.getState());

        entity.setTaxCode(model.getTaxCode());

        entity.setTaxType(model.getTaxType());

        entity.setTemporaryAssessedValue(model.getTemporaryAssessedValue());

        entity.setTertiaryStatus(model.getTertiaryStatus());

        entity.setVolumeNumber(model.getVolumeNumber());

        entity.setZipCode(model.getZipCode());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
