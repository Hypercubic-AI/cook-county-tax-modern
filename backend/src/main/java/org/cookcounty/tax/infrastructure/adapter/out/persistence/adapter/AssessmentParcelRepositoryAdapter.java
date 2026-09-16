package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.AssessmentParcelMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAssessmentParcelRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/// Persists immutable assessment parcels through mutable JPA entities.
@Component
public final class AssessmentParcelRepositoryAdapter implements AssessmentParcelRepository {

    private final JpaAssessmentParcelRepository jpaRepository;

    /// Creates the adapter for the assessment-parcel table.
    ///
    /// @param jpaRepository Spring Data repository that owns database access
    public AssessmentParcelRepositoryAdapter(JpaAssessmentParcelRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /// Returns parcels in generated-identity order.
    @Override
    public List<AssessmentParcel> findAllInPersistenceOrder() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(AssessmentParcelMapper::toDomain)
                .toList();
    }

    /// Returns one parcel by generated identity when it exists.
    @Override
    public Optional<AssessmentParcel> findById(Long id) {
        return jpaRepository.findById(id).map(AssessmentParcelMapper::toDomain);
    }

    /// Saves the complete parcel and returns the database-assigned identity and version.
    @Override
    public AssessmentParcel save(AssessmentParcel assessmentParcel) {
        return AssessmentParcelMapper.toDomain(
                jpaRepository.save(AssessmentParcelMapper.toEntity(assessmentParcel)));
    }

    /// Deletes the parcel with the supplied generated identity.
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    /// Returns parcels in stable source ingestion order.
    @Override
    public List<AssessmentParcel> findAllInInputOrder() {
        return jpaRepository.findAllByOrderByIdAsc().stream()
                .map(AssessmentParcelMapper::toDomain)
                .toList();
    }
}
