package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.HomeownerExemptionMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaHomeownerExemptionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

/// Stores immutable homeowner-exemption snapshots through mutable JPA entities.
@Component
public class HomeownerExemptionRepositoryAdapter implements HomeownerExemptionRepository {

    private final JpaHomeownerExemptionRepository jpaRepository;

    /// Creates the adapter over the homeowner-exemption persistence repository.
    public HomeownerExemptionRepositoryAdapter(JpaHomeownerExemptionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public List<HomeownerExemption> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(HomeownerExemptionMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<HomeownerExemption> findById(Long id) {
        return jpaRepository.findById(id).map(HomeownerExemptionMapper::toDomain);
    }

    @Override
    public HomeownerExemption save(HomeownerExemption homeownerExemption) {
        var saved = jpaRepository.save(HomeownerExemptionMapper.toEntity(homeownerExemption));
        return HomeownerExemptionMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
