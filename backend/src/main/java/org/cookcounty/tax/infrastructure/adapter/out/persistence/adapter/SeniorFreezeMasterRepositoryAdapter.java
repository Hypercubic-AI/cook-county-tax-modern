package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.SeniorFreezeMaster;
import org.cookcounty.tax.domain.port.out.SeniorFreezeMasterRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.SeniorFreezeMasterMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaSeniorFreezeMasterRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

/// Stores immutable Senior Freeze master snapshots through mutable JPA entities.
@Component
public class SeniorFreezeMasterRepositoryAdapter implements SeniorFreezeMasterRepository {

    private final JpaSeniorFreezeMasterRepository jpaRepository;

    /// Creates the adapter over the Senior Freeze master persistence repository.
    public SeniorFreezeMasterRepositoryAdapter(JpaSeniorFreezeMasterRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public List<SeniorFreezeMaster> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(SeniorFreezeMasterMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SeniorFreezeMaster> findById(Long id) {
        return jpaRepository.findById(id).map(SeniorFreezeMasterMapper::toDomain);
    }

    @Override
    public SeniorFreezeMaster save(SeniorFreezeMaster seniorFreezeMaster) {
        var saved = jpaRepository.save(SeniorFreezeMasterMapper.toEntity(seniorFreezeMaster));
        return SeniorFreezeMasterMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
