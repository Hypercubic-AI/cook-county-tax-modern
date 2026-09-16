package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import java.util.List;

import org.cookcounty.tax.domain.model.AssessmentParcelSourceRecord;
import org.cookcounty.tax.domain.port.out.AssessmentParcelSourceRecordRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAssessmentParcelSourceRecordRepository;
import org.springframework.stereotype.Component;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public final class AssessmentParcelSourceRecordRepositoryAdapter
        implements AssessmentParcelSourceRecordRepository {

    private final JpaAssessmentParcelSourceRecordRepository repository;

    public AssessmentParcelSourceRecordRepositoryAdapter(
            JpaAssessmentParcelSourceRecordRepository repository) {
        this.repository = repository;
    }

    // GENERATED-OVERRIDES:start
    // GENERATED-OVERRIDES:end

    @Override
    public List<AssessmentParcelSourceRecord> findAllInSourceOrder() {
        return repository.findAllByOrderBySourceOrderAsc().stream()
                .map(entity -> new AssessmentParcelSourceRecord(
                        entity.getParcelNumber(),
                        entity.getSourceOrder(),
                        entity.getSourceRecordBase64()))
                .toList();
    }
}
