
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

import java.math.BigDecimal;

// GENERATED-IMPORTS:end

@Entity
@Table(name = "assessment_details")
public class AssessmentDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "age")
    private Integer age;

    @Column(name = "area")
    private Long area;

    @Column(name = "assessment_class")
    private Integer assessmentClass;

    @Column(name = "cdu")
    private String cdu;

    @Column(name = "condition_factor")
    private BigDecimal conditionFactor;

    @Column(name = "corner_factor")
    private BigDecimal cornerFactor;

    @Column(name = "decimal_scale")
    private Integer decimalScale;

    @Column(name = "depth")
    private Long depth;

    @Column(name = "depth_factor")
    private BigDecimal depthFactor;

    @Column(name = "detail_code")
    private String detailCode;

    @Column(name = "detail_type")
    private String detailType;

    @Column(name = "extra_corner_factor")
    private BigDecimal extraCornerFactor;

    @Column(name = "front_footage")
    private Long frontFootage;

    @Column(name = "improvement_year")
    private Integer improvementYear;

    @Column(name = "key_parcel_number")
    private Long keyParcelNumber;

    @Column(name = "land_condition_factor")
    private BigDecimal landConditionFactor;

    @Column(name = "multicode")
    private Integer multicode;

    @Column(name = "occupancy_factor")
    private BigDecimal occupancyFactor;

    @Column(name = "occurrence_number")
    private Integer occurrenceNumber;

    @Column(name = "parcel_number")
    private Long parcelNumber;

    @Column(name = "parcel_volume_number")
    private Integer parcelVolumeNumber;

    @Column(name = "percent_assessed")
    private BigDecimal percentAssessed;

    @Column(name = "reproduction_cost")
    private Long reproductionCost;

    @Column(name = "split_code")
    private String splitCode;

    @Column(name = "unit_measure")
    private String unitMeasure;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "valuation")
    private Long valuation;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }


    public Long getArea() { return area; }
    public void setArea(Long area) { this.area = area; }


    public Integer getAssessmentClass() { return assessmentClass; }
    public void setAssessmentClass(Integer assessmentClass) { this.assessmentClass = assessmentClass; }


    public String getCdu() { return cdu; }
    public void setCdu(String cdu) { this.cdu = cdu; }


    public BigDecimal getConditionFactor() { return conditionFactor; }
    public void setConditionFactor(BigDecimal conditionFactor) { this.conditionFactor = conditionFactor; }


    public BigDecimal getCornerFactor() { return cornerFactor; }
    public void setCornerFactor(BigDecimal cornerFactor) { this.cornerFactor = cornerFactor; }


    public Integer getDecimalScale() { return decimalScale; }
    public void setDecimalScale(Integer decimalScale) { this.decimalScale = decimalScale; }


    public Long getDepth() { return depth; }
    public void setDepth(Long depth) { this.depth = depth; }


    public BigDecimal getDepthFactor() { return depthFactor; }
    public void setDepthFactor(BigDecimal depthFactor) { this.depthFactor = depthFactor; }


    public String getDetailCode() { return detailCode; }
    public void setDetailCode(String detailCode) { this.detailCode = detailCode; }


    public String getDetailType() { return detailType; }
    public void setDetailType(String detailType) { this.detailType = detailType; }


    public BigDecimal getExtraCornerFactor() { return extraCornerFactor; }
    public void setExtraCornerFactor(BigDecimal extraCornerFactor) { this.extraCornerFactor = extraCornerFactor; }


    public Long getFrontFootage() { return frontFootage; }
    public void setFrontFootage(Long frontFootage) { this.frontFootage = frontFootage; }


    public Integer getImprovementYear() { return improvementYear; }
    public void setImprovementYear(Integer improvementYear) { this.improvementYear = improvementYear; }


    public Long getKeyParcelNumber() { return keyParcelNumber; }
    public void setKeyParcelNumber(Long keyParcelNumber) { this.keyParcelNumber = keyParcelNumber; }


    public BigDecimal getLandConditionFactor() { return landConditionFactor; }
    public void setLandConditionFactor(BigDecimal landConditionFactor) { this.landConditionFactor = landConditionFactor; }


    public Integer getMulticode() { return multicode; }
    public void setMulticode(Integer multicode) { this.multicode = multicode; }


    public BigDecimal getOccupancyFactor() { return occupancyFactor; }
    public void setOccupancyFactor(BigDecimal occupancyFactor) { this.occupancyFactor = occupancyFactor; }


    public Integer getOccurrenceNumber() { return occurrenceNumber; }
    public void setOccurrenceNumber(Integer occurrenceNumber) { this.occurrenceNumber = occurrenceNumber; }


    public Long getParcelNumber() { return parcelNumber; }
    public void setParcelNumber(Long parcelNumber) { this.parcelNumber = parcelNumber; }


    public Integer getParcelVolumeNumber() { return parcelVolumeNumber; }
    public void setParcelVolumeNumber(Integer parcelVolumeNumber) { this.parcelVolumeNumber = parcelVolumeNumber; }


    public BigDecimal getPercentAssessed() { return percentAssessed; }
    public void setPercentAssessed(BigDecimal percentAssessed) { this.percentAssessed = percentAssessed; }


    public Long getReproductionCost() { return reproductionCost; }
    public void setReproductionCost(Long reproductionCost) { this.reproductionCost = reproductionCost; }


    public String getSplitCode() { return splitCode; }
    public void setSplitCode(String splitCode) { this.splitCode = splitCode; }


    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }


    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }


    public Long getValuation() { return valuation; }
    public void setValuation(Long valuation) { this.valuation = valuation; }



    // GENERATED-ACCESSORS:end
}
