package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.PropertyTaxRenewal;

import java.util.List;

/// Supplies the complete persisted homeowner renewal input in source order.
///
/// The returned order is part of the merge contract. Reading does not change renewal state.
public interface PropertyTaxRenewalRepository {
    /// Returns every maintained renewal row in ascending source order.
    List<PropertyTaxRenewal> findAllInSourceOrder();
}
