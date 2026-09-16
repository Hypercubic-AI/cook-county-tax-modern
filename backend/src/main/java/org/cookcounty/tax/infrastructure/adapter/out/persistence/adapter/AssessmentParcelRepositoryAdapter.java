
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.AssessmentParcelMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAssessmentParcelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class AssessmentParcelRepositoryAdapter implements AssessmentParcelRepository {

    private final JpaAssessmentParcelRepository jpaRepository;

    public AssessmentParcelRepositoryAdapter(JpaAssessmentParcelRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<AssessmentParcel> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(AssessmentParcelMapper::toDomain);
    }

    @Override
    public Optional<AssessmentParcel> findById(Long id) {
        return jpaRepository.findById(id).map(AssessmentParcelMapper::toDomain);
    }

    @Override
    public AssessmentParcel save(AssessmentParcel assessmentParcel) {
        return AssessmentParcelMapper.toDomain(jpaRepository.save(AssessmentParcelMapper.toEntity(assessmentParcel)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end

    @Override
    public List<AssessmentParcel> findAllInInputOrder() {
        return jpaRepository.findAllByOrderByIdAsc().stream()
                .map(AssessmentParcelMapper::toDomainWithPriorParcelStatus)
                .toList();
    }
}
