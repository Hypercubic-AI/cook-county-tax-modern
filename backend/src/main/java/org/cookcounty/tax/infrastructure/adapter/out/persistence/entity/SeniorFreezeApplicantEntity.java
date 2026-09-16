
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

@Entity
@Table(name = "senior_freeze_applicants")
public class SeniorFreezeApplicantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "age")
    private Integer age;

    @Column(name = "applicant_address")
    private String applicantAddress;

    @Column(name = "applicant_city")
    private String applicantCity;

    @Column(name = "applicant_first_name")
    private String applicantFirstName;

    @Column(name = "applicant_last_name")
    private String applicantLastName;

    @Column(name = "applicant_middle_initial")
    private String applicantMiddleInitial;

    @Column(name = "applicant_old_name")
    private String applicantOldName;

    @Column(name = "applicant_state")
    private String applicantState;

    @Column(name = "applicant_title")
    private String applicantTitle;

    @Column(name = "applicant_zip_code")
    private Long applicantZipCode;

    @Column(name = "base_year")
    private Integer baseYear;

    @Column(name = "base_year_eligible_equalized_value")
    private Long baseYearEligibleEqualizedValue;

    @Column(name = "base_year_indicator")
    private String baseYearIndicator;

    @Column(name = "batch_number")
    private Integer batchNumber;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "civil_service_benefits")
    private BigDecimal civilServiceBenefits;

    @Column(name = "cooperative_senior_shares")
    private Integer cooperativeSeniorShares;

    @Column(name = "denial_date")
    private Integer denialDate;

    @Column(name = "first_application_date")
    private Integer firstApplicationDate;

    @Column(name = "homeowner_base_year")
    private Integer homeownerBaseYear;

    @Column(name = "homeowner_base_year_assessed_value")
    private Long homeownerBaseYearAssessedValue;

    @Column(name = "homeowner_base_year_equalization_factor")
    private BigDecimal homeownerBaseYearEqualizationFactor;

    @Column(name = "homeowner_base_year_equalized_value")
    private Long homeownerBaseYearEqualizedValue;

    @Column(name = "homeowner_eligibility_indicator")
    private Integer homeownerEligibilityIndicator;

    @Column(name = "homeowner_status")
    private String homeownerStatus;

    @Column(name = "homestead_batch_number")
    private Integer homesteadBatchNumber;

    @Column(name = "homestead_percent_shares")
    private BigDecimal homesteadPercentShares;

    @Column(name = "homestead_shares")
    private Integer homesteadShares;

    @Column(name = "homestead_status")
    private String homesteadStatus;

    @Column(name = "homestead_year_applied")
    private Integer homesteadYearApplied;

    @Column(name = "interest_income")
    private BigDecimal interestIncome;

    @Column(name = "last_application_date")
    private Integer lastApplicationDate;

    @Column(name = "life_care_facility_indicator")
    private String lifeCareFacilityIndicator;

    @Column(name = "maintenance_indicator")
    private Integer maintenanceIndicator;

    @Column(name = "name_maintenance_indicator")
    private Integer nameMaintenanceIndicator;

    @Column(name = "net_capital_gain")
    private BigDecimal netCapitalGain;

    @Column(name = "net_rental_income")
    private BigDecimal netRentalIncome;

    @Column(name = "no_income_indicator")
    private String noIncomeIndicator;

    @Column(name = "notarized_indicator")
    private String notarizedIndicator;

    @Column(name = "other_benefits")
    private BigDecimal otherBenefits;

    @Column(name = "other_income")
    private BigDecimal otherIncome;

    @Column(name = "percent_senior_shares")
    private BigDecimal percentSeniorShares;

    @Column(name = "phone_number")
    private Long phoneNumber;

    @Column(name = "public_aid")
    private BigDecimal publicAid;

    @Column(name = "qualification_date")
    private Integer qualificationDate;

    @Column(name = "railroad_benefits")
    private BigDecimal railroadBenefits;

    @Column(name = "returned_date")
    private Integer returnedDate;

    @Column(name = "senior_freeze_percent")
    private BigDecimal seniorFreezePercent;

    @Column(name = "senior_freeze_status")
    private String seniorFreezeStatus;

    @Column(name = "signed_indicator")
    private String signedIndicator;

    @Column(name = "social_security_income")
    private BigDecimal socialSecurityIncome;

    @Column(name = "social_security_number")
    private Long socialSecurityNumber;

    @Column(name = "total_income")
    private BigDecimal totalIncome;

    @Column(name = "veterans_benefits")
    private BigDecimal veteransBenefits;

    @Column(name = "wages")
    private BigDecimal wages;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }


    public String getApplicantAddress() { return applicantAddress; }
    public void setApplicantAddress(String applicantAddress) { this.applicantAddress = applicantAddress; }


    public String getApplicantCity() { return applicantCity; }
    public void setApplicantCity(String applicantCity) { this.applicantCity = applicantCity; }


    public String getApplicantFirstName() { return applicantFirstName; }
    public void setApplicantFirstName(String applicantFirstName) { this.applicantFirstName = applicantFirstName; }


    public String getApplicantLastName() { return applicantLastName; }
    public void setApplicantLastName(String applicantLastName) { this.applicantLastName = applicantLastName; }


    public String getApplicantMiddleInitial() { return applicantMiddleInitial; }
    public void setApplicantMiddleInitial(String applicantMiddleInitial) { this.applicantMiddleInitial = applicantMiddleInitial; }


    public String getApplicantOldName() { return applicantOldName; }
    public void setApplicantOldName(String applicantOldName) { this.applicantOldName = applicantOldName; }


    public String getApplicantState() { return applicantState; }
    public void setApplicantState(String applicantState) { this.applicantState = applicantState; }


    public String getApplicantTitle() { return applicantTitle; }
    public void setApplicantTitle(String applicantTitle) { this.applicantTitle = applicantTitle; }


    public Long getApplicantZipCode() { return applicantZipCode; }
    public void setApplicantZipCode(Long applicantZipCode) { this.applicantZipCode = applicantZipCode; }


    public Integer getBaseYear() { return baseYear; }
    public void setBaseYear(Integer baseYear) { this.baseYear = baseYear; }


    public Long getBaseYearEligibleEqualizedValue() { return baseYearEligibleEqualizedValue; }
    public void setBaseYearEligibleEqualizedValue(Long baseYearEligibleEqualizedValue) { this.baseYearEligibleEqualizedValue = baseYearEligibleEqualizedValue; }


    public String getBaseYearIndicator() { return baseYearIndicator; }
    public void setBaseYearIndicator(String baseYearIndicator) { this.baseYearIndicator = baseYearIndicator; }


    public Integer getBatchNumber() { return batchNumber; }
    public void setBatchNumber(Integer batchNumber) { this.batchNumber = batchNumber; }


    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }


    public BigDecimal getCivilServiceBenefits() { return civilServiceBenefits; }
    public void setCivilServiceBenefits(BigDecimal civilServiceBenefits) { this.civilServiceBenefits = civilServiceBenefits; }


    public Integer getCooperativeSeniorShares() { return cooperativeSeniorShares; }
    public void setCooperativeSeniorShares(Integer cooperativeSeniorShares) { this.cooperativeSeniorShares = cooperativeSeniorShares; }


    public Integer getDenialDate() { return denialDate; }
    public void setDenialDate(Integer denialDate) { this.denialDate = denialDate; }


    public Integer getFirstApplicationDate() { return firstApplicationDate; }
    public void setFirstApplicationDate(Integer firstApplicationDate) { this.firstApplicationDate = firstApplicationDate; }


    public Integer getHomeownerBaseYear() { return homeownerBaseYear; }
    public void setHomeownerBaseYear(Integer homeownerBaseYear) { this.homeownerBaseYear = homeownerBaseYear; }


    public Long getHomeownerBaseYearAssessedValue() { return homeownerBaseYearAssessedValue; }
    public void setHomeownerBaseYearAssessedValue(Long homeownerBaseYearAssessedValue) { this.homeownerBaseYearAssessedValue = homeownerBaseYearAssessedValue; }


    public BigDecimal getHomeownerBaseYearEqualizationFactor() { return homeownerBaseYearEqualizationFactor; }
    public void setHomeownerBaseYearEqualizationFactor(BigDecimal homeownerBaseYearEqualizationFactor) { this.homeownerBaseYearEqualizationFactor = homeownerBaseYearEqualizationFactor; }


    public Long getHomeownerBaseYearEqualizedValue() { return homeownerBaseYearEqualizedValue; }
    public void setHomeownerBaseYearEqualizedValue(Long homeownerBaseYearEqualizedValue) { this.homeownerBaseYearEqualizedValue = homeownerBaseYearEqualizedValue; }


    public Integer getHomeownerEligibilityIndicator() { return homeownerEligibilityIndicator; }
    public void setHomeownerEligibilityIndicator(Integer homeownerEligibilityIndicator) { this.homeownerEligibilityIndicator = homeownerEligibilityIndicator; }


    public String getHomeownerStatus() { return homeownerStatus; }
    public void setHomeownerStatus(String homeownerStatus) { this.homeownerStatus = homeownerStatus; }


    public Integer getHomesteadBatchNumber() { return homesteadBatchNumber; }
    public void setHomesteadBatchNumber(Integer homesteadBatchNumber) { this.homesteadBatchNumber = homesteadBatchNumber; }


    public BigDecimal getHomesteadPercentShares() { return homesteadPercentShares; }
    public void setHomesteadPercentShares(BigDecimal homesteadPercentShares) { this.homesteadPercentShares = homesteadPercentShares; }


    public Integer getHomesteadShares() { return homesteadShares; }
    public void setHomesteadShares(Integer homesteadShares) { this.homesteadShares = homesteadShares; }


    public String getHomesteadStatus() { return homesteadStatus; }
    public void setHomesteadStatus(String homesteadStatus) { this.homesteadStatus = homesteadStatus; }


    public Integer getHomesteadYearApplied() { return homesteadYearApplied; }
    public void setHomesteadYearApplied(Integer homesteadYearApplied) { this.homesteadYearApplied = homesteadYearApplied; }


    public BigDecimal getInterestIncome() { return interestIncome; }
    public void setInterestIncome(BigDecimal interestIncome) { this.interestIncome = interestIncome; }


    public Integer getLastApplicationDate() { return lastApplicationDate; }
    public void setLastApplicationDate(Integer lastApplicationDate) { this.lastApplicationDate = lastApplicationDate; }


    public String getLifeCareFacilityIndicator() { return lifeCareFacilityIndicator; }
    public void setLifeCareFacilityIndicator(String lifeCareFacilityIndicator) { this.lifeCareFacilityIndicator = lifeCareFacilityIndicator; }


    public Integer getMaintenanceIndicator() { return maintenanceIndicator; }
    public void setMaintenanceIndicator(Integer maintenanceIndicator) { this.maintenanceIndicator = maintenanceIndicator; }


    public Integer getNameMaintenanceIndicator() { return nameMaintenanceIndicator; }
    public void setNameMaintenanceIndicator(Integer nameMaintenanceIndicator) { this.nameMaintenanceIndicator = nameMaintenanceIndicator; }


    public BigDecimal getNetCapitalGain() { return netCapitalGain; }
    public void setNetCapitalGain(BigDecimal netCapitalGain) { this.netCapitalGain = netCapitalGain; }


    public BigDecimal getNetRentalIncome() { return netRentalIncome; }
    public void setNetRentalIncome(BigDecimal netRentalIncome) { this.netRentalIncome = netRentalIncome; }


    public String getNoIncomeIndicator() { return noIncomeIndicator; }
    public void setNoIncomeIndicator(String noIncomeIndicator) { this.noIncomeIndicator = noIncomeIndicator; }


    public String getNotarizedIndicator() { return notarizedIndicator; }
    public void setNotarizedIndicator(String notarizedIndicator) { this.notarizedIndicator = notarizedIndicator; }


    public BigDecimal getOtherBenefits() { return otherBenefits; }
    public void setOtherBenefits(BigDecimal otherBenefits) { this.otherBenefits = otherBenefits; }


    public BigDecimal getOtherIncome() { return otherIncome; }
    public void setOtherIncome(BigDecimal otherIncome) { this.otherIncome = otherIncome; }


    public BigDecimal getPercentSeniorShares() { return percentSeniorShares; }
    public void setPercentSeniorShares(BigDecimal percentSeniorShares) { this.percentSeniorShares = percentSeniorShares; }


    public Long getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(Long phoneNumber) { this.phoneNumber = phoneNumber; }


    public BigDecimal getPublicAid() { return publicAid; }
    public void setPublicAid(BigDecimal publicAid) { this.publicAid = publicAid; }


    public Integer getQualificationDate() { return qualificationDate; }
    public void setQualificationDate(Integer qualificationDate) { this.qualificationDate = qualificationDate; }


    public BigDecimal getRailroadBenefits() { return railroadBenefits; }
    public void setRailroadBenefits(BigDecimal railroadBenefits) { this.railroadBenefits = railroadBenefits; }


    public Integer getReturnedDate() { return returnedDate; }
    public void setReturnedDate(Integer returnedDate) { this.returnedDate = returnedDate; }


    public BigDecimal getSeniorFreezePercent() { return seniorFreezePercent; }
    public void setSeniorFreezePercent(BigDecimal seniorFreezePercent) { this.seniorFreezePercent = seniorFreezePercent; }


    public String getSeniorFreezeStatus() { return seniorFreezeStatus; }
    public void setSeniorFreezeStatus(String seniorFreezeStatus) { this.seniorFreezeStatus = seniorFreezeStatus; }


    public String getSignedIndicator() { return signedIndicator; }
    public void setSignedIndicator(String signedIndicator) { this.signedIndicator = signedIndicator; }


    public BigDecimal getSocialSecurityIncome() { return socialSecurityIncome; }
    public void setSocialSecurityIncome(BigDecimal socialSecurityIncome) { this.socialSecurityIncome = socialSecurityIncome; }


    public Long getSocialSecurityNumber() { return socialSecurityNumber; }
    public void setSocialSecurityNumber(Long socialSecurityNumber) { this.socialSecurityNumber = socialSecurityNumber; }


    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }


    public BigDecimal getVeteransBenefits() { return veteransBenefits; }
    public void setVeteransBenefits(BigDecimal veteransBenefits) { this.veteransBenefits = veteransBenefits; }


    public BigDecimal getWages() { return wages; }
    public void setWages(BigDecimal wages) { this.wages = wages; }



    // GENERATED-ACCESSORS:end
}
