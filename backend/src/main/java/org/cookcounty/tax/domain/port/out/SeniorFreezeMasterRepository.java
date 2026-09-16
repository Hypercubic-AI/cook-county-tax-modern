
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.SeniorFreezeMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SeniorFreezeMasterRepository {
    // GENERATED-METHODS:start
    Page<SeniorFreezeMaster> findAll(Pageable pageable);
    Optional<SeniorFreezeMaster> findById(Long id);
    SeniorFreezeMaster save(SeniorFreezeMaster seniorFreezeMaster);
    void deleteById(Long id);
    // GENERATED-METHODS:end
}
