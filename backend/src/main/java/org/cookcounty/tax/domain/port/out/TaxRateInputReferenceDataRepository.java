package org.cookcounty.tax.domain.port.out;

import java.util.List;

import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;

/** Supplies the distinct ordered PS.EQUALVAL and PS.DIVSION batch roots. */
public interface TaxRateInputReferenceDataRepository {

    // GENERATED-METHODS:start
    // GENERATED-METHODS:end

    List<TaxRateEqualizedValue> findEqualizedValuesInSourceOrder();

    List<TaxRateDivision> findDivisionsInSourceOrder();
}
