package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Mutable JPA boundary for tax code master reference persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(name = "tax_code_master_references")
public class TaxCodeMasterReferenceEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public TaxCodeMasterReferenceEntity() {}

    /// Optimistic-lock version. It is absent before persistence hydration.
    @jakarta.persistence.Version private @Nullable Long version;

    @Id
    @Column(name = "tax_code", length = 5, nullable = false)
    private @Nullable String taxCode;

    @Column(name = "tax_rate", precision = 7, scale = 3, nullable = false)
    private @Nullable BigDecimal taxRate;

    /// Returns the hydrated tax code value.
    public @Nullable String getTaxCode() {
        return taxCode;
    }

    /// Supplies the tax code value during hydration or mapping.
    public void setTaxCode(@Nullable String taxCode) {
        this.taxCode = taxCode;
    }

    /// Returns the hydrated tax rate value.
    public @Nullable BigDecimal getTaxRate() {
        return taxRate;
    }

    /// Supplies the tax rate value during hydration or mapping.
    public void setTaxRate(@Nullable BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    /// Returns the hydrated optimistic-lock version.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Supplies the optimistic-lock version during hydration.
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }
}
