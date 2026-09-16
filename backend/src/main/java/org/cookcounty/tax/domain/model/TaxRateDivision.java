package org.cookcounty.tax.domain.model;

/** Fields read from the ordered PS.DIVSION / REDIVNRD01 source. */
public record TaxRateDivision(
        int sourceOrder,
        int volumeNumber,
        long parcelNumber,
        long divisionNumber) {
    // GENERATED-FIELDS:start
    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    // GENERATED-ACCESSORS:end
}
