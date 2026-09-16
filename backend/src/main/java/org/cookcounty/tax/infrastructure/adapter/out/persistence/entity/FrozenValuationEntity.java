
package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;


// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@Entity
@Table(name = "frozen_valuations")
public class FrozenValuationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GENERATED-FIELDS:start

    @Column(name = "change_action_current_improvement_value")
    private Long changeActionCurrentImprovementValue;

    @Column(name = "change_action_current_land_value")
    private Long changeActionCurrentLandValue;

    @Column(name = "change_action_current_parcel_count")
    private Long changeActionCurrentParcelCount;

    @Column(name = "change_action_current_total_value")
    private Long changeActionCurrentTotalValue;

    @Column(name = "change_action_prior_improvement_value")
    private Long changeActionPriorImprovementValue;

    @Column(name = "change_action_prior_land_value")
    private Long changeActionPriorLandValue;

    @Column(name = "change_action_prior_parcel_count")
    private Long changeActionPriorParcelCount;

    @Column(name = "change_action_prior_total_value")
    private Long changeActionPriorTotalValue;

    @Column(name = "current_improvement_value")
    private Long currentImprovementValue;

    @Column(name = "current_land_value")
    private Long currentLandValue;

    @Column(name = "current_parcel_count")
    private Long currentParcelCount;

    @Column(name = "current_total_value")
    private Long currentTotalValue;

    @Column(name = "division_number", unique = true)
    private String divisionNumber;

    @Column(name = "no_change_action_current_improvement_value")
    private Long noChangeActionCurrentImprovementValue;

    @Column(name = "no_change_action_current_land_value")
    private Long noChangeActionCurrentLandValue;

    @Column(name = "no_change_action_current_parcel_count")
    private Long noChangeActionCurrentParcelCount;

    @Column(name = "no_change_action_current_total_value")
    private Long noChangeActionCurrentTotalValue;

    @Column(name = "no_change_action_prior_improvement_value")
    private Long noChangeActionPriorImprovementValue;

    @Column(name = "no_change_action_prior_land_value")
    private Long noChangeActionPriorLandValue;

    @Column(name = "no_change_action_prior_parcel_count")
    private Long noChangeActionPriorParcelCount;

    @Column(name = "no_change_action_prior_total_value")
    private Long noChangeActionPriorTotalValue;

    @Column(name = "prior_improvement_value")
    private Long priorImprovementValue;

    @Column(name = "prior_land_value")
    private Long priorLandValue;

    @Column(name = "prior_parcel_count")
    private Long priorParcelCount;

    @Column(name = "prior_total_value")
    private Long priorTotalValue;

    @Column(name = "proposed_actual_value")
    private Long proposedActualValue;

    @Column(name = "proposed_current288_value")
    private Long proposedCurrent288Value;

    @Column(name = "proposed_expired288_value")
    private Long proposedExpired288Value;

    @Column(name = "proposed_improvement_value")
    private Long proposedImprovementValue;

    @Column(name = "proposed_total_value")
    private Long proposedTotalValue;



    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Long getChangeActionCurrentImprovementValue() { return changeActionCurrentImprovementValue; }
    public void setChangeActionCurrentImprovementValue(Long changeActionCurrentImprovementValue) { this.changeActionCurrentImprovementValue = changeActionCurrentImprovementValue; }


    public Long getChangeActionCurrentLandValue() { return changeActionCurrentLandValue; }
    public void setChangeActionCurrentLandValue(Long changeActionCurrentLandValue) { this.changeActionCurrentLandValue = changeActionCurrentLandValue; }


    public Long getChangeActionCurrentParcelCount() { return changeActionCurrentParcelCount; }
    public void setChangeActionCurrentParcelCount(Long changeActionCurrentParcelCount) { this.changeActionCurrentParcelCount = changeActionCurrentParcelCount; }


    public Long getChangeActionCurrentTotalValue() { return changeActionCurrentTotalValue; }
    public void setChangeActionCurrentTotalValue(Long changeActionCurrentTotalValue) { this.changeActionCurrentTotalValue = changeActionCurrentTotalValue; }


    public Long getChangeActionPriorImprovementValue() { return changeActionPriorImprovementValue; }
    public void setChangeActionPriorImprovementValue(Long changeActionPriorImprovementValue) { this.changeActionPriorImprovementValue = changeActionPriorImprovementValue; }


    public Long getChangeActionPriorLandValue() { return changeActionPriorLandValue; }
    public void setChangeActionPriorLandValue(Long changeActionPriorLandValue) { this.changeActionPriorLandValue = changeActionPriorLandValue; }


    public Long getChangeActionPriorParcelCount() { return changeActionPriorParcelCount; }
    public void setChangeActionPriorParcelCount(Long changeActionPriorParcelCount) { this.changeActionPriorParcelCount = changeActionPriorParcelCount; }


    public Long getChangeActionPriorTotalValue() { return changeActionPriorTotalValue; }
    public void setChangeActionPriorTotalValue(Long changeActionPriorTotalValue) { this.changeActionPriorTotalValue = changeActionPriorTotalValue; }


    public Long getCurrentImprovementValue() { return currentImprovementValue; }
    public void setCurrentImprovementValue(Long currentImprovementValue) { this.currentImprovementValue = currentImprovementValue; }


    public Long getCurrentLandValue() { return currentLandValue; }
    public void setCurrentLandValue(Long currentLandValue) { this.currentLandValue = currentLandValue; }


    public Long getCurrentParcelCount() { return currentParcelCount; }
    public void setCurrentParcelCount(Long currentParcelCount) { this.currentParcelCount = currentParcelCount; }


    public Long getCurrentTotalValue() { return currentTotalValue; }
    public void setCurrentTotalValue(Long currentTotalValue) { this.currentTotalValue = currentTotalValue; }


    public String getDivisionNumber() { return divisionNumber; }
    public void setDivisionNumber(String divisionNumber) { this.divisionNumber = divisionNumber; }


    public Long getNoChangeActionCurrentImprovementValue() { return noChangeActionCurrentImprovementValue; }
    public void setNoChangeActionCurrentImprovementValue(Long noChangeActionCurrentImprovementValue) { this.noChangeActionCurrentImprovementValue = noChangeActionCurrentImprovementValue; }


    public Long getNoChangeActionCurrentLandValue() { return noChangeActionCurrentLandValue; }
    public void setNoChangeActionCurrentLandValue(Long noChangeActionCurrentLandValue) { this.noChangeActionCurrentLandValue = noChangeActionCurrentLandValue; }


    public Long getNoChangeActionCurrentParcelCount() { return noChangeActionCurrentParcelCount; }
    public void setNoChangeActionCurrentParcelCount(Long noChangeActionCurrentParcelCount) { this.noChangeActionCurrentParcelCount = noChangeActionCurrentParcelCount; }


    public Long getNoChangeActionCurrentTotalValue() { return noChangeActionCurrentTotalValue; }
    public void setNoChangeActionCurrentTotalValue(Long noChangeActionCurrentTotalValue) { this.noChangeActionCurrentTotalValue = noChangeActionCurrentTotalValue; }


    public Long getNoChangeActionPriorImprovementValue() { return noChangeActionPriorImprovementValue; }
    public void setNoChangeActionPriorImprovementValue(Long noChangeActionPriorImprovementValue) { this.noChangeActionPriorImprovementValue = noChangeActionPriorImprovementValue; }


    public Long getNoChangeActionPriorLandValue() { return noChangeActionPriorLandValue; }
    public void setNoChangeActionPriorLandValue(Long noChangeActionPriorLandValue) { this.noChangeActionPriorLandValue = noChangeActionPriorLandValue; }


    public Long getNoChangeActionPriorParcelCount() { return noChangeActionPriorParcelCount; }
    public void setNoChangeActionPriorParcelCount(Long noChangeActionPriorParcelCount) { this.noChangeActionPriorParcelCount = noChangeActionPriorParcelCount; }


    public Long getNoChangeActionPriorTotalValue() { return noChangeActionPriorTotalValue; }
    public void setNoChangeActionPriorTotalValue(Long noChangeActionPriorTotalValue) { this.noChangeActionPriorTotalValue = noChangeActionPriorTotalValue; }


    public Long getPriorImprovementValue() { return priorImprovementValue; }
    public void setPriorImprovementValue(Long priorImprovementValue) { this.priorImprovementValue = priorImprovementValue; }


    public Long getPriorLandValue() { return priorLandValue; }
    public void setPriorLandValue(Long priorLandValue) { this.priorLandValue = priorLandValue; }


    public Long getPriorParcelCount() { return priorParcelCount; }
    public void setPriorParcelCount(Long priorParcelCount) { this.priorParcelCount = priorParcelCount; }


    public Long getPriorTotalValue() { return priorTotalValue; }
    public void setPriorTotalValue(Long priorTotalValue) { this.priorTotalValue = priorTotalValue; }


    public Long getProposedActualValue() { return proposedActualValue; }
    public void setProposedActualValue(Long proposedActualValue) { this.proposedActualValue = proposedActualValue; }


    public Long getProposedCurrent288Value() { return proposedCurrent288Value; }
    public void setProposedCurrent288Value(Long proposedCurrent288Value) { this.proposedCurrent288Value = proposedCurrent288Value; }


    public Long getProposedExpired288Value() { return proposedExpired288Value; }
    public void setProposedExpired288Value(Long proposedExpired288Value) { this.proposedExpired288Value = proposedExpired288Value; }


    public Long getProposedImprovementValue() { return proposedImprovementValue; }
    public void setProposedImprovementValue(Long proposedImprovementValue) { this.proposedImprovementValue = proposedImprovementValue; }


    public Long getProposedTotalValue() { return proposedTotalValue; }
    public void setProposedTotalValue(Long proposedTotalValue) { this.proposedTotalValue = proposedTotalValue; }



    // GENERATED-ACCESSORS:end
}
