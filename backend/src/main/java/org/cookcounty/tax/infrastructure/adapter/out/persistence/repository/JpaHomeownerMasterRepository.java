package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.HomeownerMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

/// Persists homeowner snapshots and supplies the stable key order required by the renewal merge.
public interface JpaHomeownerMasterRepository extends JpaRepository<HomeownerMasterEntity, Long> {
    // GENERATED-METHODS:start

    // GENERATED-METHODS:end
    /// Returns all snapshots by canonical property identifier, then persistence identity for
    /// duplicate keys.
    List<HomeownerMasterEntity> findAllByOrderByPropertyNumberAscIdAsc();
}
