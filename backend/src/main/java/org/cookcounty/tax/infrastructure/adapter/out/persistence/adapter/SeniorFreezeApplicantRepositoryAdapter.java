
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;
import org.cookcounty.tax.domain.port.out.SeniorFreezeApplicantRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.SeniorFreezeApplicantMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaSeniorFreezeApplicantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class SeniorFreezeApplicantRepositoryAdapter implements SeniorFreezeApplicantRepository {

    private final JpaSeniorFreezeApplicantRepository jpaRepository;

    public SeniorFreezeApplicantRepositoryAdapter(JpaSeniorFreezeApplicantRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<SeniorFreezeApplicant> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(SeniorFreezeApplicantMapper::toDomain);
    }

    @Override
    public Optional<SeniorFreezeApplicant> findById(Long id) {
        return jpaRepository.findById(id).map(SeniorFreezeApplicantMapper::toDomain);
    }

    @Override
    public SeniorFreezeApplicant save(SeniorFreezeApplicant seniorFreezeApplicant) {
        return SeniorFreezeApplicantMapper.toDomain(jpaRepository.save(SeniorFreezeApplicantMapper.toEntity(seniorFreezeApplicant)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
