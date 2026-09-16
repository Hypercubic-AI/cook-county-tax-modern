
package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface HomeownerMasterRepository {
    // GENERATED-METHODS:start
    Page<HomeownerMaster> findAll(Pageable pageable);
    Optional<HomeownerMaster> findById(Long id);
    HomeownerMaster save(HomeownerMaster homeownerMaster);
    void deleteById(Long id);
    // GENERATED-METHODS:end
    List<HomeownerMaster> findAllInPropertyOrder();
}
