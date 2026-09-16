package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.domain.model.AssessmentParcelSourceRecord;
import org.cookcounty.tax.domain.port.out.AssessmentParcelSourceRecordRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAssessmentParcelSourceRecordRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/// Reads preserved fixed-width parcel prefixes from their persistence table.
@Component
public final class AssessmentParcelSourceRecordRepositoryAdapter
        implements AssessmentParcelSourceRecordRepository {

    private final JpaAssessmentParcelSourceRecordRepository repository;

    /// Creates the adapter for preserved source prefixes.
    ///
    /// @param repository Spring Data repository that owns prefix reads
    public AssessmentParcelSourceRecordRepositoryAdapter(
            JpaAssessmentParcelSourceRecordRepository repository) {
        this.repository = repository;
    }

    /// Returns immutable prefix records in stable source order.
    @Override
    public List<AssessmentParcelSourceRecord> findAllInSourceOrder() {
        return repository.findAllByOrderBySourceOrderAsc().stream()
                .map(
                        entity ->
                                new AssessmentParcelSourceRecord(
                                        requireNonNull(
                                                entity.getParcelNumber(), "source parcel number"),
                                        requireNonNull(entity.getSourceOrder(), "source order"),
                                        requireNonNull(
                                                entity.getSourceRecordBase64(),
                                                "source record bytes")))
                .toList();
    }
}
