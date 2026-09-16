package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxCodeMasterReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/// Spring Data access to tax rates by fixed-width tax code.
public interface JpaTaxCodeMasterReferenceRepository
        extends JpaRepository<TaxCodeMasterReferenceEntity, String> {}
