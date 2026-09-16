
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

@Entity
@Table(name = "agency_equalized_valuations")
public class AgencyEqualizedValuationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "agency_number", unique = true)
    private String agencyNumber;

    @Column(name = "annexed_property_equalized_value")
    private Long annexedPropertyEqualizedValue;

    @Column(name = "burden_percent")
    private BigDecimal burdenPercent;

    @Column(name = "connecting_agency1")
    private String connectingAgency1;

    @Column(name = "connecting_agency2")
    private String connectingAgency2;

    @Column(name = "connecting_agency3")
    private String connectingAgency3;

    @Column(name = "connecting_agency4")
    private String connectingAgency4;

    @Column(name = "cook_county_air_pollution_value")
    private Long cookCountyAirPollutionValue;

    @Column(name = "cook_county_railroad_value")
    private Long cookCountyRailroadValue;

    @Column(name = "cook_county_real_estate_value")
    private Long cookCountyRealEstateValue;

    @Column(name = "cook_county_use_tax_value")
    private Long cookCountyUseTaxValue;

    @Column(name = "de_kalb_county_equalized_value")
    private Long deKalbCountyEqualizedValue;

    @Column(name = "disconnected_property_equalized_value")
    private Long disconnectedPropertyEqualizedValue;

    @Column(name = "disconnected_tif_difference")
    private Long disconnectedTifDifference;

    @Column(name = "du_page_county_equalized_value")
    private Long duPageCountyEqualizedValue;

    @Column(name = "grundy_county_equalized_value")
    private Long grundyCountyEqualizedValue;

    @Column(name = "kane_county_equalized_value")
    private Long kaneCountyEqualizedValue;

    @Column(name = "kankakee_county_equalized_value")
    private Long kankakeeCountyEqualizedValue;

    @Column(name = "kendall_county_equalized_value")
    private Long kendallCountyEqualizedValue;

    @Column(name = "la_salle_county_equalized_value")
    private Long laSalleCountyEqualizedValue;

    @Column(name = "lake_county_equalized_value")
    private Long lakeCountyEqualizedValue;

    @Column(name = "limiting_tax_rate_override")
    private BigDecimal limitingTaxRateOverride;

    @Column(name = "livingston_county_equalized_value")
    private Long livingstonCountyEqualizedValue;

    @Column(name = "mc_henry_county_equalized_value")
    private Long mcHenryCountyEqualizedValue;

    @Column(name = "new_property_equalized_value")
    private Long newPropertyEqualizedValue;

    @Column(name = "overlap_annexed_property_equalized_value")
    private Long overlapAnnexedPropertyEqualizedValue;

    @Column(name = "overlap_disconnected_property_equalized_value")
    private Long overlapDisconnectedPropertyEqualizedValue;

    @Column(name = "overlap_disconnected_tif_difference")
    private Long overlapDisconnectedTifDifference;

    @Column(name = "overlap_new_property_equalized_value")
    private Long overlapNewPropertyEqualizedValue;

    @Column(name = "parent_agency1")
    private String parentAgency1;

    @Column(name = "parent_agency2")
    private String parentAgency2;

    @Column(name = "parent_agency3")
    private String parentAgency3;

    @Column(name = "parent_agency4")
    private String parentAgency4;

    @Column(name = "parent_agency5")
    private String parentAgency5;

    @Column(name = "previous_tax_year1")
    private Integer previousTaxYear1;

    @Column(name = "previous_tax_year1_extension")
    private BigDecimal previousTaxYear1Extension;

    @Column(name = "previous_tax_year2")
    private Integer previousTaxYear2;

    @Column(name = "previous_tax_year2_extension")
    private BigDecimal previousTaxYear2Extension;

    @Column(name = "previous_tax_year3")
    private Integer previousTaxYear3;

    @Column(name = "previous_tax_year3_extension")
    private BigDecimal previousTaxYear3Extension;

    @Column(name = "tax_cap_indicator")
    private Boolean taxCapIndicator;

    @Column(name = "tax_year")
    private Integer taxYear;

    @Column(name = "will_county_equalized_value")
    private Long willCountyEqualizedValue;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public String getAgencyNumber() { return agencyNumber; }
    public void setAgencyNumber(String agencyNumber) { this.agencyNumber = agencyNumber; }


    public Long getAnnexedPropertyEqualizedValue() { return annexedPropertyEqualizedValue; }
    public void setAnnexedPropertyEqualizedValue(Long annexedPropertyEqualizedValue) { this.annexedPropertyEqualizedValue = annexedPropertyEqualizedValue; }


    public BigDecimal getBurdenPercent() { return burdenPercent; }
    public void setBurdenPercent(BigDecimal burdenPercent) { this.burdenPercent = burdenPercent; }


    public String getConnectingAgency1() { return connectingAgency1; }
    public void setConnectingAgency1(String connectingAgency1) { this.connectingAgency1 = connectingAgency1; }


    public String getConnectingAgency2() { return connectingAgency2; }
    public void setConnectingAgency2(String connectingAgency2) { this.connectingAgency2 = connectingAgency2; }


    public String getConnectingAgency3() { return connectingAgency3; }
    public void setConnectingAgency3(String connectingAgency3) { this.connectingAgency3 = connectingAgency3; }


    public String getConnectingAgency4() { return connectingAgency4; }
    public void setConnectingAgency4(String connectingAgency4) { this.connectingAgency4 = connectingAgency4; }


    public Long getCookCountyAirPollutionValue() { return cookCountyAirPollutionValue; }
    public void setCookCountyAirPollutionValue(Long cookCountyAirPollutionValue) { this.cookCountyAirPollutionValue = cookCountyAirPollutionValue; }


    public Long getCookCountyRailroadValue() { return cookCountyRailroadValue; }
    public void setCookCountyRailroadValue(Long cookCountyRailroadValue) { this.cookCountyRailroadValue = cookCountyRailroadValue; }


    public Long getCookCountyRealEstateValue() { return cookCountyRealEstateValue; }
    public void setCookCountyRealEstateValue(Long cookCountyRealEstateValue) { this.cookCountyRealEstateValue = cookCountyRealEstateValue; }


    public Long getCookCountyUseTaxValue() { return cookCountyUseTaxValue; }
    public void setCookCountyUseTaxValue(Long cookCountyUseTaxValue) { this.cookCountyUseTaxValue = cookCountyUseTaxValue; }


    public Long getDeKalbCountyEqualizedValue() { return deKalbCountyEqualizedValue; }
    public void setDeKalbCountyEqualizedValue(Long deKalbCountyEqualizedValue) { this.deKalbCountyEqualizedValue = deKalbCountyEqualizedValue; }


    public Long getDisconnectedPropertyEqualizedValue() { return disconnectedPropertyEqualizedValue; }
    public void setDisconnectedPropertyEqualizedValue(Long disconnectedPropertyEqualizedValue) { this.disconnectedPropertyEqualizedValue = disconnectedPropertyEqualizedValue; }


    public Long getDisconnectedTifDifference() { return disconnectedTifDifference; }
    public void setDisconnectedTifDifference(Long disconnectedTifDifference) { this.disconnectedTifDifference = disconnectedTifDifference; }


    public Long getDuPageCountyEqualizedValue() { return duPageCountyEqualizedValue; }
    public void setDuPageCountyEqualizedValue(Long duPageCountyEqualizedValue) { this.duPageCountyEqualizedValue = duPageCountyEqualizedValue; }


    public Long getGrundyCountyEqualizedValue() { return grundyCountyEqualizedValue; }
    public void setGrundyCountyEqualizedValue(Long grundyCountyEqualizedValue) { this.grundyCountyEqualizedValue = grundyCountyEqualizedValue; }


    public Long getKaneCountyEqualizedValue() { return kaneCountyEqualizedValue; }
    public void setKaneCountyEqualizedValue(Long kaneCountyEqualizedValue) { this.kaneCountyEqualizedValue = kaneCountyEqualizedValue; }


    public Long getKankakeeCountyEqualizedValue() { return kankakeeCountyEqualizedValue; }
    public void setKankakeeCountyEqualizedValue(Long kankakeeCountyEqualizedValue) { this.kankakeeCountyEqualizedValue = kankakeeCountyEqualizedValue; }


    public Long getKendallCountyEqualizedValue() { return kendallCountyEqualizedValue; }
    public void setKendallCountyEqualizedValue(Long kendallCountyEqualizedValue) { this.kendallCountyEqualizedValue = kendallCountyEqualizedValue; }


    public Long getLaSalleCountyEqualizedValue() { return laSalleCountyEqualizedValue; }
    public void setLaSalleCountyEqualizedValue(Long laSalleCountyEqualizedValue) { this.laSalleCountyEqualizedValue = laSalleCountyEqualizedValue; }


    public Long getLakeCountyEqualizedValue() { return lakeCountyEqualizedValue; }
    public void setLakeCountyEqualizedValue(Long lakeCountyEqualizedValue) { this.lakeCountyEqualizedValue = lakeCountyEqualizedValue; }


    public BigDecimal getLimitingTaxRateOverride() { return limitingTaxRateOverride; }
    public void setLimitingTaxRateOverride(BigDecimal limitingTaxRateOverride) { this.limitingTaxRateOverride = limitingTaxRateOverride; }


    public Long getLivingstonCountyEqualizedValue() { return livingstonCountyEqualizedValue; }
    public void setLivingstonCountyEqualizedValue(Long livingstonCountyEqualizedValue) { this.livingstonCountyEqualizedValue = livingstonCountyEqualizedValue; }


    public Long getMcHenryCountyEqualizedValue() { return mcHenryCountyEqualizedValue; }
    public void setMcHenryCountyEqualizedValue(Long mcHenryCountyEqualizedValue) { this.mcHenryCountyEqualizedValue = mcHenryCountyEqualizedValue; }


    public Long getNewPropertyEqualizedValue() { return newPropertyEqualizedValue; }
    public void setNewPropertyEqualizedValue(Long newPropertyEqualizedValue) { this.newPropertyEqualizedValue = newPropertyEqualizedValue; }


    public Long getOverlapAnnexedPropertyEqualizedValue() { return overlapAnnexedPropertyEqualizedValue; }
    public void setOverlapAnnexedPropertyEqualizedValue(Long overlapAnnexedPropertyEqualizedValue) { this.overlapAnnexedPropertyEqualizedValue = overlapAnnexedPropertyEqualizedValue; }


    public Long getOverlapDisconnectedPropertyEqualizedValue() { return overlapDisconnectedPropertyEqualizedValue; }
    public void setOverlapDisconnectedPropertyEqualizedValue(Long overlapDisconnectedPropertyEqualizedValue) { this.overlapDisconnectedPropertyEqualizedValue = overlapDisconnectedPropertyEqualizedValue; }


    public Long getOverlapDisconnectedTifDifference() { return overlapDisconnectedTifDifference; }
    public void setOverlapDisconnectedTifDifference(Long overlapDisconnectedTifDifference) { this.overlapDisconnectedTifDifference = overlapDisconnectedTifDifference; }


    public Long getOverlapNewPropertyEqualizedValue() { return overlapNewPropertyEqualizedValue; }
    public void setOverlapNewPropertyEqualizedValue(Long overlapNewPropertyEqualizedValue) { this.overlapNewPropertyEqualizedValue = overlapNewPropertyEqualizedValue; }


    public String getParentAgency1() { return parentAgency1; }
    public void setParentAgency1(String parentAgency1) { this.parentAgency1 = parentAgency1; }


    public String getParentAgency2() { return parentAgency2; }
    public void setParentAgency2(String parentAgency2) { this.parentAgency2 = parentAgency2; }


    public String getParentAgency3() { return parentAgency3; }
    public void setParentAgency3(String parentAgency3) { this.parentAgency3 = parentAgency3; }


    public String getParentAgency4() { return parentAgency4; }
    public void setParentAgency4(String parentAgency4) { this.parentAgency4 = parentAgency4; }


    public String getParentAgency5() { return parentAgency5; }
    public void setParentAgency5(String parentAgency5) { this.parentAgency5 = parentAgency5; }


    public Integer getPreviousTaxYear1() { return previousTaxYear1; }
    public void setPreviousTaxYear1(Integer previousTaxYear1) { this.previousTaxYear1 = previousTaxYear1; }


    public BigDecimal getPreviousTaxYear1Extension() { return previousTaxYear1Extension; }
    public void setPreviousTaxYear1Extension(BigDecimal previousTaxYear1Extension) { this.previousTaxYear1Extension = previousTaxYear1Extension; }


    public Integer getPreviousTaxYear2() { return previousTaxYear2; }
    public void setPreviousTaxYear2(Integer previousTaxYear2) { this.previousTaxYear2 = previousTaxYear2; }


    public BigDecimal getPreviousTaxYear2Extension() { return previousTaxYear2Extension; }
    public void setPreviousTaxYear2Extension(BigDecimal previousTaxYear2Extension) { this.previousTaxYear2Extension = previousTaxYear2Extension; }


    public Integer getPreviousTaxYear3() { return previousTaxYear3; }
    public void setPreviousTaxYear3(Integer previousTaxYear3) { this.previousTaxYear3 = previousTaxYear3; }


    public BigDecimal getPreviousTaxYear3Extension() { return previousTaxYear3Extension; }
    public void setPreviousTaxYear3Extension(BigDecimal previousTaxYear3Extension) { this.previousTaxYear3Extension = previousTaxYear3Extension; }


    public Boolean getTaxCapIndicator() { return taxCapIndicator; }
    public void setTaxCapIndicator(Boolean taxCapIndicator) { this.taxCapIndicator = taxCapIndicator; }


    public Integer getTaxYear() { return taxYear; }
    public void setTaxYear(Integer taxYear) { this.taxYear = taxYear; }


    public Long getWillCountyEqualizedValue() { return willCountyEqualizedValue; }
    public void setWillCountyEqualizedValue(Long willCountyEqualizedValue) { this.willCountyEqualizedValue = willCountyEqualizedValue; }



    // GENERATED-ACCESSORS:end
}
