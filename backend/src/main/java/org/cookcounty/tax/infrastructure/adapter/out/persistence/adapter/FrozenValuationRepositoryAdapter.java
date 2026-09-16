
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.FrozenValuationMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaFrozenValuationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class FrozenValuationRepositoryAdapter implements FrozenValuationRepository {

    private final JpaFrozenValuationRepository jpaRepository;

    public FrozenValuationRepositoryAdapter(JpaFrozenValuationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<FrozenValuation> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(FrozenValuationMapper::toDomain);
    }

    @Override
    public Optional<FrozenValuation> findById(Long id) {
        return jpaRepository.findById(id).map(FrozenValuationMapper::toDomain);
    }

    @Override
    public FrozenValuation save(FrozenValuation frozenValuation) {
        return FrozenValuationMapper.toDomain(jpaRepository.save(FrozenValuationMapper.toEntity(frozenValuation)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
