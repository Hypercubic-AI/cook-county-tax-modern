package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Mutable persistence row for one source-ordered parcel value.
///
/// The protected constructor supports persistence proxies. A new row can have no version until the
/// persistence provider inserts it.
@Entity
@Table(name = "tax_rate_equalized_values")
public class TaxRateEqualizedValueEntity {
    @Id
    @Column(name = "source_order")
    private @Nullable Integer sourceOrder;

    @Column(name = "volume_number", nullable = false, length = 3)
    private @Nullable String volumeNumber;

    @Column(name = "parcel_number", nullable = false, length = 15)
    private @Nullable String parcelNumber;

    @Column(name = "tax_code", nullable = false, length = 5)
    private @Nullable String taxCode;

    @Column(name = "assessed_value", nullable = false, precision = 11, scale = 0)
    private @Nullable BigDecimal assessedValue;

    @Column(name = "equalized_value", nullable = false, precision = 11, scale = 0)
    private @Nullable BigDecimal equalizedValue;

    @Column(name = "tax_type", nullable = false)
    private @Nullable String taxType;

    @Version
    @Column(name = "version", nullable = false)
    private @Nullable Long version;

    /// Creates an unhydrated proxy instance. Application code receives mapped domain records.
    protected TaxRateEqualizedValueEntity() {}

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

    /// Returns the numeric source form of the five-character tax-code identifier.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getTaxCode() {
        return Objects.requireNonNull(taxCode, "taxCode must be populated before reading");
    }

    /// Returns whole assessed-value units carried to output.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getAssessedValue() {
        return Objects.requireNonNull(
                assessedValue, "assessedValue must be populated before reading");
    }

    /// Returns whole equalized-value units carried to output and posting calculations.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getEqualizedValue() {
        return Objects.requireNonNull(
                equalizedValue, "equalizedValue must be populated before reading");
    }

    /// Returns the tax-type code used in attachment ordering.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getTaxType() {
        return Objects.requireNonNull(taxType, "taxType must be populated before reading");
    }

    /// Returns the optimistic-lock value, or `null` before persistence hydration.
    public @Nullable Long getVersion() {
        return version;
    }
}
