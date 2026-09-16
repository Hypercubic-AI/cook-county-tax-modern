
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

@Entity
@Table(
        name = "frozen_agency_adjustments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_frozen_agency_tax_code_agency",
                columnNames = {"tax_code", "agency_number"}))
public class FrozenAgencyAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "agency_number")
    private String agencyNumber;

    @Column(name = "annexed_assessed_value")
    private Long annexedAssessedValue;

    @Column(name = "annexed_equalized_value")
    private Long annexedEqualizedValue;

    @Column(name = "current288_value")
    private Long current288Value;

    @Column(name = "disconnected_assessed_value")
    private Long disconnectedAssessedValue;

    @Column(name = "disconnected_equalized_value")
    private Long disconnectedEqualizedValue;

    @Column(name = "expired288_value")
    private Long expired288Value;

    @Column(name = "expired_incentive_equalized_value")
    private Long expiredIncentiveEqualizedValue;

    @Column(name = "expired_incentive_tax_amount")
    private BigDecimal expiredIncentiveTaxAmount;

    @Column(name = "expired_incentive_value")
    private Long expiredIncentiveValue;

    @Column(name = "first_time_value")
    private Long firstTimeValue;

    @Column(name = "frozen_equalized_value")
    private Long frozenEqualizedValue;

    @Column(name = "frozen_tax_amount")
    private BigDecimal frozenTaxAmount;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "tax_rate")
    private BigDecimal taxRate;

    @Column(name = "tif_current_equalized_value")
    private Long tifCurrentEqualizedValue;

    @Column(name = "tif_difference_equalized_value")
    private Long tifDifferenceEqualizedValue;

    @Column(name = "tif_prior_frozen_equalized_value")
    private Long tifPriorFrozenEqualizedValue;

    @Column(name = "total_frozen_value")
    private Long totalFrozenValue;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public String getAgencyNumber() { return agencyNumber; }
    public void setAgencyNumber(String agencyNumber) { this.agencyNumber = agencyNumber; }


    public Long getAnnexedAssessedValue() { return annexedAssessedValue; }
    public void setAnnexedAssessedValue(Long annexedAssessedValue) { this.annexedAssessedValue = annexedAssessedValue; }


    public Long getAnnexedEqualizedValue() { return annexedEqualizedValue; }
    public void setAnnexedEqualizedValue(Long annexedEqualizedValue) { this.annexedEqualizedValue = annexedEqualizedValue; }


    public Long getCurrent288Value() { return current288Value; }
    public void setCurrent288Value(Long current288Value) { this.current288Value = current288Value; }


    public Long getDisconnectedAssessedValue() { return disconnectedAssessedValue; }
    public void setDisconnectedAssessedValue(Long disconnectedAssessedValue) { this.disconnectedAssessedValue = disconnectedAssessedValue; }


    public Long getDisconnectedEqualizedValue() { return disconnectedEqualizedValue; }
    public void setDisconnectedEqualizedValue(Long disconnectedEqualizedValue) { this.disconnectedEqualizedValue = disconnectedEqualizedValue; }


    public Long getExpired288Value() { return expired288Value; }
    public void setExpired288Value(Long expired288Value) { this.expired288Value = expired288Value; }


    public Long getExpiredIncentiveEqualizedValue() { return expiredIncentiveEqualizedValue; }
    public void setExpiredIncentiveEqualizedValue(Long expiredIncentiveEqualizedValue) { this.expiredIncentiveEqualizedValue = expiredIncentiveEqualizedValue; }


    public BigDecimal getExpiredIncentiveTaxAmount() { return expiredIncentiveTaxAmount; }
    public void setExpiredIncentiveTaxAmount(BigDecimal expiredIncentiveTaxAmount) { this.expiredIncentiveTaxAmount = expiredIncentiveTaxAmount; }


    public Long getExpiredIncentiveValue() { return expiredIncentiveValue; }
    public void setExpiredIncentiveValue(Long expiredIncentiveValue) { this.expiredIncentiveValue = expiredIncentiveValue; }


    public Long getFirstTimeValue() { return firstTimeValue; }
    public void setFirstTimeValue(Long firstTimeValue) { this.firstTimeValue = firstTimeValue; }


    public Long getFrozenEqualizedValue() { return frozenEqualizedValue; }
    public void setFrozenEqualizedValue(Long frozenEqualizedValue) { this.frozenEqualizedValue = frozenEqualizedValue; }


    public BigDecimal getFrozenTaxAmount() { return frozenTaxAmount; }
    public void setFrozenTaxAmount(BigDecimal frozenTaxAmount) { this.frozenTaxAmount = frozenTaxAmount; }


    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }


    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }


    public Long getTifCurrentEqualizedValue() { return tifCurrentEqualizedValue; }
    public void setTifCurrentEqualizedValue(Long tifCurrentEqualizedValue) { this.tifCurrentEqualizedValue = tifCurrentEqualizedValue; }


    public Long getTifDifferenceEqualizedValue() { return tifDifferenceEqualizedValue; }
    public void setTifDifferenceEqualizedValue(Long tifDifferenceEqualizedValue) { this.tifDifferenceEqualizedValue = tifDifferenceEqualizedValue; }


    public Long getTifPriorFrozenEqualizedValue() { return tifPriorFrozenEqualizedValue; }
    public void setTifPriorFrozenEqualizedValue(Long tifPriorFrozenEqualizedValue) { this.tifPriorFrozenEqualizedValue = tifPriorFrozenEqualizedValue; }


    public Long getTotalFrozenValue() { return totalFrozenValue; }
    public void setTotalFrozenValue(Long totalFrozenValue) { this.totalFrozenValue = totalFrozenValue; }



    // GENERATED-ACCESSORS:end
}
