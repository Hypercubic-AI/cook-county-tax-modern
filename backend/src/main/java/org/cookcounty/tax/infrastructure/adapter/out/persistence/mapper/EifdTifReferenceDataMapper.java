package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.AgencyReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference.AgencySlot;
import org.cookcounty.tax.domain.model.TownReference;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AgencyReferenceEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxCodeAgencySlotEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxCodeMasterReferenceEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TownReferenceEntity;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;

/// Canonical mapping from hydrated reference entities to immutable domain values.
@NullMarked
public final class EifdTifReferenceDataMapper {
    private EifdTifReferenceDataMapper() {}

    /// Maps a fully hydrated agency reference.
    public static AgencyReference toDomain(AgencyReferenceEntity source) {
        return new AgencyReference(
                Objects.requireNonNull(source.getAgencyNumber(), "agencyNumber"),
                Objects.requireNonNull(source.getDescription(), "description"));
    }

    /// Maps a fully hydrated town reference.
    public static TownReference toDomain(TownReferenceEntity source) {
        return new TownReference(
                Objects.requireNonNull(source.getTownNumber(), "townNumber"),
                Objects.requireNonNull(source.getName(), "name"));
    }

    /// Maps a tax code and its source-ordered assigned agency positions.
    public static TaxCodeMasterReference toDomain(
            TaxCodeMasterReferenceEntity source, List<TaxCodeAgencySlotEntity> slots) {
        return new TaxCodeMasterReference(
                Objects.requireNonNull(source.getTaxCode(), "taxCode"),
                Objects.requireNonNull(source.getTaxRate(), "taxRate"),
                slots.stream()
                        .map(
                                slot ->
                                        new AgencySlot(
                                                Objects.requireNonNull(
                                                        Objects.requireNonNull(
                                                                        slot.getId(), "slotId")
                                                                .getSlotPosition(),
                                                        "slotPosition"),
                                                Objects.requireNonNull(
                                                        slot.getAgencyNumber(), "agencyNumber")))
                        .toList());
    }
}
