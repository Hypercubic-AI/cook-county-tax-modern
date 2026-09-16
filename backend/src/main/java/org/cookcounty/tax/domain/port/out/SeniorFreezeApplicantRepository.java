
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SeniorFreezeApplicantRepository {
    // GENERATED-METHODS:start
    Page<SeniorFreezeApplicant> findAll(Pageable pageable);
    Optional<SeniorFreezeApplicant> findById(Long id);
    SeniorFreezeApplicant save(SeniorFreezeApplicant seniorFreezeApplicant);
    void deleteById(Long id);
    // GENERATED-METHODS:end
}
