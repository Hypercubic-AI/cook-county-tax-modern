package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.FrozenAgencyAdjustmentEntity;

import java.util.Objects;

/// Canonical, complete mapping between the immutable domain snapshot and its persistence entity.
public final class FrozenAgencyAdjustmentMapper {

    private FrozenAgencyAdjustmentMapper() {}

    /// Hydrates every domain component, including the persistence identifier and version.
    public static FrozenAgencyAdjustment toDomain(FrozenAgencyAdjustmentEntity entity) {
        return new FrozenAgencyAdjustment(
                entity.getId(),
                entity.getVersion(),
                Objects.requireNonNull(entity.getAgencyNumber(), "agencyNumber"),
                Objects.requireNonNull(entity.getAnnexedAssessedValue(), "annexedAssessedValue"),
                Objects.requireNonNull(entity.getAnnexedEqualizedValue(), "annexedEqualizedValue"),
                Objects.requireNonNull(entity.getCurrent288Value(), "current288Value"),
                Objects.requireNonNull(
                        entity.getDisconnectedAssessedValue(), "disconnectedAssessedValue"),
                Objects.requireNonNull(
                        entity.getDisconnectedEqualizedValue(), "disconnectedEqualizedValue"),
                Objects.requireNonNull(entity.getExpired288Value(), "expired288Value"),
                Objects.requireNonNull(
                        entity.getExpiredIncentiveEqualizedValue(),
                        "expiredIncentiveEqualizedValue"),
                entity.getExpiredIncentiveTaxAmount(),
                Objects.requireNonNull(entity.getExpiredIncentiveValue(), "expiredIncentiveValue"),
                Objects.requireNonNull(entity.getFirstTimeValue(), "firstTimeValue"),
                Objects.requireNonNull(entity.getFrozenEqualizedValue(), "frozenEqualizedValue"),
                Objects.requireNonNull(entity.getFrozenTaxAmount(), "frozenTaxAmount"),
                Objects.requireNonNull(entity.getTaxCode(), "taxCode"),
                Objects.requireNonNull(entity.getTaxRate(), "taxRate"),
                Objects.requireNonNull(
                        entity.getTifCurrentEqualizedValue(), "tifCurrentEqualizedValue"),
                Objects.requireNonNull(
                        entity.getTifDifferenceEqualizedValue(), "tifDifferenceEqualizedValue"),
                Objects.requireNonNull(
                        entity.getTifPriorFrozenEqualizedValue(), "tifPriorFrozenEqualizedValue"),
                Objects.requireNonNull(entity.getTotalFrozenValue(), "totalFrozenValue"));
    }

    /// Copies every domain component to a mutable entity for persistence.
    public static FrozenAgencyAdjustmentEntity toEntity(FrozenAgencyAdjustment model) {
        FrozenAgencyAdjustmentEntity entity = new FrozenAgencyAdjustmentEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setAgencyNumber(model.agencyNumber());
        entity.setAnnexedAssessedValue(model.annexedAssessedValue());
        entity.setAnnexedEqualizedValue(model.annexedEqualizedValue());
        entity.setCurrent288Value(model.current288Value());
        entity.setDisconnectedAssessedValue(model.disconnectedAssessedValue());
        entity.setDisconnectedEqualizedValue(model.disconnectedEqualizedValue());
        entity.setExpired288Value(model.expired288Value());
        entity.setExpiredIncentiveEqualizedValue(model.expiredIncentiveEqualizedValue());
        entity.setExpiredIncentiveTaxAmount(model.expiredIncentiveTaxAmount());
        entity.setExpiredIncentiveValue(model.expiredIncentiveValue());
        entity.setFirstTimeValue(model.firstTimeValue());
        entity.setFrozenEqualizedValue(model.frozenEqualizedValue());
        entity.setFrozenTaxAmount(model.frozenTaxAmount());
        entity.setTaxCode(model.taxCode());
        entity.setTaxRate(model.taxRate());
        entity.setTifCurrentEqualizedValue(model.tifCurrentEqualizedValue());
        entity.setTifDifferenceEqualizedValue(model.tifDifferenceEqualizedValue());
        entity.setTifPriorFrozenEqualizedValue(model.tifPriorFrozenEqualizedValue());
        entity.setTotalFrozenValue(model.totalFrozenValue());
        return entity;
    }
}
