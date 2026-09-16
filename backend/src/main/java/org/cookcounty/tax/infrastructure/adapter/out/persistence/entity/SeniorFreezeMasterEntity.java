
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

@Entity
@Table(name = "senior_freeze_masters")
public class SeniorFreezeMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "base_value_manual_calculation_indicator")
    private String baseValueManualCalculationIndicator;

    @Column(name = "base_value_no_calculation_indicator")
    private String baseValueNoCalculationIndicator;

    @Column(name = "base_value_year")
    private Integer baseValueYear;

    @Column(name = "base_value_year_class")
    private Integer baseValueYearClass;

    @Column(name = "base_year_eligible_computed_full_assessed_value")
    private Long baseYearEligibleComputedFullAssessedValue;

    @Column(name = "base_year_equalized_value")
    private Long baseYearEqualizedValue;

    @Column(name = "base_year_full_assessed_value")
    private Long baseYearFullAssessedValue;

    @Column(name = "base_year_total_eligible_computed_equalized_value")
    private Long baseYearTotalEligibleComputedEqualizedValue;

    @Column(name = "building_shares")
    private Integer buildingShares;

    @Column(name = "building_units")
    private Integer buildingUnits;

    @Column(name = "calculation_type")
    private String calculationType;

    @Column(name = "class288_expiration_assessed_value")
    private Long class288ExpirationAssessedValue;

    @Column(name = "class288_expiration_equalized_value")
    private Long class288ExpirationEqualizedValue;

    @Column(name = "class288_over_limit_assessed_value")
    private Long class288OverLimitAssessedValue;

    @Column(name = "class288_over_limit_equalized_value")
    private Long class288OverLimitEqualizedValue;

    @Column(name = "current_year_class")
    private Integer currentYearClass;

    @Column(name = "current_year_eligible_computed_assessed_value")
    private Long currentYearEligibleComputedAssessedValue;

    @Column(name = "current_year_eligible_computed_equalized_value")
    private Long currentYearEligibleComputedEqualizedValue;

    @Column(name = "current_year_farm_indicator")
    private String currentYearFarmIndicator;

    @Column(name = "current_year_final_equalized_value_difference")
    private Long currentYearFinalEqualizedValueDifference;

    @Column(name = "current_year_full_assessed_value")
    private Long currentYearFullAssessedValue;

    @Column(name = "current_year_full_equalized_value")
    private Long currentYearFullEqualizedValue;

    @Column(name = "current_year_not_eligible_assessed_value")
    private Long currentYearNotEligibleAssessedValue;

    @Column(name = "current_year_not_eligible_equalized_value")
    private Long currentYearNotEligibleEqualizedValue;

    @Column(name = "homeowner_units")
    private Integer homeownerUnits;

    @Column(name = "homestead_units")
    private Integer homesteadUnits;

    @Column(name = "key_parcel_number")
    private Long keyParcelNumber;

    @Column(name = "mailing_city")
    private String mailingCity;

    @Column(name = "mailing_direction")
    private String mailingDirection;

    @Column(name = "mailing_house_number")
    private String mailingHouseNumber;

    @Column(name = "mailing_state")
    private String mailingState;

    @Column(name = "mailing_street")
    private String mailingStreet;

    @Column(name = "mailing_suffix")
    private String mailingSuffix;

    @Column(name = "mailing_zip_code")
    private Long mailingZipCode;

    @Column(name = "maintenance_indicator")
    private Integer maintenanceIndicator;

    @Column(name = "master_name")
    private String masterName;

    @Column(name = "occupancy_factor")
    private BigDecimal occupancyFactor;

    @Column(name = "original_base_value_year")
    private Integer originalBaseValueYear;

    @Column(name = "original_base_year_eligible_computed_full_assessed_value")
    private Long originalBaseYearEligibleComputedFullAssessedValue;

    @Column(name = "original_base_year_equalized_value")
    private Long originalBaseYearEqualizedValue;

    @Column(name = "original_base_year_full_assessed_value")
    private Long originalBaseYearFullAssessedValue;

    @Column(name = "original_base_year_total_eligible_computed_equalized_value")
    private Long originalBaseYearTotalEligibleComputedEqualizedValue;

    @Column(name = "original_current_year_final_equalized_value_difference")
    private Long originalCurrentYearFinalEqualizedValueDifference;

    @Column(name = "original_manual_calculation_indicator")
    private String originalManualCalculationIndicator;

    @Column(name = "property_proration")
    private BigDecimal propertyProration;

    @Column(name = "record_code")
    private String recordCode;

    @Column(name = "senior_freeze_shares")
    private Integer seniorFreezeShares;

    @Column(name = "split_code")
    private Integer splitCode;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public String getBaseValueManualCalculationIndicator() { return baseValueManualCalculationIndicator; }
    public void setBaseValueManualCalculationIndicator(String baseValueManualCalculationIndicator) { this.baseValueManualCalculationIndicator = baseValueManualCalculationIndicator; }


    public String getBaseValueNoCalculationIndicator() { return baseValueNoCalculationIndicator; }
    public void setBaseValueNoCalculationIndicator(String baseValueNoCalculationIndicator) { this.baseValueNoCalculationIndicator = baseValueNoCalculationIndicator; }


    public Integer getBaseValueYear() { return baseValueYear; }
    public void setBaseValueYear(Integer baseValueYear) { this.baseValueYear = baseValueYear; }


    public Integer getBaseValueYearClass() { return baseValueYearClass; }
    public void setBaseValueYearClass(Integer baseValueYearClass) { this.baseValueYearClass = baseValueYearClass; }


    public Long getBaseYearEligibleComputedFullAssessedValue() { return baseYearEligibleComputedFullAssessedValue; }
    public void setBaseYearEligibleComputedFullAssessedValue(Long baseYearEligibleComputedFullAssessedValue) { this.baseYearEligibleComputedFullAssessedValue = baseYearEligibleComputedFullAssessedValue; }


    public Long getBaseYearEqualizedValue() { return baseYearEqualizedValue; }
    public void setBaseYearEqualizedValue(Long baseYearEqualizedValue) { this.baseYearEqualizedValue = baseYearEqualizedValue; }


    public Long getBaseYearFullAssessedValue() { return baseYearFullAssessedValue; }
    public void setBaseYearFullAssessedValue(Long baseYearFullAssessedValue) { this.baseYearFullAssessedValue = baseYearFullAssessedValue; }


    public Long getBaseYearTotalEligibleComputedEqualizedValue() { return baseYearTotalEligibleComputedEqualizedValue; }
    public void setBaseYearTotalEligibleComputedEqualizedValue(Long baseYearTotalEligibleComputedEqualizedValue) { this.baseYearTotalEligibleComputedEqualizedValue = baseYearTotalEligibleComputedEqualizedValue; }


    public Integer getBuildingShares() { return buildingShares; }
    public void setBuildingShares(Integer buildingShares) { this.buildingShares = buildingShares; }


    public Integer getBuildingUnits() { return buildingUnits; }
    public void setBuildingUnits(Integer buildingUnits) { this.buildingUnits = buildingUnits; }


    public String getCalculationType() { return calculationType; }
    public void setCalculationType(String calculationType) { this.calculationType = calculationType; }


    public Long getClass288ExpirationAssessedValue() { return class288ExpirationAssessedValue; }
    public void setClass288ExpirationAssessedValue(Long class288ExpirationAssessedValue) { this.class288ExpirationAssessedValue = class288ExpirationAssessedValue; }


    public Long getClass288ExpirationEqualizedValue() { return class288ExpirationEqualizedValue; }
    public void setClass288ExpirationEqualizedValue(Long class288ExpirationEqualizedValue) { this.class288ExpirationEqualizedValue = class288ExpirationEqualizedValue; }


    public Long getClass288OverLimitAssessedValue() { return class288OverLimitAssessedValue; }
    public void setClass288OverLimitAssessedValue(Long class288OverLimitAssessedValue) { this.class288OverLimitAssessedValue = class288OverLimitAssessedValue; }


    public Long getClass288OverLimitEqualizedValue() { return class288OverLimitEqualizedValue; }
    public void setClass288OverLimitEqualizedValue(Long class288OverLimitEqualizedValue) { this.class288OverLimitEqualizedValue = class288OverLimitEqualizedValue; }


    public Integer getCurrentYearClass() { return currentYearClass; }
    public void setCurrentYearClass(Integer currentYearClass) { this.currentYearClass = currentYearClass; }


    public Long getCurrentYearEligibleComputedAssessedValue() { return currentYearEligibleComputedAssessedValue; }
    public void setCurrentYearEligibleComputedAssessedValue(Long currentYearEligibleComputedAssessedValue) { this.currentYearEligibleComputedAssessedValue = currentYearEligibleComputedAssessedValue; }


    public Long getCurrentYearEligibleComputedEqualizedValue() { return currentYearEligibleComputedEqualizedValue; }
    public void setCurrentYearEligibleComputedEqualizedValue(Long currentYearEligibleComputedEqualizedValue) { this.currentYearEligibleComputedEqualizedValue = currentYearEligibleComputedEqualizedValue; }


    public String getCurrentYearFarmIndicator() { return currentYearFarmIndicator; }
    public void setCurrentYearFarmIndicator(String currentYearFarmIndicator) { this.currentYearFarmIndicator = currentYearFarmIndicator; }


    public Long getCurrentYearFinalEqualizedValueDifference() { return currentYearFinalEqualizedValueDifference; }
    public void setCurrentYearFinalEqualizedValueDifference(Long currentYearFinalEqualizedValueDifference) { this.currentYearFinalEqualizedValueDifference = currentYearFinalEqualizedValueDifference; }


    public Long getCurrentYearFullAssessedValue() { return currentYearFullAssessedValue; }
    public void setCurrentYearFullAssessedValue(Long currentYearFullAssessedValue) { this.currentYearFullAssessedValue = currentYearFullAssessedValue; }


    public Long getCurrentYearFullEqualizedValue() { return currentYearFullEqualizedValue; }
    public void setCurrentYearFullEqualizedValue(Long currentYearFullEqualizedValue) { this.currentYearFullEqualizedValue = currentYearFullEqualizedValue; }


    public Long getCurrentYearNotEligibleAssessedValue() { return currentYearNotEligibleAssessedValue; }
    public void setCurrentYearNotEligibleAssessedValue(Long currentYearNotEligibleAssessedValue) { this.currentYearNotEligibleAssessedValue = currentYearNotEligibleAssessedValue; }


    public Long getCurrentYearNotEligibleEqualizedValue() { return currentYearNotEligibleEqualizedValue; }
    public void setCurrentYearNotEligibleEqualizedValue(Long currentYearNotEligibleEqualizedValue) { this.currentYearNotEligibleEqualizedValue = currentYearNotEligibleEqualizedValue; }


    public Integer getHomeownerUnits() { return homeownerUnits; }
    public void setHomeownerUnits(Integer homeownerUnits) { this.homeownerUnits = homeownerUnits; }


    public Integer getHomesteadUnits() { return homesteadUnits; }
    public void setHomesteadUnits(Integer homesteadUnits) { this.homesteadUnits = homesteadUnits; }


    public Long getKeyParcelNumber() { return keyParcelNumber; }
    public void setKeyParcelNumber(Long keyParcelNumber) { this.keyParcelNumber = keyParcelNumber; }


    public String getMailingCity() { return mailingCity; }
    public void setMailingCity(String mailingCity) { this.mailingCity = mailingCity; }


    public String getMailingDirection() { return mailingDirection; }
    public void setMailingDirection(String mailingDirection) { this.mailingDirection = mailingDirection; }


    public String getMailingHouseNumber() { return mailingHouseNumber; }
    public void setMailingHouseNumber(String mailingHouseNumber) { this.mailingHouseNumber = mailingHouseNumber; }


    public String getMailingState() { return mailingState; }
    public void setMailingState(String mailingState) { this.mailingState = mailingState; }


    public String getMailingStreet() { return mailingStreet; }
    public void setMailingStreet(String mailingStreet) { this.mailingStreet = mailingStreet; }


    public String getMailingSuffix() { return mailingSuffix; }
    public void setMailingSuffix(String mailingSuffix) { this.mailingSuffix = mailingSuffix; }


    public Long getMailingZipCode() { return mailingZipCode; }
    public void setMailingZipCode(Long mailingZipCode) { this.mailingZipCode = mailingZipCode; }


    public Integer getMaintenanceIndicator() { return maintenanceIndicator; }
    public void setMaintenanceIndicator(Integer maintenanceIndicator) { this.maintenanceIndicator = maintenanceIndicator; }


    public String getMasterName() { return masterName; }
    public void setMasterName(String masterName) { this.masterName = masterName; }


    public BigDecimal getOccupancyFactor() { return occupancyFactor; }
    public void setOccupancyFactor(BigDecimal occupancyFactor) { this.occupancyFactor = occupancyFactor; }


    public Integer getOriginalBaseValueYear() { return originalBaseValueYear; }
    public void setOriginalBaseValueYear(Integer originalBaseValueYear) { this.originalBaseValueYear = originalBaseValueYear; }


    public Long getOriginalBaseYearEligibleComputedFullAssessedValue() { return originalBaseYearEligibleComputedFullAssessedValue; }
    public void setOriginalBaseYearEligibleComputedFullAssessedValue(Long originalBaseYearEligibleComputedFullAssessedValue) { this.originalBaseYearEligibleComputedFullAssessedValue = originalBaseYearEligibleComputedFullAssessedValue; }


    public Long getOriginalBaseYearEqualizedValue() { return originalBaseYearEqualizedValue; }
    public void setOriginalBaseYearEqualizedValue(Long originalBaseYearEqualizedValue) { this.originalBaseYearEqualizedValue = originalBaseYearEqualizedValue; }


    public Long getOriginalBaseYearFullAssessedValue() { return originalBaseYearFullAssessedValue; }
    public void setOriginalBaseYearFullAssessedValue(Long originalBaseYearFullAssessedValue) { this.originalBaseYearFullAssessedValue = originalBaseYearFullAssessedValue; }


    public Long getOriginalBaseYearTotalEligibleComputedEqualizedValue() { return originalBaseYearTotalEligibleComputedEqualizedValue; }
    public void setOriginalBaseYearTotalEligibleComputedEqualizedValue(Long originalBaseYearTotalEligibleComputedEqualizedValue) { this.originalBaseYearTotalEligibleComputedEqualizedValue = originalBaseYearTotalEligibleComputedEqualizedValue; }


    public Long getOriginalCurrentYearFinalEqualizedValueDifference() { return originalCurrentYearFinalEqualizedValueDifference; }
    public void setOriginalCurrentYearFinalEqualizedValueDifference(Long originalCurrentYearFinalEqualizedValueDifference) { this.originalCurrentYearFinalEqualizedValueDifference = originalCurrentYearFinalEqualizedValueDifference; }


    public String getOriginalManualCalculationIndicator() { return originalManualCalculationIndicator; }
    public void setOriginalManualCalculationIndicator(String originalManualCalculationIndicator) { this.originalManualCalculationIndicator = originalManualCalculationIndicator; }


    public BigDecimal getPropertyProration() { return propertyProration; }
    public void setPropertyProration(BigDecimal propertyProration) { this.propertyProration = propertyProration; }


    public String getRecordCode() { return recordCode; }
    public void setRecordCode(String recordCode) { this.recordCode = recordCode; }


    public Integer getSeniorFreezeShares() { return seniorFreezeShares; }
    public void setSeniorFreezeShares(Integer seniorFreezeShares) { this.seniorFreezeShares = seniorFreezeShares; }


    public Integer getSplitCode() { return splitCode; }
    public void setSplitCode(Integer splitCode) { this.splitCode = splitCode; }



    // GENERATED-ACCESSORS:end
}
