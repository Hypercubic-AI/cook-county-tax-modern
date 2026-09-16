package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxCodeAgencySlotEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxCodeAgencySlotId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/// Spring Data access to assigned agency positions for each tax code.
public interface JpaTaxCodeAgencySlotRepository
        extends JpaRepository<TaxCodeAgencySlotEntity, TaxCodeAgencySlotId> {
    /// Returns assigned positions in the source order required by all 40-slot processing.
    List<TaxCodeAgencySlotEntity> findAllByIdTaxCodeOrderByIdSlotPosition(String taxCode);
}
