package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TownReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/// Spring Data access to town names by fixed-width town code.
public interface JpaTownReferenceRepository extends JpaRepository<TownReferenceEntity, String> {}
