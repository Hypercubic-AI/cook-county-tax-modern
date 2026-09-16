package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;
import org.cookcounty.tax.domain.port.out.SeniorFreezeApplicantRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.SeniorFreezeApplicantMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaSeniorFreezeApplicantRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

/// Stores immutable Senior Freeze applicant snapshots through mutable JPA entities.
@Component
public class SeniorFreezeApplicantRepositoryAdapter implements SeniorFreezeApplicantRepository {

    private final JpaSeniorFreezeApplicantRepository jpaRepository;

    /// Creates the adapter over the Senior Freeze applicant persistence repository.
    public SeniorFreezeApplicantRepositoryAdapter(
            JpaSeniorFreezeApplicantRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public List<SeniorFreezeApplicant> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(SeniorFreezeApplicantMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SeniorFreezeApplicant> findById(Long id) {
        return jpaRepository.findById(id).map(SeniorFreezeApplicantMapper::toDomain);
    }

    @Override
    public SeniorFreezeApplicant save(SeniorFreezeApplicant seniorFreezeApplicant) {
        var saved = jpaRepository.save(SeniorFreezeApplicantMapper.toEntity(seniorFreezeApplicant));
        return SeniorFreezeApplicantMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
