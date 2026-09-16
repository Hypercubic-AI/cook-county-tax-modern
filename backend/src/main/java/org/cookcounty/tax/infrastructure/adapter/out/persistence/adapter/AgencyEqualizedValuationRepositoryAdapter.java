package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.AgencyEqualizedValuationMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAgencyEqualizedValuationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/// JPA adapter that maps complete agency equalized valuation snapshots.
@Component
public final class AgencyEqualizedValuationRepositoryAdapter
        implements AgencyEqualizedValuationRepository {

    private final JpaAgencyEqualizedValuationRepository jpaRepository;

    /// Uses the supplied repository for complete snapshot mapping and database version checks.
    public AgencyEqualizedValuationRepositoryAdapter(
            JpaAgencyEqualizedValuationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<AgencyEqualizedValuation> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(AgencyEqualizedValuationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AgencyEqualizedValuation> findById(Long id) {
        return jpaRepository.findById(id).map(AgencyEqualizedValuationMapper::toDomain);
    }

    @Override
    public AgencyEqualizedValuation save(AgencyEqualizedValuation agencyEqualizedValuation) {
        return AgencyEqualizedValuationMapper.toDomain(
                jpaRepository.save(
                        AgencyEqualizedValuationMapper.toEntity(agencyEqualizedValuation)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
