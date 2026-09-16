package org.cookcounty.tax.domain.model;

/** Fields read from the ordered PS.EQUALVAL / EQVALRD01 source. */
public record TaxRateEqualizedValue(
        int sourceOrder,
        int volumeNumber,
        long parcelNumber,
        int taxCode,
        long assessedValue,
        long equalizedValue,
        String taxType) {
    // GENERATED-FIELDS:start
    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    // GENERATED-ACCESSORS:end
}
