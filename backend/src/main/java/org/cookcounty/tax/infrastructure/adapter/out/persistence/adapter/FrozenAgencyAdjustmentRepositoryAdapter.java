
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.FrozenAgencyAdjustmentMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaFrozenAgencyAdjustmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class FrozenAgencyAdjustmentRepositoryAdapter implements FrozenAgencyAdjustmentRepository {

    private final JpaFrozenAgencyAdjustmentRepository jpaRepository;

    public FrozenAgencyAdjustmentRepositoryAdapter(JpaFrozenAgencyAdjustmentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<FrozenAgencyAdjustment> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(FrozenAgencyAdjustmentMapper::toDomain);
    }

    @Override
    public Optional<FrozenAgencyAdjustment> findById(Long id) {
        return jpaRepository.findById(id).map(FrozenAgencyAdjustmentMapper::toDomain);
    }

    @Override
    public FrozenAgencyAdjustment save(FrozenAgencyAdjustment frozenAgencyAdjustment) {
        return FrozenAgencyAdjustmentMapper.toDomain(jpaRepository.save(FrozenAgencyAdjustmentMapper.toEntity(frozenAgencyAdjustment)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end

    @Override
    public Optional<FrozenAgencyAdjustment> findByTaxCodeAndAgencyNumber(
            String taxCode, String agencyNumber) {
        return jpaRepository.findByTaxCodeAndAgencyNumber(taxCode, agencyNumber)
                .map(FrozenAgencyAdjustmentMapper::toDomain);
    }
}
