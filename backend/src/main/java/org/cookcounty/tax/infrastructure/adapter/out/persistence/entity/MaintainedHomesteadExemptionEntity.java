
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

@Entity
@Table(name = "maintained_homestead_exemptions")
public class MaintainedHomesteadExemptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "application_year")
    private Integer applicationYear;

    @Column(name = "assessed_value")
    private Long assessedValue;

    @Column(name = "assessment_class")
    private Integer assessmentClass;

    @Column(name = "certificate_of_error_number")
    private Integer certificateOfErrorNumber;

    @Column(name = "city")
    private String city;

    @Column(name = "clerks_class")
    private Integer clerksClass;

    @Column(name = "cooperative_quantity")
    private Integer cooperativeQuantity;

    @Column(name = "equalization_factor")
    private BigDecimal equalizationFactor;

    @Column(name = "equalized_value")
    private Long equalizedValue;

    @Column(name = "exemption_type")
    private Integer exemptionType;

    @Column(name = "mailing_address")
    private String mailingAddress;

    @Column(name = "nphe_amount")
    private Long npheAmount;

    @Column(name = "nphe_base_year")
    private Integer npheBaseYear;

    @Column(name = "nphe_status")
    private String npheStatus;

    @Column(name = "occupancy_factor")
    private BigDecimal occupancyFactor;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "property_number")
    private Long propertyNumber;

    @Column(name = "proration")
    private BigDecimal proration;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "secondary_response_status")
    private Integer secondaryResponseStatus;

    @Column(name = "state")
    private String state;

    @Column(name = "tax_code")
    private Integer taxCode;

    @Column(name = "tax_type")
    private String taxType;

    @Column(name = "tertiary_status")
    private Integer tertiaryStatus;

    @Column(name = "volume_number")
    private Integer volumeNumber;

    @Column(name = "zip_code")
    private Long zipCode;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Integer getApplicationYear() { return applicationYear; }
    public void setApplicationYear(Integer applicationYear) { this.applicationYear = applicationYear; }


    public Long getAssessedValue() { return assessedValue; }
    public void setAssessedValue(Long assessedValue) { this.assessedValue = assessedValue; }


    public Integer getAssessmentClass() { return assessmentClass; }
    public void setAssessmentClass(Integer assessmentClass) { this.assessmentClass = assessmentClass; }


    public Integer getCertificateOfErrorNumber() { return certificateOfErrorNumber; }
    public void setCertificateOfErrorNumber(Integer certificateOfErrorNumber) { this.certificateOfErrorNumber = certificateOfErrorNumber; }


    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }


    public Integer getClerksClass() { return clerksClass; }
    public void setClerksClass(Integer clerksClass) { this.clerksClass = clerksClass; }


    public Integer getCooperativeQuantity() { return cooperativeQuantity; }
    public void setCooperativeQuantity(Integer cooperativeQuantity) { this.cooperativeQuantity = cooperativeQuantity; }


    public BigDecimal getEqualizationFactor() { return equalizationFactor; }
    public void setEqualizationFactor(BigDecimal equalizationFactor) { this.equalizationFactor = equalizationFactor; }


    public Long getEqualizedValue() { return equalizedValue; }
    public void setEqualizedValue(Long equalizedValue) { this.equalizedValue = equalizedValue; }


    public Integer getExemptionType() { return exemptionType; }
    public void setExemptionType(Integer exemptionType) { this.exemptionType = exemptionType; }


    public String getMailingAddress() { return mailingAddress; }
    public void setMailingAddress(String mailingAddress) { this.mailingAddress = mailingAddress; }


    public Long getNpheAmount() { return npheAmount; }
    public void setNpheAmount(Long npheAmount) { this.npheAmount = npheAmount; }


    public Integer getNpheBaseYear() { return npheBaseYear; }
    public void setNpheBaseYear(Integer npheBaseYear) { this.npheBaseYear = npheBaseYear; }


    public String getNpheStatus() { return npheStatus; }
    public void setNpheStatus(String npheStatus) { this.npheStatus = npheStatus; }


    public BigDecimal getOccupancyFactor() { return occupancyFactor; }
    public void setOccupancyFactor(BigDecimal occupancyFactor) { this.occupancyFactor = occupancyFactor; }


    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }


    public Long getPropertyNumber() { return propertyNumber; }
    public void setPropertyNumber(Long propertyNumber) { this.propertyNumber = propertyNumber; }


    public BigDecimal getProration() { return proration; }
    public void setProration(BigDecimal proration) { this.proration = proration; }


    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }


    public Integer getSecondaryResponseStatus() { return secondaryResponseStatus; }
    public void setSecondaryResponseStatus(Integer secondaryResponseStatus) { this.secondaryResponseStatus = secondaryResponseStatus; }


    public String getState() { return state; }
    public void setState(String state) { this.state = state; }


    public Integer getTaxCode() { return taxCode; }
    public void setTaxCode(Integer taxCode) { this.taxCode = taxCode; }


    public String getTaxType() { return taxType; }
    public void setTaxType(String taxType) { this.taxType = taxType; }


    public Integer getTertiaryStatus() { return tertiaryStatus; }
    public void setTertiaryStatus(Integer tertiaryStatus) { this.tertiaryStatus = tertiaryStatus; }


    public Integer getVolumeNumber() { return volumeNumber; }
    public void setVolumeNumber(Integer volumeNumber) { this.volumeNumber = volumeNumber; }


    public Long getZipCode() { return zipCode; }
    public void setZipCode(Long zipCode) { this.zipCode = zipCode; }



    // GENERATED-ACCESSORS:end
}
