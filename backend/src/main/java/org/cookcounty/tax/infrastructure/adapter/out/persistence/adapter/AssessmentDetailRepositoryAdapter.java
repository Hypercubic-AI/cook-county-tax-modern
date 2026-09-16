package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.AssessmentDetailMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAssessmentDetailRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/// Persists immutable assessment details through mutable JPA entities.
@Component
public final class AssessmentDetailRepositoryAdapter implements AssessmentDetailRepository {

    private final JpaAssessmentDetailRepository jpaRepository;

    /// Creates the adapter for the assessment-detail table.
    ///
    /// @param jpaRepository Spring Data repository that owns database access
    public AssessmentDetailRepositoryAdapter(JpaAssessmentDetailRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /// Returns details in generated-identity order.
    @Override
    public List<AssessmentDetail> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(AssessmentDetailMapper::toDomain)
                .toList();
    }

    /// Returns one detail by generated identity when it exists.
    @Override
    public Optional<AssessmentDetail> findById(Long id) {
        return jpaRepository.findById(id).map(AssessmentDetailMapper::toDomain);
    }

    /// Saves the complete detail and returns the database-assigned identity and version.
    @Override
    public AssessmentDetail save(AssessmentDetail assessmentDetail) {
        return AssessmentDetailMapper.toDomain(
                jpaRepository.save(AssessmentDetailMapper.toEntity(assessmentDetail)));
    }

    /// Deletes the detail with the supplied generated identity.
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    /// Returns details in stable source ingestion order.
    @Override
    public List<AssessmentDetail> findAllInInputOrder() {
        return jpaRepository.findAllByOrderByIdAsc().stream()
                .map(AssessmentDetailMapper::toDomain)
                .toList();
    }
}
