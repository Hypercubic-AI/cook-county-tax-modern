package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

/// Mutable JPA boundary for frozen valuation persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(name = "frozen_valuations")
public class FrozenValuationEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public FrozenValuationEntity() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Version private @Nullable Long version;

    // GENERATED-FIELDS:start

    @Column(name = "change_action_current_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal changeActionCurrentImprovementValue;

    @Column(name = "change_action_current_land_value", precision = 13, scale = 0)
    private @Nullable BigDecimal changeActionCurrentLandValue;

    @Column(name = "change_action_current_parcel_count")
    private @Nullable Long changeActionCurrentParcelCount;

    @Column(name = "change_action_current_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal changeActionCurrentTotalValue;

    @Column(name = "change_action_prior_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal changeActionPriorImprovementValue;

    @Column(name = "change_action_prior_land_value", precision = 13, scale = 0)
    private @Nullable BigDecimal changeActionPriorLandValue;

    @Column(name = "change_action_prior_parcel_count")
    private @Nullable Long changeActionPriorParcelCount;

    @Column(name = "change_action_prior_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal changeActionPriorTotalValue;

    @Column(name = "current_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal currentImprovementValue;

    @Column(name = "current_land_value", precision = 13, scale = 0)
    private @Nullable BigDecimal currentLandValue;

    @Column(name = "current_parcel_count")
    private @Nullable Long currentParcelCount;

    @Column(name = "current_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal currentTotalValue;

    @Column(name = "division_number", length = 14)
    private @Nullable String divisionNumber;

    @Column(name = "no_change_action_current_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal noChangeActionCurrentImprovementValue;

    @Column(name = "no_change_action_current_land_value", precision = 13, scale = 0)
    private @Nullable BigDecimal noChangeActionCurrentLandValue;

    @Column(name = "no_change_action_current_parcel_count")
    private @Nullable Long noChangeActionCurrentParcelCount;

    @Column(name = "no_change_action_current_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal noChangeActionCurrentTotalValue;

    @Column(name = "no_change_action_prior_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal noChangeActionPriorImprovementValue;

    @Column(name = "no_change_action_prior_land_value", precision = 13, scale = 0)
    private @Nullable BigDecimal noChangeActionPriorLandValue;

    @Column(name = "no_change_action_prior_parcel_count")
    private @Nullable Long noChangeActionPriorParcelCount;

    @Column(name = "no_change_action_prior_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal noChangeActionPriorTotalValue;

    @Column(name = "prior_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal priorImprovementValue;

    @Column(name = "prior_land_value", precision = 13, scale = 0)
    private @Nullable BigDecimal priorLandValue;

    @Column(name = "prior_parcel_count")
    private @Nullable Long priorParcelCount;

    @Column(name = "prior_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal priorTotalValue;

    @Column(name = "proposed_actual_value", precision = 13, scale = 0)
    private @Nullable BigDecimal proposedActualValue;

    @Column(name = "proposed_current288_value", precision = 13, scale = 0)
    private @Nullable BigDecimal proposedCurrent288Value;

    @Column(name = "proposed_expired288_value", precision = 13, scale = 0)
    private @Nullable BigDecimal proposedExpired288Value;

    @Column(name = "proposed_improvement_value", precision = 13, scale = 0)
    private @Nullable BigDecimal proposedImprovementValue;

    @Column(name = "proposed_total_value", precision = 13, scale = 0)
    private @Nullable BigDecimal proposedTotalValue;

    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    /// Returns the hydrated id value.
    public @Nullable Long getId() {
        return id;
    }

    /// Supplies the id value during hydration or mapping.
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /// Returns the hydrated version value.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Supplies the version value during hydration or mapping.
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    /// Returns the hydrated change action current improvement value value.
    public @Nullable BigDecimal getChangeActionCurrentImprovementValue() {
        return changeActionCurrentImprovementValue;
    }

    /// Supplies the change action current improvement value value during hydration or mapping.
    public void setChangeActionCurrentImprovementValue(
            @Nullable BigDecimal changeActionCurrentImprovementValue) {
        this.changeActionCurrentImprovementValue = changeActionCurrentImprovementValue;
    }

    /// Returns the hydrated change action current land value value.
    public @Nullable BigDecimal getChangeActionCurrentLandValue() {
        return changeActionCurrentLandValue;
    }

    /// Supplies the change action current land value value during hydration or mapping.
    public void setChangeActionCurrentLandValue(@Nullable BigDecimal changeActionCurrentLandValue) {
        this.changeActionCurrentLandValue = changeActionCurrentLandValue;
    }

    /// Returns the hydrated change action current parcel count value.
    public @Nullable Long getChangeActionCurrentParcelCount() {
        return changeActionCurrentParcelCount;
    }

    /// Supplies the change action current parcel count value during hydration or mapping.
    public void setChangeActionCurrentParcelCount(@Nullable Long changeActionCurrentParcelCount) {
        this.changeActionCurrentParcelCount = changeActionCurrentParcelCount;
    }

    /// Returns the hydrated change action current total value value.
    public @Nullable BigDecimal getChangeActionCurrentTotalValue() {
        return changeActionCurrentTotalValue;
    }

    /// Supplies the change action current total value value during hydration or mapping.
    public void setChangeActionCurrentTotalValue(
            @Nullable BigDecimal changeActionCurrentTotalValue) {
        this.changeActionCurrentTotalValue = changeActionCurrentTotalValue;
    }

    /// Returns the hydrated change action prior improvement value value.
    public @Nullable BigDecimal getChangeActionPriorImprovementValue() {
        return changeActionPriorImprovementValue;
    }

    /// Supplies the change action prior improvement value value during hydration or mapping.
    public void setChangeActionPriorImprovementValue(
            @Nullable BigDecimal changeActionPriorImprovementValue) {
        this.changeActionPriorImprovementValue = changeActionPriorImprovementValue;
    }

    /// Returns the hydrated change action prior land value value.
    public @Nullable BigDecimal getChangeActionPriorLandValue() {
        return changeActionPriorLandValue;
    }

    /// Supplies the change action prior land value value during hydration or mapping.
    public void setChangeActionPriorLandValue(@Nullable BigDecimal changeActionPriorLandValue) {
        this.changeActionPriorLandValue = changeActionPriorLandValue;
    }

    /// Returns the hydrated change action prior parcel count value.
    public @Nullable Long getChangeActionPriorParcelCount() {
        return changeActionPriorParcelCount;
    }

    /// Supplies the change action prior parcel count value during hydration or mapping.
    public void setChangeActionPriorParcelCount(@Nullable Long changeActionPriorParcelCount) {
        this.changeActionPriorParcelCount = changeActionPriorParcelCount;
    }

    /// Returns the hydrated change action prior total value value.
    public @Nullable BigDecimal getChangeActionPriorTotalValue() {
        return changeActionPriorTotalValue;
    }

    /// Supplies the change action prior total value value during hydration or mapping.
    public void setChangeActionPriorTotalValue(@Nullable BigDecimal changeActionPriorTotalValue) {
        this.changeActionPriorTotalValue = changeActionPriorTotalValue;
    }

    /// Returns the hydrated current improvement value value.
    public @Nullable BigDecimal getCurrentImprovementValue() {
        return currentImprovementValue;
    }

    /// Supplies the current improvement value value during hydration or mapping.
    public void setCurrentImprovementValue(@Nullable BigDecimal currentImprovementValue) {
        this.currentImprovementValue = currentImprovementValue;
    }

    /// Returns the hydrated current land value value.
    public @Nullable BigDecimal getCurrentLandValue() {
        return currentLandValue;
    }

    /// Supplies the current land value value during hydration or mapping.
    public void setCurrentLandValue(@Nullable BigDecimal currentLandValue) {
        this.currentLandValue = currentLandValue;
    }

    /// Returns the hydrated current parcel count value.
    public @Nullable Long getCurrentParcelCount() {
        return currentParcelCount;
    }

    /// Supplies the current parcel count value during hydration or mapping.
    public void setCurrentParcelCount(@Nullable Long currentParcelCount) {
        this.currentParcelCount = currentParcelCount;
    }

    /// Returns the hydrated current total value value.
    public @Nullable BigDecimal getCurrentTotalValue() {
        return currentTotalValue;
    }

    /// Supplies the current total value value during hydration or mapping.
    public void setCurrentTotalValue(@Nullable BigDecimal currentTotalValue) {
        this.currentTotalValue = currentTotalValue;
    }

    /// Returns the hydrated division number value.
    public @Nullable String getDivisionNumber() {
        return divisionNumber;
    }

    /// Supplies the division number value during hydration or mapping.
    public void setDivisionNumber(@Nullable String divisionNumber) {
        this.divisionNumber = divisionNumber;
    }

    /// Returns the hydrated no change action current improvement value value.
    public @Nullable BigDecimal getNoChangeActionCurrentImprovementValue() {
        return noChangeActionCurrentImprovementValue;
    }

    /// Supplies the no change action current improvement value value during hydration or mapping.
    public void setNoChangeActionCurrentImprovementValue(
            BigDecimal noChangeActionCurrentImprovementValue) {
        this.noChangeActionCurrentImprovementValue = noChangeActionCurrentImprovementValue;
    }

    /// Returns the hydrated no change action current land value value.
    public @Nullable BigDecimal getNoChangeActionCurrentLandValue() {
        return noChangeActionCurrentLandValue;
    }

    /// Supplies the no change action current land value value during hydration or mapping.
    public void setNoChangeActionCurrentLandValue(
            @Nullable BigDecimal noChangeActionCurrentLandValue) {
        this.noChangeActionCurrentLandValue = noChangeActionCurrentLandValue;
    }

    /// Returns the hydrated no change action current parcel count value.
    public @Nullable Long getNoChangeActionCurrentParcelCount() {
        return noChangeActionCurrentParcelCount;
    }

    /// Supplies the no change action current parcel count value during hydration or mapping.
    public void setNoChangeActionCurrentParcelCount(
            @Nullable Long noChangeActionCurrentParcelCount) {
        this.noChangeActionCurrentParcelCount = noChangeActionCurrentParcelCount;
    }

    /// Returns the hydrated no change action current total value value.
    public @Nullable BigDecimal getNoChangeActionCurrentTotalValue() {
        return noChangeActionCurrentTotalValue;
    }

    /// Supplies the no change action current total value value during hydration or mapping.
    public void setNoChangeActionCurrentTotalValue(
            @Nullable BigDecimal noChangeActionCurrentTotalValue) {
        this.noChangeActionCurrentTotalValue = noChangeActionCurrentTotalValue;
    }

    /// Returns the hydrated no change action prior improvement value value.
    public @Nullable BigDecimal getNoChangeActionPriorImprovementValue() {
        return noChangeActionPriorImprovementValue;
    }

    /// Supplies the no change action prior improvement value value during hydration or mapping.
    public void setNoChangeActionPriorImprovementValue(
            @Nullable BigDecimal noChangeActionPriorImprovementValue) {
        this.noChangeActionPriorImprovementValue = noChangeActionPriorImprovementValue;
    }

    /// Returns the hydrated no change action prior land value value.
    public @Nullable BigDecimal getNoChangeActionPriorLandValue() {
        return noChangeActionPriorLandValue;
    }

    /// Supplies the no change action prior land value value during hydration or mapping.
    public void setNoChangeActionPriorLandValue(@Nullable BigDecimal noChangeActionPriorLandValue) {
        this.noChangeActionPriorLandValue = noChangeActionPriorLandValue;
    }

    /// Returns the hydrated no change action prior parcel count value.
    public @Nullable Long getNoChangeActionPriorParcelCount() {
        return noChangeActionPriorParcelCount;
    }

    /// Supplies the no change action prior parcel count value during hydration or mapping.
    public void setNoChangeActionPriorParcelCount(@Nullable Long noChangeActionPriorParcelCount) {
        this.noChangeActionPriorParcelCount = noChangeActionPriorParcelCount;
    }

    /// Returns the hydrated no change action prior total value value.
    public @Nullable BigDecimal getNoChangeActionPriorTotalValue() {
        return noChangeActionPriorTotalValue;
    }

    /// Supplies the no change action prior total value value during hydration or mapping.
    public void setNoChangeActionPriorTotalValue(
            @Nullable BigDecimal noChangeActionPriorTotalValue) {
        this.noChangeActionPriorTotalValue = noChangeActionPriorTotalValue;
    }

    /// Returns the hydrated prior improvement value value.
    public @Nullable BigDecimal getPriorImprovementValue() {
        return priorImprovementValue;
    }

    /// Supplies the prior improvement value value during hydration or mapping.
    public void setPriorImprovementValue(@Nullable BigDecimal priorImprovementValue) {
        this.priorImprovementValue = priorImprovementValue;
    }

    /// Returns the hydrated prior land value value.
    public @Nullable BigDecimal getPriorLandValue() {
        return priorLandValue;
    }

    /// Supplies the prior land value value during hydration or mapping.
    public void setPriorLandValue(@Nullable BigDecimal priorLandValue) {
        this.priorLandValue = priorLandValue;
    }

    /// Returns the hydrated prior parcel count value.
    public @Nullable Long getPriorParcelCount() {
        return priorParcelCount;
    }

    /// Supplies the prior parcel count value during hydration or mapping.
    public void setPriorParcelCount(@Nullable Long priorParcelCount) {
        this.priorParcelCount = priorParcelCount;
    }

    /// Returns the hydrated prior total value value.
    public @Nullable BigDecimal getPriorTotalValue() {
        return priorTotalValue;
    }

    /// Supplies the prior total value value during hydration or mapping.
    public void setPriorTotalValue(@Nullable BigDecimal priorTotalValue) {
        this.priorTotalValue = priorTotalValue;
    }

    /// Returns the hydrated proposed actual value value.
    public @Nullable BigDecimal getProposedActualValue() {
        return proposedActualValue;
    }

    /// Supplies the proposed actual value value during hydration or mapping.
    public void setProposedActualValue(@Nullable BigDecimal proposedActualValue) {
        this.proposedActualValue = proposedActualValue;
    }

    /// Returns the hydrated proposed current288 value value.
    public @Nullable BigDecimal getProposedCurrent288Value() {
        return proposedCurrent288Value;
    }

    /// Supplies the proposed current288 value value during hydration or mapping.
    public void setProposedCurrent288Value(@Nullable BigDecimal proposedCurrent288Value) {
        this.proposedCurrent288Value = proposedCurrent288Value;
    }

    /// Returns the hydrated proposed expired288 value value.
    public @Nullable BigDecimal getProposedExpired288Value() {
        return proposedExpired288Value;
    }

    /// Supplies the proposed expired288 value value during hydration or mapping.
    public void setProposedExpired288Value(@Nullable BigDecimal proposedExpired288Value) {
        this.proposedExpired288Value = proposedExpired288Value;
    }

    /// Returns the hydrated proposed improvement value value.
    public @Nullable BigDecimal getProposedImprovementValue() {
        return proposedImprovementValue;
    }

    /// Supplies the proposed improvement value value during hydration or mapping.
    public void setProposedImprovementValue(@Nullable BigDecimal proposedImprovementValue) {
        this.proposedImprovementValue = proposedImprovementValue;
    }

    /// Returns the hydrated proposed total value value.
    public @Nullable BigDecimal getProposedTotalValue() {
        return proposedTotalValue;
    }

    /// Supplies the proposed total value value during hydration or mapping.
    public void setProposedTotalValue(@Nullable BigDecimal proposedTotalValue) {
        this.proposedTotalValue = proposedTotalValue;
    }

    // GENERATED-ACCESSORS:end
}
