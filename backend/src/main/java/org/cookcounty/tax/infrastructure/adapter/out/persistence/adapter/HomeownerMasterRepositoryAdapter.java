package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.HomeownerMasterMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaHomeownerMasterRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

/// Stores immutable homeowner-master snapshots through mutable JPA entities.
@Component
public class HomeownerMasterRepositoryAdapter implements HomeownerMasterRepository {

    private final JpaHomeownerMasterRepository jpaRepository;

    /// Creates the adapter over the homeowner-master persistence repository.
    public HomeownerMasterRepositoryAdapter(JpaHomeownerMasterRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public List<HomeownerMaster> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(HomeownerMasterMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<HomeownerMaster> findById(Long id) {
        return jpaRepository.findById(id).map(HomeownerMasterMapper::toDomain);
    }

    @Override
    public HomeownerMaster save(HomeownerMaster homeownerMaster) {
        var saved = jpaRepository.save(HomeownerMasterMapper.toEntity(homeownerMaster));
        return HomeownerMasterMapper.toDomain(saved);
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
