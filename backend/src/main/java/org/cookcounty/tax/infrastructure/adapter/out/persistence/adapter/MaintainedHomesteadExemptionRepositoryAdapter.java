
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.MaintainedHomesteadExemption;
import org.cookcounty.tax.domain.port.out.MaintainedHomesteadExemptionRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.MaintainedHomesteadExemptionMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaMaintainedHomesteadExemptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class MaintainedHomesteadExemptionRepositoryAdapter implements MaintainedHomesteadExemptionRepository {

    private final JpaMaintainedHomesteadExemptionRepository jpaRepository;

    public MaintainedHomesteadExemptionRepositoryAdapter(JpaMaintainedHomesteadExemptionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<MaintainedHomesteadExemption> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(MaintainedHomesteadExemptionMapper::toDomain);
    }

    @Override
    public Optional<MaintainedHomesteadExemption> findById(Long id) {
        return jpaRepository.findById(id).map(MaintainedHomesteadExemptionMapper::toDomain);
    }

    @Override
    public MaintainedHomesteadExemption save(MaintainedHomesteadExemption maintainedHomesteadExemption) {
        return MaintainedHomesteadExemptionMapper.toDomain(jpaRepository.save(MaintainedHomesteadExemptionMapper.toEntity(maintainedHomesteadExemption)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
