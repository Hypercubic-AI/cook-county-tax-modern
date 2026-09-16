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

/// Mutable persistence boundary for one assessment parcel.
///
/// Fields are nullable before JPA hydration. The canonical mapper establishes the complete domain
/// contract after hydration and preserves all twelve valuation slots and the optimistic-lock
/// version.
@Entity
@Table(name = "assessment_parcels")
public class AssessmentParcelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Version private @Nullable Long version;

    @Column(name = "archived_pre_conversion_proposed_total", precision = 9, scale = 0)
    private @Nullable BigDecimal archivedPreConversionProposedTotal;

    @Column(name = "assessment_status")
    private @Nullable String assessmentStatus;

    @Column(name = "clerk_major_class")
    private @Nullable String clerkMajorClass;

    @Column(name = "combined_homeowner_non_homeowner_value", precision = 9, scale = 0)
    private @Nullable BigDecimal combinedHomeownerNonHomeownerValue;

    @Column(name = "current_improvement_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentImprovementValue;

    @Column(name = "current_land_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentLandValue;

    @Column(name = "current_total_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentTotalValue;

    @Column(name = "detail_questionnaire_count")
    private @Nullable Integer detailQuestionnaireCount;

    @Column(name = "farm_value", precision = 9, scale = 0)
    private @Nullable BigDecimal farmValue;

    @Column(name = "overall_class")
    private @Nullable Integer overallClass;

    @Column(name = "parcel_number", length = 16)
    private @Nullable String parcelNumber;

    @Column(name = "parcel_status")
    private @Nullable String parcelStatus;

    @Column(name = "prior_improvement_value", precision = 9, scale = 0)
    private @Nullable BigDecimal priorImprovementValue;

    @Column(name = "prior_land_value", precision = 9, scale = 0)
    private @Nullable BigDecimal priorLandValue;

    @Column(name = "prior_total_value", precision = 9, scale = 0)
    private @Nullable BigDecimal priorTotalValue;

    @Column(name = "proposed_improvement_value", precision = 9, scale = 0)
    private @Nullable BigDecimal proposedImprovementValue;

    @Column(name = "proposed_land_value", precision = 9, scale = 0)
    private @Nullable BigDecimal proposedLandValue;

    @Column(name = "proposed_total_value", precision = 9, scale = 0)
    private @Nullable BigDecimal proposedTotalValue;

    @Column(name = "sales_segment_count")
    private @Nullable Integer salesSegmentCount;

    @Column(name = "tax_code", length = 6)
    private @Nullable String taxCode;

    @Column(name = "tax_type")
    private @Nullable String taxType;

    @Column(name = "volume_number", length = 4)
    private @Nullable String volumeNumber;

    @Column(name = "prior_parcel_status", insertable = false, updatable = false)
    private @Nullable String priorParcelStatus;

    @Column(name = "property_division_number", length = 14, insertable = false, updatable = false)
    private @Nullable String propertyDivisionNumber;

    @Column(name = "eifd_prior_land_value", precision = 9, scale = 0)
    private @Nullable BigDecimal eifdPriorLandValue;

    @Column(name = "eifd_prior_improvement_value", precision = 9, scale = 0)
    private @Nullable BigDecimal eifdPriorImprovementValue;

    @Column(name = "eifd_prior_total_value", precision = 9, scale = 0)
    private @Nullable BigDecimal eifdPriorTotalValue;

    @Column(name = "eifd_current_land_value", precision = 9, scale = 0)
    private @Nullable BigDecimal eifdCurrentLandValue;

    @Column(name = "eifd_current_improvement_value", precision = 9, scale = 0)
    private @Nullable BigDecimal eifdCurrentImprovementValue;

    @Column(name = "eifd_current_total_value", precision = 9, scale = 0)
    private @Nullable BigDecimal eifdCurrentTotalValue;

    /// Creates an unhydrated entity for JPA or the canonical mapper.
    public AssessmentParcelEntity() {}

    /// Returns the generated persistence identity, or `null` before hydration or when its input is
    /// absent.
    public @Nullable Long getId() {
        return id;
    }

    /// Sets the generated persistence identity during hydration or complete domain mapping.
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /// Returns the optimistic-lock version, or `null` before hydration or when its input is absent.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Sets the optimistic-lock version during hydration or complete domain mapping.
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    /// Returns the archived pre-conversion proposed total, or `null` before hydration or when its
    /// input is absent.
    public @Nullable BigDecimal getArchivedPreConversionProposedTotal() {
        return archivedPreConversionProposedTotal;
    }

    /// Sets the archived pre-conversion proposed total during hydration or complete domain mapping.
    public void setArchivedPreConversionProposedTotal(
            @Nullable BigDecimal archivedPreConversionProposedTotal) {
        this.archivedPreConversionProposedTotal = archivedPreConversionProposedTotal;
    }

    /// Returns the assessment status, or `null` before hydration or when its input is absent.
    public @Nullable String getAssessmentStatus() {
        return assessmentStatus;
    }

    /// Sets the assessment status during hydration or complete domain mapping.
    public void setAssessmentStatus(@Nullable String assessmentStatus) {
        this.assessmentStatus = assessmentStatus;
    }

    /// Returns the Clerk major class, or `null` before hydration or when its input is absent.
    public @Nullable String getClerkMajorClass() {
        return clerkMajorClass;
    }

    /// Sets the Clerk major class during hydration or complete domain mapping.
    public void setClerkMajorClass(@Nullable String clerkMajorClass) {
        this.clerkMajorClass = clerkMajorClass;
    }

    /// Returns the combined homeowner and non-homeowner value, or `null` before hydration or when
    /// its input is absent.
    public @Nullable BigDecimal getCombinedHomeownerNonHomeownerValue() {
        return combinedHomeownerNonHomeownerValue;
    }

    /// Sets the combined homeowner and non-homeowner value during hydration or complete domain
    /// mapping.
    public void setCombinedHomeownerNonHomeownerValue(
            @Nullable BigDecimal combinedHomeownerNonHomeownerValue) {
        this.combinedHomeownerNonHomeownerValue = combinedHomeownerNonHomeownerValue;
    }

    /// Returns the current improvement value, or `null` before hydration or when its input is
    /// absent.
    public @Nullable BigDecimal getCurrentImprovementValue() {
        return currentImprovementValue;
    }

    /// Sets the current improvement value during hydration or complete domain mapping.
    public void setCurrentImprovementValue(@Nullable BigDecimal currentImprovementValue) {
        this.currentImprovementValue = currentImprovementValue;
    }

    /// Returns the current land value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getCurrentLandValue() {
        return currentLandValue;
    }

    /// Sets the current land value during hydration or complete domain mapping.
    public void setCurrentLandValue(@Nullable BigDecimal currentLandValue) {
        this.currentLandValue = currentLandValue;
    }

    /// Returns the current total value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getCurrentTotalValue() {
        return currentTotalValue;
    }

    /// Sets the current total value during hydration or complete domain mapping.
    public void setCurrentTotalValue(@Nullable BigDecimal currentTotalValue) {
        this.currentTotalValue = currentTotalValue;
    }

    /// Returns the detail and questionnaire count, or `null` before hydration or when its input is
    /// absent.
    public @Nullable Integer getDetailQuestionnaireCount() {
        return detailQuestionnaireCount;
    }

    /// Sets the detail and questionnaire count during hydration or complete domain mapping.
    public void setDetailQuestionnaireCount(@Nullable Integer detailQuestionnaireCount) {
        this.detailQuestionnaireCount = detailQuestionnaireCount;
    }

    /// Returns the farm value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getFarmValue() {
        return farmValue;
    }

    /// Sets the farm value during hydration or complete domain mapping.
    public void setFarmValue(@Nullable BigDecimal farmValue) {
        this.farmValue = farmValue;
    }

    /// Returns the selected overall class, or `null` before hydration or when its input is absent.
    public @Nullable Integer getOverallClass() {
        return overallClass;
    }

    /// Sets the selected overall class during hydration or complete domain mapping.
    public void setOverallClass(@Nullable Integer overallClass) {
        this.overallClass = overallClass;
    }

    /// Returns the parcel identifier, or `null` before hydration or when its input is absent.
    public @Nullable String getParcelNumber() {
        return parcelNumber;
    }

    /// Sets the parcel identifier during hydration or complete domain mapping.
    public void setParcelNumber(@Nullable String parcelNumber) {
        this.parcelNumber = parcelNumber;
    }

    /// Returns the parcel status, or `null` before hydration or when its input is absent.
    public @Nullable String getParcelStatus() {
        return parcelStatus;
    }

    /// Sets the parcel status during hydration or complete domain mapping.
    public void setParcelStatus(@Nullable String parcelStatus) {
        this.parcelStatus = parcelStatus;
    }

    /// Returns the prior improvement value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getPriorImprovementValue() {
        return priorImprovementValue;
    }

    /// Sets the prior improvement value during hydration or complete domain mapping.
    public void setPriorImprovementValue(@Nullable BigDecimal priorImprovementValue) {
        this.priorImprovementValue = priorImprovementValue;
    }

    /// Returns the prior land value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getPriorLandValue() {
        return priorLandValue;
    }

    /// Sets the prior land value during hydration or complete domain mapping.
    public void setPriorLandValue(@Nullable BigDecimal priorLandValue) {
        this.priorLandValue = priorLandValue;
    }

    /// Returns the prior total value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getPriorTotalValue() {
        return priorTotalValue;
    }

    /// Sets the prior total value during hydration or complete domain mapping.
    public void setPriorTotalValue(@Nullable BigDecimal priorTotalValue) {
        this.priorTotalValue = priorTotalValue;
    }

    /// Returns the proposed improvement value, or `null` before hydration or when its input is
    /// absent.
    public @Nullable BigDecimal getProposedImprovementValue() {
        return proposedImprovementValue;
    }

    /// Sets the proposed improvement value during hydration or complete domain mapping.
    public void setProposedImprovementValue(@Nullable BigDecimal proposedImprovementValue) {
        this.proposedImprovementValue = proposedImprovementValue;
    }

    /// Returns the proposed land value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getProposedLandValue() {
        return proposedLandValue;
    }

    /// Sets the proposed land value during hydration or complete domain mapping.
    public void setProposedLandValue(@Nullable BigDecimal proposedLandValue) {
        this.proposedLandValue = proposedLandValue;
    }

    /// Returns the proposed total value, or `null` before hydration or when its input is absent.
    public @Nullable BigDecimal getProposedTotalValue() {
        return proposedTotalValue;
    }

    /// Sets the proposed total value during hydration or complete domain mapping.
    public void setProposedTotalValue(@Nullable BigDecimal proposedTotalValue) {
        this.proposedTotalValue = proposedTotalValue;
    }

    /// Returns the embedded sale count, or `null` before hydration or when its input is absent.
    public @Nullable Integer getSalesSegmentCount() {
        return salesSegmentCount;
    }

    /// Sets the embedded sale count during hydration or complete domain mapping.
    public void setSalesSegmentCount(@Nullable Integer salesSegmentCount) {
        this.salesSegmentCount = salesSegmentCount;
    }

    /// Returns the tax code, or `null` before hydration or when its input is absent.
    public @Nullable String getTaxCode() {
        return taxCode;
    }

    /// Sets the tax code during hydration or complete domain mapping.
    public void setTaxCode(@Nullable String taxCode) {
        this.taxCode = taxCode;
    }

    /// Returns the tax type, or `null` before hydration or when its input is absent.
    public @Nullable String getTaxType() {
        return taxType;
    }

    /// Sets the tax type during hydration or complete domain mapping.
    public void setTaxType(@Nullable String taxType) {
        this.taxType = taxType;
    }

    /// Returns the volume number, or `null` before hydration or when its input is absent.
    public @Nullable String getVolumeNumber() {
        return volumeNumber;
    }

    /// Sets the volume number during hydration or complete domain mapping.
    public void setVolumeNumber(@Nullable String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }

    /// Returns the prior increment-input parcel status, or `null` before hydration or when its
    /// input is absent.
    public @Nullable String getPriorParcelStatus() {
        return priorParcelStatus;
    }

    /// Sets the prior increment-input parcel status during hydration or complete domain mapping.
    public void setPriorParcelStatus(@Nullable String priorParcelStatus) {
        this.priorParcelStatus = priorParcelStatus;
    }

    /// Returns the increment-input division key, or `null` before hydration or when its input is
    /// absent.
    public @Nullable String getPropertyDivisionNumber() {
        return propertyDivisionNumber;
    }

    /// Sets the increment-input division key during hydration or complete domain mapping.
    public void setPropertyDivisionNumber(@Nullable String propertyDivisionNumber) {
        this.propertyDivisionNumber = propertyDivisionNumber;
    }

    /// Returns the increment-input prior land value, or `null` before hydration or when its input
    /// is absent.
    public @Nullable BigDecimal getEifdPriorLandValue() {
        return eifdPriorLandValue;
    }

    /// Sets the increment-input prior land value during hydration or complete domain mapping.
    public void setEifdPriorLandValue(@Nullable BigDecimal eifdPriorLandValue) {
        this.eifdPriorLandValue = eifdPriorLandValue;
    }

    /// Returns the increment-input prior improvement value, or `null` before hydration or when its
    /// input is absent.
    public @Nullable BigDecimal getEifdPriorImprovementValue() {
        return eifdPriorImprovementValue;
    }

    /// Sets the increment-input prior improvement value during hydration or complete domain
    /// mapping.
    public void setEifdPriorImprovementValue(@Nullable BigDecimal eifdPriorImprovementValue) {
        this.eifdPriorImprovementValue = eifdPriorImprovementValue;
    }

    /// Returns the increment-input prior total value, or `null` before hydration or when its input
    /// is absent.
    public @Nullable BigDecimal getEifdPriorTotalValue() {
        return eifdPriorTotalValue;
    }

    /// Sets the increment-input prior total value during hydration or complete domain mapping.
    public void setEifdPriorTotalValue(@Nullable BigDecimal eifdPriorTotalValue) {
        this.eifdPriorTotalValue = eifdPriorTotalValue;
    }

    /// Returns the increment-input current land value, or `null` before hydration or when its input
    /// is absent.
    public @Nullable BigDecimal getEifdCurrentLandValue() {
        return eifdCurrentLandValue;
    }

    /// Sets the increment-input current land value during hydration or complete domain mapping.
    public void setEifdCurrentLandValue(@Nullable BigDecimal eifdCurrentLandValue) {
        this.eifdCurrentLandValue = eifdCurrentLandValue;
    }

    /// Returns the increment-input current improvement value, or `null` before hydration or when
    /// its input is absent.
    public @Nullable BigDecimal getEifdCurrentImprovementValue() {
        return eifdCurrentImprovementValue;
    }

    /// Sets the increment-input current improvement value during hydration or complete domain
    /// mapping.
    public void setEifdCurrentImprovementValue(@Nullable BigDecimal eifdCurrentImprovementValue) {
        this.eifdCurrentImprovementValue = eifdCurrentImprovementValue;
    }

    /// Returns the increment-input current total value, or `null` before hydration or when its
    /// input is absent.
    public @Nullable BigDecimal getEifdCurrentTotalValue() {
        return eifdCurrentTotalValue;
    }

    /// Sets the increment-input current total value during hydration or complete domain mapping.
    public void setEifdCurrentTotalValue(@Nullable BigDecimal eifdCurrentTotalValue) {
        this.eifdCurrentTotalValue = eifdCurrentTotalValue;
    }
}
