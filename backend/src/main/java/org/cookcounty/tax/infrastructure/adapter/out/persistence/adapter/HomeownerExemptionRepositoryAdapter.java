
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.HomeownerExemptionMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaHomeownerExemptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class HomeownerExemptionRepositoryAdapter implements HomeownerExemptionRepository {

    private final JpaHomeownerExemptionRepository jpaRepository;

    public HomeownerExemptionRepositoryAdapter(JpaHomeownerExemptionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<HomeownerExemption> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(HomeownerExemptionMapper::toDomain);
    }

    @Override
    public Optional<HomeownerExemption> findById(Long id) {
        return jpaRepository.findById(id).map(HomeownerExemptionMapper::toDomain);
    }

    @Override
    public HomeownerExemption save(HomeownerExemption homeownerExemption) {
        return HomeownerExemptionMapper.toDomain(jpaRepository.save(HomeownerExemptionMapper.toEntity(homeownerExemption)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
