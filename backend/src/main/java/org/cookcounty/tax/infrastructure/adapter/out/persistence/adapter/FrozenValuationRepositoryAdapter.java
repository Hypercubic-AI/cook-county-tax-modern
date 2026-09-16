package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.FrozenValuationMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaFrozenValuationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/// JPA adapter that maps complete frozen valuation snapshots.
@Component
public final class FrozenValuationRepositoryAdapter implements FrozenValuationRepository {

    private final JpaFrozenValuationRepository jpaRepository;

    /// Uses the supplied repository for complete valuation mapping and database version checks.
    public FrozenValuationRepositoryAdapter(JpaFrozenValuationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<FrozenValuation> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(FrozenValuationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FrozenValuation> findById(Long id) {
        return jpaRepository.findById(id).map(FrozenValuationMapper::toDomain);
    }

    @Override
    public FrozenValuation save(FrozenValuation frozenValuation) {
        return FrozenValuationMapper.toDomain(
                jpaRepository.save(FrozenValuationMapper.toEntity(frozenValuation)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
