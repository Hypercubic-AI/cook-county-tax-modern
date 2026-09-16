package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Entity
@Table(name = "tax_rate_divisions")
public class TaxRateDivisionEntity {

    // GENERATED-FIELDS:start
    // GENERATED-FIELDS:end

    @Id
    @Column(name = "source_order")
    private Integer sourceOrder;

    @Column(name = "volume_number", nullable = false)
    private Integer volumeNumber;

    @Column(name = "parcel_number", nullable = false)
    private Long parcelNumber;

    @Column(name = "division_number", nullable = false)
    private Long divisionNumber;

    protected TaxRateDivisionEntity() {
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

    public Long getDivisionNumber() {
        return divisionNumber;
    }
}
