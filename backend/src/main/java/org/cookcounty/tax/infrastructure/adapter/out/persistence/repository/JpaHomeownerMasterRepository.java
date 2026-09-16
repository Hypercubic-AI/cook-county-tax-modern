
package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.HomeownerMasterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

public interface JpaHomeownerMasterRepository extends JpaRepository<HomeownerMasterEntity, Long> {
    // GENERATED-METHODS:start

    // GENERATED-METHODS:end
    List<HomeownerMasterEntity> findAllByOrderByPropertyNumberAscIdAsc();
}
