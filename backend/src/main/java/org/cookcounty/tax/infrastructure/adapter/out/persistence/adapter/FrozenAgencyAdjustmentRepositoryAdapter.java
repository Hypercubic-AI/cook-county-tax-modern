package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.FrozenAgencyAdjustmentMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaFrozenAgencyAdjustmentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/// JPA adapter that maps complete frozen agency adjustment snapshots.
@Component
public final class FrozenAgencyAdjustmentRepositoryAdapter
        implements FrozenAgencyAdjustmentRepository {

    private final JpaFrozenAgencyAdjustmentRepository jpaRepository;

    /// Uses the supplied repository for complete adjustment mapping and database version checks.
    public FrozenAgencyAdjustmentRepositoryAdapter(
            JpaFrozenAgencyAdjustmentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<FrozenAgencyAdjustment> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(FrozenAgencyAdjustmentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<FrozenAgencyAdjustment> findById(Long id) {
        return jpaRepository.findById(id).map(FrozenAgencyAdjustmentMapper::toDomain);
    }

    @Override
    public FrozenAgencyAdjustment save(FrozenAgencyAdjustment frozenAgencyAdjustment) {
        return FrozenAgencyAdjustmentMapper.toDomain(
                jpaRepository.save(FrozenAgencyAdjustmentMapper.toEntity(frozenAgencyAdjustment)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<FrozenAgencyAdjustment> findByTaxCodeAndAgencyNumber(
            String taxCode, String agencyNumber) {
        return jpaRepository
                .findByTaxCodeAndAgencyNumber(taxCode, agencyNumber)
                .map(FrozenAgencyAdjustmentMapper::toDomain);
    }
}
