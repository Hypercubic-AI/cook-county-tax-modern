package org.cookcounty.tax.domain.model;

/// One source-ordered homeowner renewal input.
///
/// The processor derives the match result from the maintained homeowner key. The input does not
/// carry a trusted match decision.
///
/// @param propertyNumber canonical 15-digit property identifier used by the ordered merge
/// @param batchNumber five-character renewal batch identifier carried to reporting
public record PropertyTaxRenewal(String propertyNumber, String batchNumber) {
    /// Pads the property identifier for the ordered merge without computing a match decision.
    ///
    /// @throws IllegalArgumentException if the property identifier is empty, contains nondigits, or
    ///   exceeds 15 digits
    public PropertyTaxRenewal {
        propertyNumber =
                PropertyTaxExemptionsNumericBoundary.unsignedIdentifier(
                        propertyNumber, 15, "propertyNumber");
    }
}
