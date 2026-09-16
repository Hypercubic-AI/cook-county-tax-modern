
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.FrozenValuation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface FrozenValuationRepository {
    // GENERATED-METHODS:start
    Page<FrozenValuation> findAll(Pageable pageable);
    Optional<FrozenValuation> findById(Long id);
    FrozenValuation save(FrozenValuation frozenValuation);
    void deleteById(Long id);
    // GENERATED-METHODS:end
}
