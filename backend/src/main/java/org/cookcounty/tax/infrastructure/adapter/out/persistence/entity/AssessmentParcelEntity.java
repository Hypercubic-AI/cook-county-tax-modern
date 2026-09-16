
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@Entity
@Table(name = "assessment_parcels")
public class AssessmentParcelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "archived_pre_conversion_proposed_total")
    private Long archivedPreConversionProposedTotal;

    @Column(name = "assessment_status")
    private String assessmentStatus;

    @Column(name = "clerk_major_class")
    private String clerkMajorClass;

    @Column(name = "combined_homeowner_non_homeowner_value")
    private Long combinedHomeownerNonHomeownerValue;

    @Column(name = "current_improvement_value")
    private Long currentImprovementValue;

    @Column(name = "current_land_value")
    private Long currentLandValue;

    @Column(name = "current_total_value")
    private Long currentTotalValue;

    @Column(name = "detail_questionnaire_count")
    private Integer detailQuestionnaireCount;

    @Column(name = "farm_value")
    private Long farmValue;

    @Column(name = "overall_class")
    private Integer overallClass;

    @Column(name = "parcel_number")
    private Long parcelNumber;

    @Column(name = "parcel_status")
    private String parcelStatus;

    @Column(name = "prior_improvement_value")
    private Long priorImprovementValue;

    @Column(name = "prior_land_value")
    private Long priorLandValue;

    @Column(name = "prior_total_value")
    private Long priorTotalValue;


    @Column(name = "proposed_improvement_value")
    private Long proposedImprovementValue;

    @Column(name = "proposed_land_value")
    private Long proposedLandValue;

    @Column(name = "proposed_total_value")
    private Long proposedTotalValue;

    @Column(name = "sales_segment_count")
    private Integer salesSegmentCount;

    @Column(name = "tax_code")
    private Integer taxCode;

    @Column(name = "tax_type")
    private String taxType;

    @Column(name = "volume_number")
    private Integer volumeNumber;



    // GENERATED-FIELDS:end

    @Column(name = "prior_parcel_status", insertable = false, updatable = false)
    private String priorParcelStatus;

    @Column(name = "eifd_prior_land_value", insertable = false, updatable = false)
    private Long eifdPriorLandValue;

    @Column(name = "eifd_prior_improvement_value", insertable = false, updatable = false)
    private Long eifdPriorImprovementValue;

    @Column(name = "eifd_prior_total_value", insertable = false, updatable = false)
    private Long eifdPriorTotalValue;

    @Column(name = "eifd_current_land_value", insertable = false, updatable = false)
    private Long eifdCurrentLandValue;

    @Column(name = "eifd_current_improvement_value", insertable = false, updatable = false)
    private Long eifdCurrentImprovementValue;

    @Column(name = "eifd_current_total_value", insertable = false, updatable = false)
    private Long eifdCurrentTotalValue;

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Long getArchivedPreConversionProposedTotal() { return archivedPreConversionProposedTotal; }
    public void setArchivedPreConversionProposedTotal(Long archivedPreConversionProposedTotal) { this.archivedPreConversionProposedTotal = archivedPreConversionProposedTotal; }


    public String getAssessmentStatus() { return assessmentStatus; }
    public void setAssessmentStatus(String assessmentStatus) { this.assessmentStatus = assessmentStatus; }


    public String getClerkMajorClass() { return clerkMajorClass; }
    public void setClerkMajorClass(String clerkMajorClass) { this.clerkMajorClass = clerkMajorClass; }


    public Long getCombinedHomeownerNonHomeownerValue() { return combinedHomeownerNonHomeownerValue; }
    public void setCombinedHomeownerNonHomeownerValue(Long combinedHomeownerNonHomeownerValue) { this.combinedHomeownerNonHomeownerValue = combinedHomeownerNonHomeownerValue; }


    public Long getCurrentImprovementValue() { return currentImprovementValue; }
    public void setCurrentImprovementValue(Long currentImprovementValue) { this.currentImprovementValue = currentImprovementValue; }


    public Long getCurrentLandValue() { return currentLandValue; }
    public void setCurrentLandValue(Long currentLandValue) { this.currentLandValue = currentLandValue; }


    public Long getCurrentTotalValue() { return currentTotalValue; }
    public void setCurrentTotalValue(Long currentTotalValue) { this.currentTotalValue = currentTotalValue; }


    public Integer getDetailQuestionnaireCount() { return detailQuestionnaireCount; }
    public void setDetailQuestionnaireCount(Integer detailQuestionnaireCount) { this.detailQuestionnaireCount = detailQuestionnaireCount; }


    public Long getFarmValue() { return farmValue; }
    public void setFarmValue(Long farmValue) { this.farmValue = farmValue; }


    public Integer getOverallClass() { return overallClass; }
    public void setOverallClass(Integer overallClass) { this.overallClass = overallClass; }


    public Long getParcelNumber() { return parcelNumber; }
    public void setParcelNumber(Long parcelNumber) { this.parcelNumber = parcelNumber; }


    public String getParcelStatus() { return parcelStatus; }
    public void setParcelStatus(String parcelStatus) { this.parcelStatus = parcelStatus; }


    public Long getPriorImprovementValue() { return priorImprovementValue; }
    public void setPriorImprovementValue(Long priorImprovementValue) { this.priorImprovementValue = priorImprovementValue; }


    public Long getPriorLandValue() { return priorLandValue; }
    public void setPriorLandValue(Long priorLandValue) { this.priorLandValue = priorLandValue; }


    public Long getPriorTotalValue() { return priorTotalValue; }
    public void setPriorTotalValue(Long priorTotalValue) { this.priorTotalValue = priorTotalValue; }



    public Long getProposedImprovementValue() { return proposedImprovementValue; }
    public void setProposedImprovementValue(Long proposedImprovementValue) { this.proposedImprovementValue = proposedImprovementValue; }


    public Long getProposedLandValue() { return proposedLandValue; }
    public void setProposedLandValue(Long proposedLandValue) { this.proposedLandValue = proposedLandValue; }


    public Long getProposedTotalValue() { return proposedTotalValue; }
    public void setProposedTotalValue(Long proposedTotalValue) { this.proposedTotalValue = proposedTotalValue; }


    public Integer getSalesSegmentCount() { return salesSegmentCount; }
    public void setSalesSegmentCount(Integer salesSegmentCount) { this.salesSegmentCount = salesSegmentCount; }


    public Integer getTaxCode() { return taxCode; }
    public void setTaxCode(Integer taxCode) { this.taxCode = taxCode; }


    public String getTaxType() { return taxType; }
    public void setTaxType(String taxType) { this.taxType = taxType; }


    public Integer getVolumeNumber() { return volumeNumber; }
    public void setVolumeNumber(Integer volumeNumber) { this.volumeNumber = volumeNumber; }



    // GENERATED-ACCESSORS:end

    public String getPriorParcelStatus() { return priorParcelStatus; }
    public void setPriorParcelStatus(String priorParcelStatus) { this.priorParcelStatus = priorParcelStatus; }

    public Long getEifdPriorLandValue() { return eifdPriorLandValue; }
    public Long getEifdPriorImprovementValue() { return eifdPriorImprovementValue; }
    public Long getEifdPriorTotalValue() { return eifdPriorTotalValue; }
    public Long getEifdCurrentLandValue() { return eifdCurrentLandValue; }
    public Long getEifdCurrentImprovementValue() { return eifdCurrentImprovementValue; }
    public Long getEifdCurrentTotalValue() { return eifdCurrentTotalValue; }
}
