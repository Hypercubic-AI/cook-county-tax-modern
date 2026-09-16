
package org.cookcounty.tax.domain.model;


import java.math.BigDecimal;


public class FrozenAgencyAdjustment {

    private Long id;

    // GENERATED-FIELDS:start

    private String agencyNumber;

    private Long annexedAssessedValue;

    private Long annexedEqualizedValue;

    private Long current288Value;

    private Long disconnectedAssessedValue;

    private Long disconnectedEqualizedValue;

    private Long expired288Value;

    private Long expiredIncentiveEqualizedValue;

    private BigDecimal expiredIncentiveTaxAmount;

    private Long expiredIncentiveValue;

    private Long firstTimeValue;

    private Long frozenEqualizedValue;

    private BigDecimal frozenTaxAmount;

    private String taxCode;

    private BigDecimal taxRate;

    private Long tifCurrentEqualizedValue;

    private Long tifDifferenceEqualizedValue;

    private Long tifPriorFrozenEqualizedValue;

    private Long totalFrozenValue;



    // GENERATED-FIELDS:end

    public FrozenAgencyAdjustment() {}

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
