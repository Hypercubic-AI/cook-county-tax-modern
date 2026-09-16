
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.HomeownerMasterMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaHomeownerMasterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class HomeownerMasterRepositoryAdapter implements HomeownerMasterRepository {

    private final JpaHomeownerMasterRepository jpaRepository;

    public HomeownerMasterRepositoryAdapter(JpaHomeownerMasterRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<HomeownerMaster> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(HomeownerMasterMapper::toDomain);
    }

    @Override
    public Optional<HomeownerMaster> findById(Long id) {
        return jpaRepository.findById(id).map(HomeownerMasterMapper::toDomain);
    }

    @Override
    public HomeownerMaster save(HomeownerMaster homeownerMaster) {
        return HomeownerMasterMapper.toDomain(jpaRepository.save(HomeownerMasterMapper.toEntity(homeownerMaster)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end

    @Override
    public List<HomeownerMaster> findAllInPropertyOrder() {
        return jpaRepository.findAllByOrderByPropertyNumberAscIdAsc().stream()
                .map(HomeownerMasterMapper::toDomain)
                .toList();
    }
}
