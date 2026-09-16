package org.cookcounty.tax.domain.port.out;

import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;

import java.util.List;

/// Supplies the maintained repository inputs for division stamping.
///
/// Each complete read uses ascending source order. The kernel applies the separate business-key
/// ordering rules and reports typed batch diagnostics for violations.
public interface TaxRateInputReferenceDataRepository {
    /// Returns all equalized-value inputs in stable ascending source order.
    List<TaxRateEqualizedValue> findEqualizedValuesInSourceOrder();

    /// Returns all parcel-to-division inputs in stable ascending source order.
    List<TaxRateDivision> findDivisionsInSourceOrder();
}
