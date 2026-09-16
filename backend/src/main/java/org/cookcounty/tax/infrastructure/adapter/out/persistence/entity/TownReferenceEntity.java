package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

/// Mutable JPA boundary for town reference persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(name = "town_references")
public class TownReferenceEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public TownReferenceEntity() {}

    /// Optimistic-lock version. It is absent before persistence hydration.
    @jakarta.persistence.Version private @Nullable Long version;

    @Id
    @Column(name = "town_number", length = 2, nullable = false)
    private @Nullable String townNumber;

    @Column(name = "name", length = 13, nullable = false)
    private @Nullable String name;

    /// Returns the hydrated town number value.
    public @Nullable String getTownNumber() {
        return townNumber;
    }

    /// Supplies the town number value during hydration or mapping.
    public void setTownNumber(@Nullable String townNumber) {
        this.townNumber = townNumber;
    }

    /// Returns the hydrated name value.
    public @Nullable String getName() {
        return name;
    }

    /// Supplies the name value during hydration or mapping.
    public void setName(@Nullable String name) {
        this.name = name;
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
