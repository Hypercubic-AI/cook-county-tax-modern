package org.cookcounty.tax.domain.model;

import java.util.Objects;

/// Preserved fixed-width parcel bytes used to publish a complete assessment-master record.
///
/// The assessed-value projector overlays mapped parcel and detail fields on these bytes. It keeps
/// fields outside the modern domain model unchanged. Source order is the publication order.
///
/// @param parcelNumber signed fifteen-digit parcel identifier that links the envelope to its parcel
/// @param sourceOrder one-based order from the source assessment-master input
/// @param sourceRecordBase64 Base64 encoding of the preserved 122-byte parcel prefix
public record AssessmentParcelSourceRecord(
        String parcelNumber, int sourceOrder, String sourceRecordBase64) {
    /// Validates the business key and required preserved bytes.
    public AssessmentParcelSourceRecord {
        parcelNumber = AssessmentNumericBoundary.signedIdentifier(parcelNumber, 15, "parcelNumber");
        Objects.requireNonNull(sourceRecordBase64, "sourceRecordBase64");
    }
}
