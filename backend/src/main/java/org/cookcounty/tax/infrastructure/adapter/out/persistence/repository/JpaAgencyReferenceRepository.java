package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AgencyReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/// Spring Data access to agency descriptions by fixed-width agency identity.
public interface JpaAgencyReferenceRepository
        extends JpaRepository<AgencyReferenceEntity, String> {}
