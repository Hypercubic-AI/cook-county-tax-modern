package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.jspecify.annotations.Nullable;

/// Mutable persistence boundary for one preserved parcel source prefix.
///
/// Stored source bytes remain read-only domain evidence. The version participates in optimistic
/// locking at this persistence boundary and is intentionally not exposed by the read-only domain
/// projection. Every field can be absent only before JPA hydration.
@Entity
@Table(name = "assessment_parcel_source_records")
public class AssessmentParcelSourceRecordEntity {

    @Id
    @Column(name = "parcel_number", nullable = false, length = 16)
    private @Nullable String parcelNumber;

    @Column(name = "source_order", nullable = false, unique = true)
    private @Nullable Integer sourceOrder;

    @Column(name = "source_record_base64", nullable = false)
    private @Nullable String sourceRecordBase64;

    @Version
    @Column(name = "version", nullable = false)
    private @Nullable Long version;

    /// Creates an unhydrated entity for JPA.
    public AssessmentParcelSourceRecordEntity() {}

    /// Returns the linking parcel identifier, or `null` before hydration.
    public @Nullable String getParcelNumber() {
        return parcelNumber;
    }

    /// Sets the linking parcel identifier during hydration.
    public void setParcelNumber(@Nullable String parcelNumber) {
        this.parcelNumber = parcelNumber;
    }

    /// Returns the source publication order, or `null` before hydration.
    public @Nullable Integer getSourceOrder() {
        return sourceOrder;
    }

    /// Sets the source publication order during hydration.
    public void setSourceOrder(@Nullable Integer sourceOrder) {
        this.sourceOrder = sourceOrder;
    }

    /// Returns the Base64 source prefix, or `null` before hydration.
    public @Nullable String getSourceRecordBase64() {
        return sourceRecordBase64;
    }

    /// Sets the Base64 source prefix during hydration.
    public void setSourceRecordBase64(@Nullable String sourceRecordBase64) {
        this.sourceRecordBase64 = sourceRecordBase64;
    }

    /// Returns the optimistic-lock version, or `null` before hydration or the first save.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Sets the optimistic-lock version during hydration.
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }
}
