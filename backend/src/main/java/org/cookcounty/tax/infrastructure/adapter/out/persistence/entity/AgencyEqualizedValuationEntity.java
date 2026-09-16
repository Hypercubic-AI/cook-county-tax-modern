package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.*;

// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

/// Mutable JPA boundary for agency equalized valuation persistence.
///
/// Fields are nullable only while JPA constructs or hydrates this entity. Canonical mapping
/// requires a complete domain snapshot before the value leaves the persistence boundary.
@Entity
@Table(name = "agency_equalized_valuations")
public class AgencyEqualizedValuationEntity {

    /// Creates an empty instance for JPA hydration or canonical domain mapping.
    public AgencyEqualizedValuationEntity() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Version private @Nullable Long version;

    // GENERATED-FIELDS:start

    @Column(name = "agency_number", length = 9)
    private @Nullable String agencyNumber;

    @Column(name = "annexed_property_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal annexedPropertyEqualizedValue;

    @Column(name = "burden_percent", precision = 5, scale = 2)
    private @Nullable BigDecimal burdenPercent;

    @Column(name = "connecting_agency1", length = 9)
    private @Nullable String connectingAgency1;

    @Column(name = "connecting_agency2", length = 9)
    private @Nullable String connectingAgency2;

    @Column(name = "connecting_agency3", length = 9)
    private @Nullable String connectingAgency3;

    @Column(name = "connecting_agency4", length = 9)
    private @Nullable String connectingAgency4;

    @Column(name = "cook_county_air_pollution_value", precision = 11, scale = 0)
    private @Nullable BigDecimal cookCountyAirPollutionValue;

    @Column(name = "cook_county_railroad_value", precision = 11, scale = 0)
    private @Nullable BigDecimal cookCountyRailroadValue;

    @Column(name = "cook_county_real_estate_value", precision = 13, scale = 0)
    private @Nullable BigDecimal cookCountyRealEstateValue;

    @Column(name = "cook_county_use_tax_value", precision = 11, scale = 0)
    private @Nullable BigDecimal cookCountyUseTaxValue;

    @Column(name = "de_kalb_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal deKalbCountyEqualizedValue;

    @Column(name = "disconnected_property_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal disconnectedPropertyEqualizedValue;

    @Column(name = "disconnected_tif_difference", precision = 11, scale = 0)
    private @Nullable BigDecimal disconnectedTifDifference;

    @Column(name = "du_page_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal duPageCountyEqualizedValue;

    @Column(name = "grundy_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal grundyCountyEqualizedValue;

    @Column(name = "kane_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal kaneCountyEqualizedValue;

    @Column(name = "kankakee_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal kankakeeCountyEqualizedValue;

    @Column(name = "kendall_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal kendallCountyEqualizedValue;

    @Column(name = "la_salle_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal laSalleCountyEqualizedValue;

    @Column(name = "lake_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal lakeCountyEqualizedValue;

    @Column(name = "limiting_tax_rate_override", precision = 9, scale = 6)
    private @Nullable BigDecimal limitingTaxRateOverride;

    @Column(name = "livingston_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal livingstonCountyEqualizedValue;

    @Column(name = "mc_henry_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal mcHenryCountyEqualizedValue;

    @Column(name = "new_property_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal newPropertyEqualizedValue;

    @Column(name = "overlap_annexed_property_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal overlapAnnexedPropertyEqualizedValue;

    @Column(name = "overlap_disconnected_property_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal overlapDisconnectedPropertyEqualizedValue;

    @Column(name = "overlap_disconnected_tif_difference", precision = 11, scale = 0)
    private @Nullable BigDecimal overlapDisconnectedTifDifference;

    @Column(name = "overlap_new_property_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal overlapNewPropertyEqualizedValue;

    @Column(name = "parent_agency1", length = 9)
    private @Nullable String parentAgency1;

    @Column(name = "parent_agency2", length = 9)
    private @Nullable String parentAgency2;

    @Column(name = "parent_agency3", length = 9)
    private @Nullable String parentAgency3;

    @Column(name = "parent_agency4", length = 9)
    private @Nullable String parentAgency4;

    @Column(name = "parent_agency5", length = 9)
    private @Nullable String parentAgency5;

    @Column(name = "previous_tax_year1")
    private @Nullable Integer previousTaxYear1;

    @Column(name = "previous_tax_year1_extension", precision = 13, scale = 2)
    private @Nullable BigDecimal previousTaxYear1Extension;

    @Column(name = "previous_tax_year2")
    private @Nullable Integer previousTaxYear2;

    @Column(name = "previous_tax_year2_extension", precision = 13, scale = 2)
    private @Nullable BigDecimal previousTaxYear2Extension;

    @Column(name = "previous_tax_year3")
    private @Nullable Integer previousTaxYear3;

    @Column(name = "previous_tax_year3_extension", precision = 13, scale = 2)
    private @Nullable BigDecimal previousTaxYear3Extension;

    @Column(name = "tax_cap_indicator")
    private @Nullable Boolean taxCapIndicator;

    @Column(name = "tax_year")
    private @Nullable Integer taxYear;

    @Column(name = "will_county_equalized_value", precision = 11, scale = 0)
    private @Nullable BigDecimal willCountyEqualizedValue;

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

    /// Returns the hydrated annexed property equalized value value.
    public @Nullable BigDecimal getAnnexedPropertyEqualizedValue() {
        return annexedPropertyEqualizedValue;
    }

    /// Supplies the annexed property equalized value value during hydration or mapping.
    public void setAnnexedPropertyEqualizedValue(
            @Nullable BigDecimal annexedPropertyEqualizedValue) {
        this.annexedPropertyEqualizedValue = annexedPropertyEqualizedValue;
    }

    /// Returns the hydrated burden percent value.
    public @Nullable BigDecimal getBurdenPercent() {
        return burdenPercent;
    }

    /// Supplies the burden percent value during hydration or mapping.
    public void setBurdenPercent(@Nullable BigDecimal burdenPercent) {
        this.burdenPercent = burdenPercent;
    }

    /// Returns the hydrated connecting agency1 value.
    public @Nullable String getConnectingAgency1() {
        return connectingAgency1;
    }

    /// Supplies the connecting agency1 value during hydration or mapping.
    public void setConnectingAgency1(@Nullable String connectingAgency1) {
        this.connectingAgency1 = connectingAgency1;
    }

    /// Returns the hydrated connecting agency2 value.
    public @Nullable String getConnectingAgency2() {
        return connectingAgency2;
    }

    /// Supplies the connecting agency2 value during hydration or mapping.
    public void setConnectingAgency2(@Nullable String connectingAgency2) {
        this.connectingAgency2 = connectingAgency2;
    }

    /// Returns the hydrated connecting agency3 value.
    public @Nullable String getConnectingAgency3() {
        return connectingAgency3;
    }

    /// Supplies the connecting agency3 value during hydration or mapping.
    public void setConnectingAgency3(@Nullable String connectingAgency3) {
        this.connectingAgency3 = connectingAgency3;
    }

    /// Returns the hydrated connecting agency4 value.
    public @Nullable String getConnectingAgency4() {
        return connectingAgency4;
    }

    /// Supplies the connecting agency4 value during hydration or mapping.
    public void setConnectingAgency4(@Nullable String connectingAgency4) {
        this.connectingAgency4 = connectingAgency4;
    }

    /// Returns the hydrated cook county air pollution value value.
    public @Nullable BigDecimal getCookCountyAirPollutionValue() {
        return cookCountyAirPollutionValue;
    }

    /// Supplies the cook county air pollution value value during hydration or mapping.
    public void setCookCountyAirPollutionValue(@Nullable BigDecimal cookCountyAirPollutionValue) {
        this.cookCountyAirPollutionValue = cookCountyAirPollutionValue;
    }

    /// Returns the hydrated cook county railroad value value.
    public @Nullable BigDecimal getCookCountyRailroadValue() {
        return cookCountyRailroadValue;
    }

    /// Supplies the cook county railroad value value during hydration or mapping.
    public void setCookCountyRailroadValue(@Nullable BigDecimal cookCountyRailroadValue) {
        this.cookCountyRailroadValue = cookCountyRailroadValue;
    }

    /// Returns the hydrated cook county real estate value value.
    public @Nullable BigDecimal getCookCountyRealEstateValue() {
        return cookCountyRealEstateValue;
    }

    /// Supplies the cook county real estate value value during hydration or mapping.
    public void setCookCountyRealEstateValue(@Nullable BigDecimal cookCountyRealEstateValue) {
        this.cookCountyRealEstateValue = cookCountyRealEstateValue;
    }

    /// Returns the hydrated cook county use tax value value.
    public @Nullable BigDecimal getCookCountyUseTaxValue() {
        return cookCountyUseTaxValue;
    }

    /// Supplies the cook county use tax value value during hydration or mapping.
    public void setCookCountyUseTaxValue(@Nullable BigDecimal cookCountyUseTaxValue) {
        this.cookCountyUseTaxValue = cookCountyUseTaxValue;
    }

    /// Returns the hydrated de kalb county equalized value value.
    public @Nullable BigDecimal getDeKalbCountyEqualizedValue() {
        return deKalbCountyEqualizedValue;
    }

    /// Supplies the de kalb county equalized value value during hydration or mapping.
    public void setDeKalbCountyEqualizedValue(@Nullable BigDecimal deKalbCountyEqualizedValue) {
        this.deKalbCountyEqualizedValue = deKalbCountyEqualizedValue;
    }

    /// Returns the hydrated disconnected property equalized value value.
    public @Nullable BigDecimal getDisconnectedPropertyEqualizedValue() {
        return disconnectedPropertyEqualizedValue;
    }

    /// Supplies the disconnected property equalized value value during hydration or mapping.
    public void setDisconnectedPropertyEqualizedValue(
            @Nullable BigDecimal disconnectedPropertyEqualizedValue) {
        this.disconnectedPropertyEqualizedValue = disconnectedPropertyEqualizedValue;
    }

    /// Returns the hydrated disconnected tif difference value.
    public @Nullable BigDecimal getDisconnectedTifDifference() {
        return disconnectedTifDifference;
    }

    /// Supplies the disconnected tif difference value during hydration or mapping.
    public void setDisconnectedTifDifference(@Nullable BigDecimal disconnectedTifDifference) {
        this.disconnectedTifDifference = disconnectedTifDifference;
    }

    /// Returns the hydrated du page county equalized value value.
    public @Nullable BigDecimal getDuPageCountyEqualizedValue() {
        return duPageCountyEqualizedValue;
    }

    /// Supplies the du page county equalized value value during hydration or mapping.
    public void setDuPageCountyEqualizedValue(@Nullable BigDecimal duPageCountyEqualizedValue) {
        this.duPageCountyEqualizedValue = duPageCountyEqualizedValue;
    }

    /// Returns the hydrated grundy county equalized value value.
    public @Nullable BigDecimal getGrundyCountyEqualizedValue() {
        return grundyCountyEqualizedValue;
    }

    /// Supplies the grundy county equalized value value during hydration or mapping.
    public void setGrundyCountyEqualizedValue(@Nullable BigDecimal grundyCountyEqualizedValue) {
        this.grundyCountyEqualizedValue = grundyCountyEqualizedValue;
    }

    /// Returns the hydrated kane county equalized value value.
    public @Nullable BigDecimal getKaneCountyEqualizedValue() {
        return kaneCountyEqualizedValue;
    }

    /// Supplies the kane county equalized value value during hydration or mapping.
    public void setKaneCountyEqualizedValue(@Nullable BigDecimal kaneCountyEqualizedValue) {
        this.kaneCountyEqualizedValue = kaneCountyEqualizedValue;
    }

    /// Returns the hydrated kankakee county equalized value value.
    public @Nullable BigDecimal getKankakeeCountyEqualizedValue() {
        return kankakeeCountyEqualizedValue;
    }

    /// Supplies the kankakee county equalized value value during hydration or mapping.
    public void setKankakeeCountyEqualizedValue(@Nullable BigDecimal kankakeeCountyEqualizedValue) {
        this.kankakeeCountyEqualizedValue = kankakeeCountyEqualizedValue;
    }

    /// Returns the hydrated kendall county equalized value value.
    public @Nullable BigDecimal getKendallCountyEqualizedValue() {
        return kendallCountyEqualizedValue;
    }

    /// Supplies the kendall county equalized value value during hydration or mapping.
    public void setKendallCountyEqualizedValue(@Nullable BigDecimal kendallCountyEqualizedValue) {
        this.kendallCountyEqualizedValue = kendallCountyEqualizedValue;
    }

    /// Returns the hydrated la salle county equalized value value.
    public @Nullable BigDecimal getLaSalleCountyEqualizedValue() {
        return laSalleCountyEqualizedValue;
    }

    /// Supplies the la salle county equalized value value during hydration or mapping.
    public void setLaSalleCountyEqualizedValue(@Nullable BigDecimal laSalleCountyEqualizedValue) {
        this.laSalleCountyEqualizedValue = laSalleCountyEqualizedValue;
    }

    /// Returns the hydrated lake county equalized value value.
    public @Nullable BigDecimal getLakeCountyEqualizedValue() {
        return lakeCountyEqualizedValue;
    }

    /// Supplies the lake county equalized value value during hydration or mapping.
    public void setLakeCountyEqualizedValue(@Nullable BigDecimal lakeCountyEqualizedValue) {
        this.lakeCountyEqualizedValue = lakeCountyEqualizedValue;
    }

    /// Returns the hydrated limiting tax rate override value.
    public @Nullable BigDecimal getLimitingTaxRateOverride() {
        return limitingTaxRateOverride;
    }

    /// Supplies the limiting tax rate override value during hydration or mapping.
    public void setLimitingTaxRateOverride(@Nullable BigDecimal limitingTaxRateOverride) {
        this.limitingTaxRateOverride = limitingTaxRateOverride;
    }

    /// Returns the hydrated livingston county equalized value value.
    public @Nullable BigDecimal getLivingstonCountyEqualizedValue() {
        return livingstonCountyEqualizedValue;
    }

    /// Supplies the livingston county equalized value value during hydration or mapping.
    public void setLivingstonCountyEqualizedValue(
            @Nullable BigDecimal livingstonCountyEqualizedValue) {
        this.livingstonCountyEqualizedValue = livingstonCountyEqualizedValue;
    }

    /// Returns the hydrated mc henry county equalized value value.
    public @Nullable BigDecimal getMcHenryCountyEqualizedValue() {
        return mcHenryCountyEqualizedValue;
    }

    /// Supplies the mc henry county equalized value value during hydration or mapping.
    public void setMcHenryCountyEqualizedValue(@Nullable BigDecimal mcHenryCountyEqualizedValue) {
        this.mcHenryCountyEqualizedValue = mcHenryCountyEqualizedValue;
    }

    /// Returns the hydrated new property equalized value value.
    public @Nullable BigDecimal getNewPropertyEqualizedValue() {
        return newPropertyEqualizedValue;
    }

    /// Supplies the new property equalized value value during hydration or mapping.
    public void setNewPropertyEqualizedValue(@Nullable BigDecimal newPropertyEqualizedValue) {
        this.newPropertyEqualizedValue = newPropertyEqualizedValue;
    }

    /// Returns the hydrated overlap annexed property equalized value value.
    public @Nullable BigDecimal getOverlapAnnexedPropertyEqualizedValue() {
        return overlapAnnexedPropertyEqualizedValue;
    }

    /// Supplies the overlap annexed property equalized value value during hydration or mapping.
    public void setOverlapAnnexedPropertyEqualizedValue(
            @Nullable BigDecimal overlapAnnexedPropertyEqualizedValue) {
        this.overlapAnnexedPropertyEqualizedValue = overlapAnnexedPropertyEqualizedValue;
    }

    /// Returns the hydrated overlap disconnected property equalized value value.
    public @Nullable BigDecimal getOverlapDisconnectedPropertyEqualizedValue() {
        return overlapDisconnectedPropertyEqualizedValue;
    }

    /// Supplies the overlap disconnected property equalized value value during hydration or
    /// mapping.
    public void setOverlapDisconnectedPropertyEqualizedValue(
            BigDecimal overlapDisconnectedPropertyEqualizedValue) {
        this.overlapDisconnectedPropertyEqualizedValue = overlapDisconnectedPropertyEqualizedValue;
    }

    /// Returns the hydrated overlap disconnected tif difference value.
    public @Nullable BigDecimal getOverlapDisconnectedTifDifference() {
        return overlapDisconnectedTifDifference;
    }

    /// Supplies the overlap disconnected tif difference value during hydration or mapping.
    public void setOverlapDisconnectedTifDifference(
            @Nullable BigDecimal overlapDisconnectedTifDifference) {
        this.overlapDisconnectedTifDifference = overlapDisconnectedTifDifference;
    }

    /// Returns the hydrated overlap new property equalized value value.
    public @Nullable BigDecimal getOverlapNewPropertyEqualizedValue() {
        return overlapNewPropertyEqualizedValue;
    }

    /// Supplies the overlap new property equalized value value during hydration or mapping.
    public void setOverlapNewPropertyEqualizedValue(
            @Nullable BigDecimal overlapNewPropertyEqualizedValue) {
        this.overlapNewPropertyEqualizedValue = overlapNewPropertyEqualizedValue;
    }

    /// Returns the hydrated parent agency1 value.
    public @Nullable String getParentAgency1() {
        return parentAgency1;
    }

    /// Supplies the parent agency1 value during hydration or mapping.
    public void setParentAgency1(@Nullable String parentAgency1) {
        this.parentAgency1 = parentAgency1;
    }

    /// Returns the hydrated parent agency2 value.
    public @Nullable String getParentAgency2() {
        return parentAgency2;
    }

    /// Supplies the parent agency2 value during hydration or mapping.
    public void setParentAgency2(@Nullable String parentAgency2) {
        this.parentAgency2 = parentAgency2;
    }

    /// Returns the hydrated parent agency3 value.
    public @Nullable String getParentAgency3() {
        return parentAgency3;
    }

    /// Supplies the parent agency3 value during hydration or mapping.
    public void setParentAgency3(@Nullable String parentAgency3) {
        this.parentAgency3 = parentAgency3;
    }

    /// Returns the hydrated parent agency4 value.
    public @Nullable String getParentAgency4() {
        return parentAgency4;
    }

    /// Supplies the parent agency4 value during hydration or mapping.
    public void setParentAgency4(@Nullable String parentAgency4) {
        this.parentAgency4 = parentAgency4;
    }

    /// Returns the hydrated parent agency5 value.
    public @Nullable String getParentAgency5() {
        return parentAgency5;
    }

    /// Supplies the parent agency5 value during hydration or mapping.
    public void setParentAgency5(@Nullable String parentAgency5) {
        this.parentAgency5 = parentAgency5;
    }

    /// Returns the hydrated previous tax year1 value.
    public @Nullable Integer getPreviousTaxYear1() {
        return previousTaxYear1;
    }

    /// Supplies the previous tax year1 value during hydration or mapping.
    public void setPreviousTaxYear1(@Nullable Integer previousTaxYear1) {
        this.previousTaxYear1 = previousTaxYear1;
    }

    /// Returns the hydrated previous tax year1 extension value.
    public @Nullable BigDecimal getPreviousTaxYear1Extension() {
        return previousTaxYear1Extension;
    }

    /// Supplies the previous tax year1 extension value during hydration or mapping.
    public void setPreviousTaxYear1Extension(@Nullable BigDecimal previousTaxYear1Extension) {
        this.previousTaxYear1Extension = previousTaxYear1Extension;
    }

    /// Returns the hydrated previous tax year2 value.
    public @Nullable Integer getPreviousTaxYear2() {
        return previousTaxYear2;
    }

    /// Supplies the previous tax year2 value during hydration or mapping.
    public void setPreviousTaxYear2(@Nullable Integer previousTaxYear2) {
        this.previousTaxYear2 = previousTaxYear2;
    }

    /// Returns the hydrated previous tax year2 extension value.
    public @Nullable BigDecimal getPreviousTaxYear2Extension() {
        return previousTaxYear2Extension;
    }

    /// Supplies the previous tax year2 extension value during hydration or mapping.
    public void setPreviousTaxYear2Extension(@Nullable BigDecimal previousTaxYear2Extension) {
        this.previousTaxYear2Extension = previousTaxYear2Extension;
    }

    /// Returns the hydrated previous tax year3 value.
    public @Nullable Integer getPreviousTaxYear3() {
        return previousTaxYear3;
    }

    /// Supplies the previous tax year3 value during hydration or mapping.
    public void setPreviousTaxYear3(@Nullable Integer previousTaxYear3) {
        this.previousTaxYear3 = previousTaxYear3;
    }

    /// Returns the hydrated previous tax year3 extension value.
    public @Nullable BigDecimal getPreviousTaxYear3Extension() {
        return previousTaxYear3Extension;
    }

    /// Supplies the previous tax year3 extension value during hydration or mapping.
    public void setPreviousTaxYear3Extension(@Nullable BigDecimal previousTaxYear3Extension) {
        this.previousTaxYear3Extension = previousTaxYear3Extension;
    }

    /// Returns the hydrated tax cap indicator value.
    public @Nullable Boolean getTaxCapIndicator() {
        return taxCapIndicator;
    }

    /// Supplies the tax cap indicator value during hydration or mapping.
    public void setTaxCapIndicator(@Nullable Boolean taxCapIndicator) {
        this.taxCapIndicator = taxCapIndicator;
    }

    /// Returns the hydrated tax year value.
    public @Nullable Integer getTaxYear() {
        return taxYear;
    }

    /// Supplies the tax year value during hydration or mapping.
    public void setTaxYear(@Nullable Integer taxYear) {
        this.taxYear = taxYear;
    }

    /// Returns the hydrated will county equalized value value.
    public @Nullable BigDecimal getWillCountyEqualizedValue() {
        return willCountyEqualizedValue;
    }

    /// Supplies the will county equalized value value during hydration or mapping.
    public void setWillCountyEqualizedValue(@Nullable BigDecimal willCountyEqualizedValue) {
        this.willCountyEqualizedValue = willCountyEqualizedValue;
    }

    // GENERATED-ACCESSORS:end
}
