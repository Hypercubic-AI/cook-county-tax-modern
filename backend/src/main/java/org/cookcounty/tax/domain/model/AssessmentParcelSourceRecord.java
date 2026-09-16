package org.cookcounty.tax.domain.model;

/** Preserved source envelope for fields outside the modern assessed parcel model. */
public record AssessmentParcelSourceRecord(
        long parcelNumber,
        int sourceOrder,
        String sourceRecordBase64) {
    // GENERATED-FIELDS:start
    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    // GENERATED-ACCESSORS:end
}
