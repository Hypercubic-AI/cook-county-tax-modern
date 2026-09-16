package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.AssessmentParcelSourceRecord;

import java.util.List;

/// Reads preserved parcel prefixes needed for complete fixed-width publication.
public interface AssessmentParcelSourceRecordRepository {

    /// Returns every prefix in deterministic source assessment-master order.
    List<AssessmentParcelSourceRecord> findAllInSourceOrder();
}
