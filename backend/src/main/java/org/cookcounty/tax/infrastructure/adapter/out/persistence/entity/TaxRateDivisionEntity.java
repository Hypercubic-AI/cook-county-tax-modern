package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/// Mutable persistence row for one source-ordered parcel-to-division association.
///
/// The protected constructor supports persistence proxies. A new row can have no version until the
/// persistence provider inserts it.
@Entity
@Table(name = "tax_rate_divisions")
public class TaxRateDivisionEntity {
    @Id
    @Column(name = "source_order")
    private @Nullable Integer sourceOrder;

    @Column(name = "volume_number", nullable = false, length = 3)
    private @Nullable String volumeNumber;

    @Column(name = "parcel_number", nullable = false, length = 15)
    private @Nullable String parcelNumber;

    @Column(name = "division_number", nullable = false, length = 14)
    private @Nullable String divisionNumber;

    @Version
    @Column(name = "version", nullable = false)
    private @Nullable Long version;

    /// Creates an unhydrated proxy instance. Application code receives mapped domain records.
    protected TaxRateDivisionEntity() {}

    /// Returns the deterministic input sequence.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getSourceOrder() {
        return Objects.requireNonNull(sourceOrder, "sourceOrder must be populated before reading");
    }

    /// Returns the volume portion of the parcel key.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getVolumeNumber() {
        return Objects.requireNonNull(
                volumeNumber, "volumeNumber must be populated before reading");
    }

    /// Returns the parcel portion of the source key.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getParcelNumber() {
        return Objects.requireNonNull(
                parcelNumber, "parcelNumber must be populated before reading");
    }

    /// Returns the division identifier stamped on a matching parcel.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getDivisionNumber() {
        return Objects.requireNonNull(
                divisionNumber, "divisionNumber must be populated before reading");
    }

    /// Returns the optimistic-lock value, or `null` before persistence hydration.
    public @Nullable Long getVersion() {
        return version;
    }
}
