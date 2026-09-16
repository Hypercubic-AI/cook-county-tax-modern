package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/// Read-only persistence row for one renewal input in deterministic source order.
///
/// The version is absent only before JPA hydrates the row. It protects maintained renewal input
/// from concurrent replacement even though the domain exposes only read behavior.
@Entity
@Table(name = "property_tax_renewals")
public class PropertyTaxRenewalEntity {
    /// Recorded input position. Ordering uses this identity, not the property number.
    @Id
    @Column(name = "source_order")
    private @Nullable Integer sourceOrder;

    /// Optimistic-lock state for the maintained row. Null means JPA has not supplied it.
    @Version private @Nullable Long version;

    /// Canonical 15-digit property key, including leading zeros.
    @Column(name = "property_number", nullable = false, length = 15)
    private @Nullable String propertyNumber;

    /// Five-character renewal batch identifier retained in the report.
    @Column(name = "batch_number", nullable = false, length = 5)
    private @Nullable String batchNumber;

    protected PropertyTaxRenewalEntity() {}

    /// Returns the recorded input position used to reproduce renewal processing order.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getSourceOrder() {
        return Objects.requireNonNull(sourceOrder, "sourceOrder must be populated before reading");
    }

    /// Returns the canonical 15-digit property key without converting it to a number.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getPropertyNumber() {
        return Objects.requireNonNull(
                propertyNumber, "propertyNumber must be populated before reading");
    }

    /// Returns the five-character batch identifier carried to renewal reporting.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBatchNumber() {
        return Objects.requireNonNull(batchNumber, "batchNumber must be populated before reading");
    }
}
