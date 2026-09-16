package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.MaintainedHomesteadExemption;
import org.cookcounty.tax.domain.port.out.MaintainedHomesteadExemptionRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.MaintainedHomesteadExemptionMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaMaintainedHomesteadExemptionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

/// Stores immutable maintained-homestead snapshots through mutable JPA entities.
@Component
public class MaintainedHomesteadExemptionRepositoryAdapter
        implements MaintainedHomesteadExemptionRepository {

    private final JpaMaintainedHomesteadExemptionRepository jpaRepository;

    /// Creates the adapter over the maintained-homestead persistence repository.
    public MaintainedHomesteadExemptionRepositoryAdapter(
            JpaMaintainedHomesteadExemptionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public List<MaintainedHomesteadExemption> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(MaintainedHomesteadExemptionMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MaintainedHomesteadExemption> findById(Long id) {
        return jpaRepository.findById(id).map(MaintainedHomesteadExemptionMapper::toDomain);
    }

    @Override
    public MaintainedHomesteadExemption save(
            MaintainedHomesteadExemption maintainedHomesteadExemption) {
        var saved =
                jpaRepository.save(
                        MaintainedHomesteadExemptionMapper.toEntity(maintainedHomesteadExemption));
        return MaintainedHomesteadExemptionMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
