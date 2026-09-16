package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.HomeownerExemptionEntity;

/// Preserves every homeowner-exemption field, generated identity, and version across persistence.
public final class HomeownerExemptionMapper {
    private HomeownerExemptionMapper() {}

    /// Creates an immutable domain snapshot from one hydrated persistence entity.
    public static HomeownerExemption toDomain(HomeownerExemptionEntity entity) {
        return new HomeownerExemption(
                entity.getId(),
                entity.getVersion(),
                entity.getApplicationYear(),
                entity.getAssessedValue(),
                entity.getAssessmentClass(),
                entity.getCertificateOfErrorNumber(),
                entity.getCity(),
                entity.getClerksClass(),
                entity.getCooperativeQuantity(),
                entity.getEligibilityIndicator(),
                entity.getEqualizationFactor(),
                entity.getEqualizedValue(),
                entity.getExemptionType(),
                entity.getKeyParcelNumber(),
                entity.getMailingAddress(),
                entity.getOccupancyFactor(),
                entity.getOwnerName(),
                entity.getPropertyNumber(),
                entity.getProration(),
                entity.getRecordCode(),
                entity.getResponseStatus(),
                entity.getSecondaryResponseStatus(),
                entity.getSplitCode(),
                entity.getState(),
                entity.getTaxCode(),
                entity.getTaxType(),
                entity.getTertiaryStatus(),
                entity.getVolumeNumber(),
                entity.getZipCode());
    }

    /// Creates a mutable persistence entity without dropping identity or version state.
    public static HomeownerExemptionEntity toEntity(HomeownerExemption model) {
        HomeownerExemptionEntity entity = new HomeownerExemptionEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setApplicationYear(model.applicationYear());
        entity.setAssessedValue(model.assessedValue());
        entity.setAssessmentClass(model.assessmentClass());
        entity.setCertificateOfErrorNumber(model.certificateOfErrorNumber());
        entity.setCity(model.city());
        entity.setClerksClass(model.clerksClass());
        entity.setCooperativeQuantity(model.cooperativeQuantity());
        entity.setEligibilityIndicator(model.eligibilityIndicator());
        entity.setEqualizationFactor(model.equalizationFactor());
        entity.setEqualizedValue(model.equalizedValue());
        entity.setExemptionType(model.exemptionType());
        entity.setKeyParcelNumber(model.keyParcelNumber());
        entity.setMailingAddress(model.mailingAddress());
        entity.setOccupancyFactor(model.occupancyFactor());
        entity.setOwnerName(model.ownerName());
        entity.setPropertyNumber(model.propertyNumber());
        entity.setProration(model.proration());
        entity.setRecordCode(model.recordCode());
        entity.setResponseStatus(model.responseStatus());
        entity.setSecondaryResponseStatus(model.secondaryResponseStatus());
        entity.setSplitCode(model.splitCode());
        entity.setState(model.state());
        entity.setTaxCode(model.taxCode());
        entity.setTaxType(model.taxType());
        entity.setTertiaryStatus(model.tertiaryStatus());
        entity.setVolumeNumber(model.volumeNumber());
        entity.setZipCode(model.zipCode());
        return entity;
    }
}
