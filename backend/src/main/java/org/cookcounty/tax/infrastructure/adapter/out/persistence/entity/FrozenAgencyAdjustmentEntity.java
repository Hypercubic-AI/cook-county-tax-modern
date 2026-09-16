package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.*;

// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

/// Mutable JPA boundary for frozen agency adjustment persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(
        name = "frozen_agency_adjustments",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_frozen_agency_tax_code_agency",
                        columnNames = {"tax_code", "agency_number"}))
public class FrozenAgencyAdjustmentEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public FrozenAgencyAdjustmentEntity() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Version private @Nullable Long version;

    // GENERATED-FIELDS:start

    @Column(name = "agency_number", length = 9)
    private @Nullable String agencyNumber;

    @Column(name = "annexed_assessed_value", precision = 13, scale = 0)
    private @Nullable BigDecimal annexedAssessedValue;

    @Column(name = "annexed_equalized_value", precision = 13, scale = 0)
    private @Nullable BigDecimal annexedEqualizedValue;

    @Column(name = "current288_value", precision = 13, scale = 0)
    private @Nullable BigDecimal current288Value;

    @Column(name = "disconnected_assessed_value", precision = 13, scale = 0)
    private @Nullable BigDecimal disconnectedAssessedValue;

    @Column(name = "disconnected_equalized_value", precision = 13, scale = 0)
    private @Nullable BigDecimal disconnectedEqualizedValue;

    @Column(name = "expired288_value", precision = 13, scale = 0)
    private @Nullable BigDecimal expired288Value;

    @Column(name = "expired_incentive_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal expiredIncentiveEqualizedValue;

    @Column(name = "expired_incentive_tax_amount", precision = 11, scale = 2)
    private @Nullable BigDecimal expiredIncentiveTaxAmount;

    @Column(name = "expired_incentive_value", precision = 11, scale = 0)
    private @Nullable BigDecimal expiredIncentiveValue;

    @Column(name = "first_time_value", precision = 13, scale = 0)
    private @Nullable BigDecimal firstTimeValue;

    @Column(name = "frozen_equalized_value", precision = 13, scale = 0)
    private @Nullable BigDecimal frozenEqualizedValue;

    @Column(name = "frozen_tax_amount", precision = 15, scale = 2)
    private @Nullable BigDecimal frozenTaxAmount;

    @Column(name = "tax_code", length = 5)
    private @Nullable String taxCode;

    @Column(name = "tax_rate", precision = 6, scale = 3)
    private @Nullable BigDecimal taxRate;

    @Column(name = "tif_current_equalized_value", precision = 13, scale = 0)
    private @Nullable BigDecimal tifCurrentEqualizedValue;

    @Column(name = "tif_difference_equalized_value", precision = 13, scale = 0)
    private @Nullable BigDecimal tifDifferenceEqualizedValue;

    @Column(name = "tif_prior_frozen_equalized_value", precision = 13, scale = 0)
    private @Nullable BigDecimal tifPriorFrozenEqualizedValue;

    @Column(name = "total_frozen_value", precision = 13, scale = 0)
    private @Nullable BigDecimal totalFrozenValue;

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

    /// Returns the hydrated agency number value.
    public @Nullable String getAgencyNumber() {
        return agencyNumber;
    }

    /// Supplies the agency number value during hydration or mapping.
    public void setAgencyNumber(@Nullable String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    /// Returns the hydrated annexed assessed value value.
    public @Nullable BigDecimal getAnnexedAssessedValue() {
        return annexedAssessedValue;
    }

    /// Supplies the annexed assessed value value during hydration or mapping.
    public void setAnnexedAssessedValue(@Nullable BigDecimal annexedAssessedValue) {
        this.annexedAssessedValue = annexedAssessedValue;
    }

    /// Returns the hydrated annexed equalized value value.
    public @Nullable BigDecimal getAnnexedEqualizedValue() {
        return annexedEqualizedValue;
    }

    /// Supplies the annexed equalized value value during hydration or mapping.
    public void setAnnexedEqualizedValue(@Nullable BigDecimal annexedEqualizedValue) {
        this.annexedEqualizedValue = annexedEqualizedValue;
    }

    /// Returns the hydrated current288 value value.
    public @Nullable BigDecimal getCurrent288Value() {
        return current288Value;
    }

    /// Supplies the current288 value value during hydration or mapping.
    public void setCurrent288Value(@Nullable BigDecimal current288Value) {
        this.current288Value = current288Value;
    }

    /// Returns the hydrated disconnected assessed value value.
    public @Nullable BigDecimal getDisconnectedAssessedValue() {
        return disconnectedAssessedValue;
    }

    /// Supplies the disconnected assessed value value during hydration or mapping.
    public void setDisconnectedAssessedValue(@Nullable BigDecimal disconnectedAssessedValue) {
        this.disconnectedAssessedValue = disconnectedAssessedValue;
    }

    /// Returns the hydrated disconnected equalized value value.
    public @Nullable BigDecimal getDisconnectedEqualizedValue() {
        return disconnectedEqualizedValue;
    }

    /// Supplies the disconnected equalized value value during hydration or mapping.
    public void setDisconnectedEqualizedValue(@Nullable BigDecimal disconnectedEqualizedValue) {
        this.disconnectedEqualizedValue = disconnectedEqualizedValue;
    }

    /// Returns the hydrated expired288 value value.
    public @Nullable BigDecimal getExpired288Value() {
        return expired288Value;
    }

    /// Supplies the expired288 value value during hydration or mapping.
    public void setExpired288Value(@Nullable BigDecimal expired288Value) {
        this.expired288Value = expired288Value;
    }

    /// Returns the hydrated expired incentive equalized value value.
    public @Nullable BigDecimal getExpiredIncentiveEqualizedValue() {
        return expiredIncentiveEqualizedValue;
    }

    /// Supplies the expired incentive equalized value value during hydration or mapping.
    public void setExpiredIncentiveEqualizedValue(
            @Nullable BigDecimal expiredIncentiveEqualizedValue) {
        this.expiredIncentiveEqualizedValue = expiredIncentiveEqualizedValue;
    }

    /// Returns the hydrated expired incentive tax amount value.
    public @Nullable BigDecimal getExpiredIncentiveTaxAmount() {
        return expiredIncentiveTaxAmount;
    }

    /// Supplies the expired incentive tax amount value during hydration or mapping.
    public void setExpiredIncentiveTaxAmount(@Nullable BigDecimal expiredIncentiveTaxAmount) {
        this.expiredIncentiveTaxAmount = expiredIncentiveTaxAmount;
    }

    /// Returns the hydrated expired incentive value value.
    public @Nullable BigDecimal getExpiredIncentiveValue() {
        return expiredIncentiveValue;
    }

    /// Supplies the expired incentive value value during hydration or mapping.
    public void setExpiredIncentiveValue(@Nullable BigDecimal expiredIncentiveValue) {
        this.expiredIncentiveValue = expiredIncentiveValue;
    }

    /// Returns the hydrated first time value value.
    public @Nullable BigDecimal getFirstTimeValue() {
        return firstTimeValue;
    }

    /// Supplies the first time value value during hydration or mapping.
    public void setFirstTimeValue(@Nullable BigDecimal firstTimeValue) {
        this.firstTimeValue = firstTimeValue;
    }

    /// Returns the hydrated frozen equalized value value.
    public @Nullable BigDecimal getFrozenEqualizedValue() {
        return frozenEqualizedValue;
    }

    /// Supplies the frozen equalized value value during hydration or mapping.
    public void setFrozenEqualizedValue(@Nullable BigDecimal frozenEqualizedValue) {
        this.frozenEqualizedValue = frozenEqualizedValue;
    }

    /// Returns the hydrated frozen tax amount value.
    public @Nullable BigDecimal getFrozenTaxAmount() {
        return frozenTaxAmount;
    }

    /// Supplies the frozen tax amount value during hydration or mapping.
    public void setFrozenTaxAmount(@Nullable BigDecimal frozenTaxAmount) {
        this.frozenTaxAmount = frozenTaxAmount;
    }

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

    /// Returns the hydrated tif current equalized value value.
    public @Nullable BigDecimal getTifCurrentEqualizedValue() {
        return tifCurrentEqualizedValue;
    }

    /// Supplies the tif current equalized value value during hydration or mapping.
    public void setTifCurrentEqualizedValue(@Nullable BigDecimal tifCurrentEqualizedValue) {
        this.tifCurrentEqualizedValue = tifCurrentEqualizedValue;
    }

    /// Returns the hydrated tif difference equalized value value.
    public @Nullable BigDecimal getTifDifferenceEqualizedValue() {
        return tifDifferenceEqualizedValue;
    }

    /// Supplies the tif difference equalized value value during hydration or mapping.
    public void setTifDifferenceEqualizedValue(@Nullable BigDecimal tifDifferenceEqualizedValue) {
        this.tifDifferenceEqualizedValue = tifDifferenceEqualizedValue;
    }

    /// Returns the hydrated tif prior frozen equalized value value.
    public @Nullable BigDecimal getTifPriorFrozenEqualizedValue() {
        return tifPriorFrozenEqualizedValue;
    }

    /// Supplies the tif prior frozen equalized value value during hydration or mapping.
    public void setTifPriorFrozenEqualizedValue(@Nullable BigDecimal tifPriorFrozenEqualizedValue) {
        this.tifPriorFrozenEqualizedValue = tifPriorFrozenEqualizedValue;
    }

    /// Returns the hydrated total frozen value value.
    public @Nullable BigDecimal getTotalFrozenValue() {
        return totalFrozenValue;
    }

    /// Supplies the total frozen value value during hydration or mapping.
    public void setTotalFrozenValue(@Nullable BigDecimal totalFrozenValue) {
        this.totalFrozenValue = totalFrozenValue;
    }

    // GENERATED-ACCESSORS:end
}
