package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

/// Associates one source-ordered parcel with the division number used by downstream handoffs.
///
/// Source order is a repository sequence, not a business identifier. The string components retain
/// the complete zero-padded widths of the unsigned source fields.
///
/// @param sourceOrder stable input sequence used for deterministic repository reads
/// @param volumeNumber unsigned three-digit volume portion of the parcel key
/// @param parcelNumber unsigned fifteen-digit parcel portion of the source key
/// @param divisionNumber unsigned fourteen-digit division identifier stamped on matching output
/// @param version optimistic-lock value, or `null` before persistence supplies one
public record TaxRateDivision(
        int sourceOrder,
        String volumeNumber,
        String parcelNumber,
        String divisionNumber,
        @Nullable Long version) {
    /// Validates all fixed-width business identifiers.
    public TaxRateDivision {
        volumeNumber = TaxRateNumericBoundary.identifier(volumeNumber, 3, "volumeNumber");
        parcelNumber = TaxRateNumericBoundary.identifier(parcelNumber, 15, "parcelNumber");
        divisionNumber = TaxRateNumericBoundary.identifier(divisionNumber, 14, "divisionNumber");
    }
}
