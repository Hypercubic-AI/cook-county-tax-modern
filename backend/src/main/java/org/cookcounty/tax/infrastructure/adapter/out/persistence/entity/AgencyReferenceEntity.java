package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

/// Mutable JPA boundary for agency reference persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(name = "agency_references")
public class AgencyReferenceEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public AgencyReferenceEntity() {}

    /// Optimistic-lock version. It is absent before persistence hydration.
    @jakarta.persistence.Version private @Nullable Long version;

    @Id
    @Column(name = "agency_number", length = 9, nullable = false)
    private @Nullable String agencyNumber;

    @Column(name = "description", length = 44, nullable = false)
    private @Nullable String description;

    /// Returns the hydrated agency number value.
    public @Nullable String getAgencyNumber() {
        return agencyNumber;
    }

    /// Supplies the agency number value during hydration or mapping.
    public void setAgencyNumber(@Nullable String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    /// Returns the hydrated description value.
    public @Nullable String getDescription() {
        return description;
    }

    /// Supplies the description value during hydration or mapping.
    public void setDescription(@Nullable String description) {
        this.description = description;
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
