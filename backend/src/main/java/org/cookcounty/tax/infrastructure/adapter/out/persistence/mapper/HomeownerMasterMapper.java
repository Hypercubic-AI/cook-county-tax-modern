package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.HomeownerMasterEntity;

/// Preserves every homeowner-master field, generated identity, and version across persistence.
public final class HomeownerMasterMapper {
    private HomeownerMasterMapper() {}

    /// Creates an immutable domain snapshot from one hydrated persistence entity.
    ///
    /// The base-year amount, year, and establishment code remain one coordinated group.
    ///
    /// @param entity fully hydrated homeowner entity
    /// @return snapshot that preserves identity, version, optional fields, and base-year state
    public static HomeownerMaster toDomain(HomeownerMasterEntity entity) {
        return new HomeownerMaster(
                entity.getId(),
                entity.getVersion(),
                entity.getApplicationYear(),
                entity.getAssessedValue(),
                entity.getAssessmentClass(),
                entity.getCertificateOfErrorNumber(),
                entity.getCity(),
                entity.getCooperativeQuantity(),
                entity.getEqualizationFactor(),
                entity.getEqualizedValue(),
                entity.getExemptionType(),
                entity.getMailingAddress(),
                entity.getBaseYearExemptionAmount(),
                entity.getExemptionBaseYear(),
                entity.getBaseYearEstablishmentCode(),
                entity.getOccupancyFactor(),
                entity.getOwnerName(),
                entity.getPropertyNumber(),
                entity.getProration(),
                entity.getResponseStatus(),
                entity.getSecondaryResponseStatus(),
                entity.getState(),
                entity.getTaxCode(),
                entity.getTaxType(),
                entity.getTemporaryAssessedValue(),
                entity.getTertiaryStatus(),
                entity.getVolumeNumber(),
                entity.getZipCode());
    }

    /// Creates a mutable persistence entity without dropping identity or version state.
    ///
    /// @param model complete homeowner snapshot
    /// @return entity with the descriptive base-year columns populated from the domain group
    public static HomeownerMasterEntity toEntity(HomeownerMaster model) {
        HomeownerMasterEntity entity = new HomeownerMasterEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setApplicationYear(model.applicationYear());
        entity.setAssessedValue(model.assessedValue());
        entity.setAssessmentClass(model.assessmentClass());
        entity.setCertificateOfErrorNumber(model.certificateOfErrorNumber());
        entity.setCity(model.city());
        entity.setCooperativeQuantity(model.cooperativeQuantity());
        entity.setEqualizationFactor(model.equalizationFactor());
        entity.setEqualizedValue(model.equalizedValue());
        entity.setExemptionType(model.exemptionType());
        entity.setMailingAddress(model.mailingAddress());
        entity.setBaseYearExemptionAmount(model.baseYearExemptionAmount());
        entity.setExemptionBaseYear(model.exemptionBaseYear());
        entity.setBaseYearEstablishmentCode(model.baseYearEstablishmentCode());
        entity.setOccupancyFactor(model.occupancyFactor());
        entity.setOwnerName(model.ownerName());
        entity.setPropertyNumber(model.propertyNumber());
        entity.setProration(model.proration());
        entity.setResponseStatus(model.responseStatus());
        entity.setSecondaryResponseStatus(model.secondaryResponseStatus());
        entity.setState(model.state());
        entity.setTaxCode(model.taxCode());
        entity.setTaxType(model.taxType());
        entity.setTemporaryAssessedValue(model.temporaryAssessedValue());
        entity.setTertiaryStatus(model.tertiaryStatus());
        entity.setVolumeNumber(model.volumeNumber());
        entity.setZipCode(model.zipCode());
        return entity;
    }
}
