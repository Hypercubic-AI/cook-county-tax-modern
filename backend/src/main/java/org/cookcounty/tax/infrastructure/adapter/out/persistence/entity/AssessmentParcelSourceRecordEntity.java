package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Entity
@Table(name = "assessment_parcel_source_records")
public class AssessmentParcelSourceRecordEntity {

    // GENERATED-FIELDS:start
    // GENERATED-FIELDS:end

    @Id
    @Column(name = "parcel_number")
    private Long parcelNumber;

    @Column(name = "source_order", nullable = false, unique = true)
    private Integer sourceOrder;

    @Column(name = "source_record_base64", nullable = false)
    private String sourceRecordBase64;

    // GENERATED-ACCESSORS:start
    // GENERATED-ACCESSORS:end

    public Long getParcelNumber() {
        return parcelNumber;
    }

    public void setParcelNumber(Long parcelNumber) {
        this.parcelNumber = parcelNumber;
    }

    public Integer getSourceOrder() {
        return sourceOrder;
    }

    public void setSourceOrder(Integer sourceOrder) {
        this.sourceOrder = sourceOrder;
    }

    public String getSourceRecordBase64() {
        return sourceRecordBase64;
    }

    public void setSourceRecordBase64(String sourceRecordBase64) {
        this.sourceRecordBase64 = sourceRecordBase64;
    }
}
