
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.SeniorFreezeMaster;
import org.cookcounty.tax.domain.port.out.SeniorFreezeMasterRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.SeniorFreezeMasterMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaSeniorFreezeMasterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class SeniorFreezeMasterRepositoryAdapter implements SeniorFreezeMasterRepository {

    private final JpaSeniorFreezeMasterRepository jpaRepository;

    public SeniorFreezeMasterRepositoryAdapter(JpaSeniorFreezeMasterRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<SeniorFreezeMaster> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(SeniorFreezeMasterMapper::toDomain);
    }

    @Override
    public Optional<SeniorFreezeMaster> findById(Long id) {
        return jpaRepository.findById(id).map(SeniorFreezeMasterMapper::toDomain);
    }

    @Override
    public SeniorFreezeMaster save(SeniorFreezeMaster seniorFreezeMaster) {
        return SeniorFreezeMasterMapper.toDomain(jpaRepository.save(SeniorFreezeMasterMapper.toEntity(seniorFreezeMaster)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end
}
