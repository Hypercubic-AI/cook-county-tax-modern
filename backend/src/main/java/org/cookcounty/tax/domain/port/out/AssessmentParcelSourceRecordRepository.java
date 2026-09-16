package org.cookcounty.tax.domain.port.out;

import java.util.List;

import org.cookcounty.tax.domain.model.AssessmentParcelSourceRecord;

public interface AssessmentParcelSourceRecordRepository {
    // GENERATED-METHODS:start
    // GENERATED-METHODS:end

    List<AssessmentParcelSourceRecord> findAllInSourceOrder();
}
