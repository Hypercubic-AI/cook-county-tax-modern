package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/// Carries one source-ordered parcel value into division stamping and agency attachment.
///
/// Assessed and equalized values use exact whole source units. Source order is a repository
/// sequence, not a business identifier.
///
/// @param sourceOrder stable input sequence used for deterministic repository reads
/// @param volumeNumber unsigned three-digit volume portion of the parcel key
/// @param parcelNumber unsigned fifteen-digit parcel portion of the source key
/// @param taxCode unsigned five-digit tax-code identifier
/// @param assessedValue whole assessed-value units carried to output
/// @param equalizedValue whole equalized-value units carried to output and posting calculations
/// @param taxType tax-type code used in deterministic attachment ordering
/// @param version optimistic-lock value, or `null` before persistence supplies one
public record TaxRateEqualizedValue(
        int sourceOrder,
        String volumeNumber,
        String parcelNumber,
        String taxCode,
        BigDecimal assessedValue,
        BigDecimal equalizedValue,
        String taxType,
        @Nullable Long version) {
    /// Validates fixed-width keys and eleven-digit whole-unit values.
    public TaxRateEqualizedValue {
        volumeNumber = TaxRateNumericBoundary.identifier(volumeNumber, 3, "volumeNumber");
        parcelNumber = TaxRateNumericBoundary.identifier(parcelNumber, 15, "parcelNumber");
        taxCode = TaxRateNumericBoundary.identifier(taxCode, 5, "taxCode");
        assessedValue = TaxRateNumericBoundary.fixedPoint(assessedValue, 11, 0, "assessedValue");
        equalizedValue = TaxRateNumericBoundary.fixedPoint(equalizedValue, 11, 0, "equalizedValue");
        Objects.requireNonNull(taxType, "taxType");
    }
}
