package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

/// Mutable JPA boundary for tax code agency slot persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(name = "tax_code_agency_slots")
public class TaxCodeAgencySlotEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public TaxCodeAgencySlotEntity() {}

    /// Optimistic-lock version. It is absent before persistence hydration.
    @jakarta.persistence.Version private @Nullable Long version;

    @EmbeddedId private @Nullable TaxCodeAgencySlotId id;

    @Column(name = "agency_number", length = 9, nullable = false)
    private @Nullable String agencyNumber;

    /// Returns the hydrated id value.
    public @Nullable TaxCodeAgencySlotId getId() {
        return id;
    }

    /// Supplies the id value during hydration or mapping.
    public void setId(@Nullable TaxCodeAgencySlotId id) {
        this.id = id;
    }

    /// Returns the hydrated agency number value.
    public @Nullable String getAgencyNumber() {
        return agencyNumber;
    }

    /// Supplies the agency number value during hydration or mapping.
    public void setAgencyNumber(@Nullable String agencyNumber) {
        this.agencyNumber = agencyNumber;
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
