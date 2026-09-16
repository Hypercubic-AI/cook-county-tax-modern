
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.MaintainedHomesteadExemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MaintainedHomesteadExemptionRepository {
    // GENERATED-METHODS:start
    Page<MaintainedHomesteadExemption> findAll(Pageable pageable);
    Optional<MaintainedHomesteadExemption> findById(Long id);
    MaintainedHomesteadExemption save(MaintainedHomesteadExemption maintainedHomesteadExemption);
    void deleteById(Long id);
    // GENERATED-METHODS:end
}
