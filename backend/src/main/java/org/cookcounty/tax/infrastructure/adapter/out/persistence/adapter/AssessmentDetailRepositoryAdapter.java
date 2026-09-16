
package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.AssessmentDetailMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAssessmentDetailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class AssessmentDetailRepositoryAdapter implements AssessmentDetailRepository {

    private final JpaAssessmentDetailRepository jpaRepository;

    public AssessmentDetailRepositoryAdapter(JpaAssessmentDetailRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // GENERATED-OVERRIDES:start
    @Override
    public Page<AssessmentDetail> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(AssessmentDetailMapper::toDomain);
    }

    @Override
    public Optional<AssessmentDetail> findById(Long id) {
        return jpaRepository.findById(id).map(AssessmentDetailMapper::toDomain);
    }

    @Override
    public AssessmentDetail save(AssessmentDetail assessmentDetail) {
        return AssessmentDetailMapper.toDomain(jpaRepository.save(AssessmentDetailMapper.toEntity(assessmentDetail)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    // GENERATED-OVERRIDES:end

    @Override
    public List<AssessmentDetail> findAllInInputOrder() {
        return jpaRepository.findAllByOrderByIdAsc().stream()
                .map(AssessmentDetailMapper::toDomain)
                .toList();
    }
}
