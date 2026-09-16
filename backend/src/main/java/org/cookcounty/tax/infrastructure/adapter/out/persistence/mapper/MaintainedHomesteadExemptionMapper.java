package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.MaintainedHomesteadExemption;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.MaintainedHomesteadExemptionEntity;

/// Preserves every maintained-homestead field, identity, and version across persistence.
public final class MaintainedHomesteadExemptionMapper {
    private MaintainedHomesteadExemptionMapper() {}

    /// Creates an immutable domain snapshot from one hydrated persistence entity.
    ///
    /// @param entity fully hydrated maintained exemption entity
    /// @return snapshot that preserves identity, version, and coordinated base-year state
    public static MaintainedHomesteadExemption toDomain(MaintainedHomesteadExemptionEntity entity) {
        return new MaintainedHomesteadExemption(
                entity.getId(),
                entity.getVersion(),
                entity.getApplicationYear(),
                entity.getAssessedValue(),
                entity.getAssessmentClass(),
                entity.getCertificateOfErrorNumber(),
                entity.getCity(),
                entity.getClerksClass(),
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
                entity.getTertiaryStatus(),
                entity.getVolumeNumber(),
                entity.getZipCode());
    }

    /// Creates a mutable persistence entity without dropping identity or version state.
    ///
    /// @param model complete maintained exemption snapshot
    /// @return entity with the descriptive base-year columns populated from the domain group
    public static MaintainedHomesteadExemptionEntity toEntity(MaintainedHomesteadExemption model) {
        MaintainedHomesteadExemptionEntity entity = new MaintainedHomesteadExemptionEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setApplicationYear(model.applicationYear());
        entity.setAssessedValue(model.assessedValue());
        entity.setAssessmentClass(model.assessmentClass());
        entity.setCertificateOfErrorNumber(model.certificateOfErrorNumber());
        entity.setCity(model.city());
        entity.setClerksClass(model.clerksClass());
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
        entity.setTertiaryStatus(model.tertiaryStatus());
        entity.setVolumeNumber(model.volumeNumber());
        entity.setZipCode(model.zipCode());
        return entity;
    }
}
