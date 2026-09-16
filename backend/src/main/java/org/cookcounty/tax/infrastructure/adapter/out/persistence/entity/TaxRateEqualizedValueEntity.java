package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Entity
@Table(name = "tax_rate_equalized_values")
public class TaxRateEqualizedValueEntity {

    // GENERATED-FIELDS:start
    // GENERATED-FIELDS:end

    @Id
    @Column(name = "source_order")
    private Integer sourceOrder;

    @Column(name = "volume_number", nullable = false)
    private Integer volumeNumber;

    @Column(name = "parcel_number", nullable = false)
    private Long parcelNumber;

    @Column(name = "tax_code", nullable = false)
    private Integer taxCode;

    @Column(name = "assessed_value", nullable = false)
    private Long assessedValue;

    @Column(name = "equalized_value", nullable = false)
    private Long equalizedValue;

    @Column(name = "tax_type", nullable = false)
    private String taxType;

    protected TaxRateEqualizedValueEntity() {
    }

    // GENERATED-ACCESSORS:start
    // GENERATED-ACCESSORS:end

    public Integer getSourceOrder() {
        return sourceOrder;
    }

    public Integer getVolumeNumber() {
        return volumeNumber;
    }

    public Long getParcelNumber() {
        return parcelNumber;
    }

    public Integer getTaxCode() {
        return taxCode;
    }

    public Long getAssessedValue() {
        return assessedValue;
    }

    public Long getEqualizedValue() {
        return equalizedValue;
    }

    public String getTaxType() {
        return taxType;
    }
}
