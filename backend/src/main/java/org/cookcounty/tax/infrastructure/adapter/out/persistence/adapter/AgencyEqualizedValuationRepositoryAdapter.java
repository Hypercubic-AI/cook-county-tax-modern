
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.AgencyEqualizedValuationMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAgencyEqualizedValuationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class AgencyEqualizedValuationRepositoryAdapter implements AgencyEqualizedValuationRepository {

    private final JpaAgencyEqualizedValuationRepository jpaRepository;

    public AgencyEqualizedValuationRepositoryAdapter(JpaAgencyEqualizedValuationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<AgencyEqualizedValuation> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(AgencyEqualizedValuationMapper::toDomain);
    }

    @Override
    public Optional<AgencyEqualizedValuation> findById(Long id) {
        return jpaRepository.findById(id).map(AgencyEqualizedValuationMapper::toDomain);
    }

    @Override
    public AgencyEqualizedValuation save(AgencyEqualizedValuation agencyEqualizedValuation) {
        return AgencyEqualizedValuationMapper.toDomain(jpaRepository.save(AgencyEqualizedValuationMapper.toEntity(agencyEqualizedValuation)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
