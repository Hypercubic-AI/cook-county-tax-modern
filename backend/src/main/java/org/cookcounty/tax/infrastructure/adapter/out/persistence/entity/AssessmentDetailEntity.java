package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/// Mutable persistence boundary for one assessment detail.
///
/// Fields are nullable before JPA hydration. The canonical mapper establishes the complete domain
/// contract after hydration and preserves the optimistic-lock version on every save.
@Entity
@Table(name = "assessment_details")
public class AssessmentDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Version private @Nullable Long version;

    @Column(name = "age")
    private @Nullable Integer age;

    @Column(name = "area")
    private @Nullable Long area;

    @Column(name = "assessment_class")
    private @Nullable Integer assessmentClass;

    @Column(name = "supplemental_detail_code")
    private @Nullable String supplementalDetailCode;

    @Column(name = "condition_factor", precision = 3, scale = 1)
    private @Nullable BigDecimal conditionFactor;

    @Column(name = "corner_factor", precision = 5, scale = 4)
    private @Nullable BigDecimal cornerFactor;

    @Column(name = "decimal_scale")
    private @Nullable Integer decimalScale;

    @Column(name = "depth")
    private @Nullable Long depth;

    @Column(name = "depth_factor", precision = 5, scale = 3)
    private @Nullable BigDecimal depthFactor;

    @Column(name = "detail_code")
    private @Nullable String detailCode;

    @Column(name = "detail_type")
    private @Nullable String detailType;

    @Column(name = "extra_corner_factor", precision = 5, scale = 5)
    private @Nullable BigDecimal extraCornerFactor;

    @Column(name = "front_footage")
    private @Nullable Long frontFootage;

    @Column(name = "improvement_year")
    private @Nullable Integer improvementYear;

    @Column(name = "key_parcel_number", length = 16)
    private @Nullable String keyParcelNumber;

    @Column(name = "land_condition_factor", precision = 3, scale = 1)
    private @Nullable BigDecimal landConditionFactor;

    @Column(name = "multicode")
    private @Nullable Integer multicode;

    @Column(name = "occupancy_factor", precision = 3, scale = 1)
    private @Nullable BigDecimal occupancyFactor;

    @Column(name = "occurrence_number")
    private @Nullable Integer occurrenceNumber;

    @Column(name = "parcel_number", length = 16)
    private @Nullable String parcelNumber;

    @Column(name = "parcel_volume_number", length = 4)
    private @Nullable String parcelVolumeNumber;

    @Column(name = "percent_assessed", precision = 7, scale = 5)
    private @Nullable BigDecimal percentAssessed;

    @Column(name = "reproduction_cost", precision = 9, scale = 0)
    private @Nullable BigDecimal reproductionCost;

    @Column(name = "split_code")
    private @Nullable String splitCode;

    @Column(name = "unit_measure")
    private @Nullable String unitMeasure;

    @Column(name = "unit_price", precision = 7, scale = 2)
    private @Nullable BigDecimal unitPrice;

    @Column(name = "valuation", precision = 9, scale = 0)
    private @Nullable BigDecimal valuation;

    /// Creates an unhydrated entity for JPA or the canonical mapper.
    public AssessmentDetailEntity() {}

    /// Returns the generated persistence identity, or `null` before hydration or when the source
    /// layout omits it.
    public @Nullable Long getId() {
        return id;
    }

    /// Sets the generated persistence identity during hydration or complete domain mapping.
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /// Returns the optimistic-lock version, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Sets the optimistic-lock version during hydration or complete domain mapping.
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    /// Returns the improvement age, or `null` before hydration or when the source layout omits it.
    public @Nullable Integer getAge() {
        return age;
    }

    /// Sets the improvement age during hydration or complete domain mapping.
    public void setAge(@Nullable Integer age) {
        this.age = age;
    }

    /// Returns the encoded improvement area, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable Long getArea() {
        return area;
    }

    /// Sets the encoded improvement area during hydration or complete domain mapping.
    public void setArea(@Nullable Long area) {
        this.area = area;
    }

    /// Returns the Assessor class, or `null` before hydration or when the source layout omits it.
    public @Nullable Integer getAssessmentClass() {
        return assessmentClass;
    }

    /// Sets the Assessor class during hydration or complete domain mapping.
    public void setAssessmentClass(@Nullable Integer assessmentClass) {
        this.assessmentClass = assessmentClass;
    }

    /// Returns the observed two-character detail code.
    ///
    /// The value is `null` before hydration or when the selected detail layout omits the field.
    public @Nullable String getSupplementalDetailCode() {
        return supplementalDetailCode;
    }

    /// Sets the observed two-character detail code during hydration or complete domain mapping.
    ///
    /// `GR` identifies the garage check. Type-5 conversion clears `YR`.
    public void setSupplementalDetailCode(@Nullable String supplementalDetailCode) {
        this.supplementalDetailCode = supplementalDetailCode;
    }

    /// Returns the percentage condition factor, or `null` before hydration or when the source
    /// layout omits it.
    public @Nullable BigDecimal getConditionFactor() {
        return conditionFactor;
    }

    /// Sets the percentage condition factor during hydration or complete domain mapping.
    public void setConditionFactor(@Nullable BigDecimal conditionFactor) {
        this.conditionFactor = conditionFactor;
    }

    /// Returns the land corner factor, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable BigDecimal getCornerFactor() {
        return cornerFactor;
    }

    /// Sets the land corner factor during hydration or complete domain mapping.
    public void setCornerFactor(@Nullable BigDecimal cornerFactor) {
        this.cornerFactor = cornerFactor;
    }

    /// Returns the encoded decimal places, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable Integer getDecimalScale() {
        return decimalScale;
    }

    /// Sets the encoded decimal places during hydration or complete domain mapping.
    public void setDecimalScale(@Nullable Integer decimalScale) {
        this.decimalScale = decimalScale;
    }

    /// Returns the encoded land depth, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable Long getDepth() {
        return depth;
    }

    /// Sets the encoded land depth during hydration or complete domain mapping.
    public void setDepth(@Nullable Long depth) {
        this.depth = depth;
    }

    /// Returns the land depth factor, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable BigDecimal getDepthFactor() {
        return depthFactor;
    }

    /// Sets the land depth factor during hydration or complete domain mapping.
    public void setDepthFactor(@Nullable BigDecimal depthFactor) {
        this.depthFactor = depthFactor;
    }

    /// Returns the detail subtype code, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable String getDetailCode() {
        return detailCode;
    }

    /// Sets the detail subtype code during hydration or complete domain mapping.
    public void setDetailCode(@Nullable String detailCode) {
        this.detailCode = detailCode;
    }

    /// Returns the detail layout selector, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable String getDetailType() {
        return detailType;
    }

    /// Sets the detail layout selector during hydration or complete domain mapping.
    public void setDetailType(@Nullable String detailType) {
        this.detailType = detailType;
    }

    /// Returns the extra-corner factor, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable BigDecimal getExtraCornerFactor() {
        return extraCornerFactor;
    }

    /// Sets the extra-corner factor during hydration or complete domain mapping.
    public void setExtraCornerFactor(@Nullable BigDecimal extraCornerFactor) {
        this.extraCornerFactor = extraCornerFactor;
    }

    /// Returns the encoded land frontage, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable Long getFrontFootage() {
        return frontFootage;
    }

    /// Sets the encoded land frontage during hydration or complete domain mapping.
    public void setFrontFootage(@Nullable Long frontFootage) {
        this.frontFootage = frontFootage;
    }

    /// Returns the two-digit improvement year, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable Integer getImprovementYear() {
        return improvementYear;
    }

    /// Sets the two-digit improvement year during hydration or complete domain mapping.
    public void setImprovementYear(@Nullable Integer improvementYear) {
        this.improvementYear = improvementYear;
    }

    /// Returns the related parcel identifier, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable String getKeyParcelNumber() {
        return keyParcelNumber;
    }

    /// Sets the related parcel identifier during hydration or complete domain mapping.
    public void setKeyParcelNumber(@Nullable String keyParcelNumber) {
        this.keyParcelNumber = keyParcelNumber;
    }

    /// Returns the land-condition percentage, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable BigDecimal getLandConditionFactor() {
        return landConditionFactor;
    }

    /// Sets the land-condition percentage during hydration or complete domain mapping.
    public void setLandConditionFactor(@Nullable BigDecimal landConditionFactor) {
        this.landConditionFactor = landConditionFactor;
    }

    /// Returns the source multicode, or `null` before hydration or when the source layout omits it.
    public @Nullable Integer getMulticode() {
        return multicode;
    }

    /// Sets the source multicode during hydration or complete domain mapping.
    public void setMulticode(@Nullable Integer multicode) {
        this.multicode = multicode;
    }

    /// Returns the Type-5 occupancy percentage, or `null` before hydration or when the source
    /// layout omits it.
    public @Nullable BigDecimal getOccupancyFactor() {
        return occupancyFactor;
    }

    /// Sets the Type-5 occupancy percentage during hydration or complete domain mapping.
    public void setOccupancyFactor(@Nullable BigDecimal occupancyFactor) {
        this.occupancyFactor = occupancyFactor;
    }

    /// Returns the source occurrence order, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable Integer getOccurrenceNumber() {
        return occurrenceNumber;
    }

    /// Sets the source occurrence order during hydration or complete domain mapping.
    public void setOccurrenceNumber(@Nullable Integer occurrenceNumber) {
        this.occurrenceNumber = occurrenceNumber;
    }

    /// Returns the owning parcel identifier, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable String getParcelNumber() {
        return parcelNumber;
    }

    /// Sets the owning parcel identifier during hydration or complete domain mapping.
    public void setParcelNumber(@Nullable String parcelNumber) {
        this.parcelNumber = parcelNumber;
    }

    /// Returns the owning parcel volume, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable String getParcelVolumeNumber() {
        return parcelVolumeNumber;
    }

    /// Sets the owning parcel volume during hydration or complete domain mapping.
    public void setParcelVolumeNumber(@Nullable String parcelVolumeNumber) {
        this.parcelVolumeNumber = parcelVolumeNumber;
    }

    /// Returns the assessed percentage, or `null` before hydration or when the source layout omits
    /// it.
    public @Nullable BigDecimal getPercentAssessed() {
        return percentAssessed;
    }

    /// Sets the assessed percentage during hydration or complete domain mapping.
    public void setPercentAssessed(@Nullable BigDecimal percentAssessed) {
        this.percentAssessed = percentAssessed;
    }

    /// Returns the whole-dollar reproduction cost, or `null` before hydration or when the source
    /// layout omits it.
    public @Nullable BigDecimal getReproductionCost() {
        return reproductionCost;
    }

    /// Sets the whole-dollar reproduction cost during hydration or complete domain mapping.
    public void setReproductionCost(@Nullable BigDecimal reproductionCost) {
        this.reproductionCost = reproductionCost;
    }

    /// Returns the source split indicator, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable String getSplitCode() {
        return splitCode;
    }

    /// Sets the source split indicator during hydration or complete domain mapping.
    public void setSplitCode(@Nullable String splitCode) {
        this.splitCode = splitCode;
    }

    /// Returns the land unit code, or `null` before hydration or when the source layout omits it.
    public @Nullable String getUnitMeasure() {
        return unitMeasure;
    }

    /// Sets the land unit code during hydration or complete domain mapping.
    public void setUnitMeasure(@Nullable String unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    /// Returns the price per decoded unit, or `null` before hydration or when the source layout
    /// omits it.
    public @Nullable BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /// Sets the price per decoded unit during hydration or complete domain mapping.
    public void setUnitPrice(@Nullable BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /// Returns the whole-dollar assessed value, or `null` before hydration or when the source
    /// layout omits it.
    public @Nullable BigDecimal getValuation() {
        return valuation;
    }

    /// Sets the whole-dollar assessed value during hydration or complete domain mapping.
    public void setValuation(@Nullable BigDecimal valuation) {
        this.valuation = valuation;
    }
}
