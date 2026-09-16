package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;
import org.cookcounty.tax.domain.port.out.TaxRateInputReferenceDataRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateDivisionEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateEqualizedValueEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTaxRateDivisionRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTaxRateEqualizedValueRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/// Maps the complete maintained reference rows into immutable processing inputs.
@Component
public final class TaxRateInputReferenceDataRepositoryAdapter
        implements TaxRateInputReferenceDataRepository {
    private final JpaTaxRateEqualizedValueRepository equalizedValueRepository;
    private final JpaTaxRateDivisionRepository divisionRepository;

    /// Creates the adapter for both independently ordered reference roots.
    public TaxRateInputReferenceDataRepositoryAdapter(
            JpaTaxRateEqualizedValueRepository equalizedValueRepository,
            JpaTaxRateDivisionRepository divisionRepository) {
        this.equalizedValueRepository = equalizedValueRepository;
        this.divisionRepository = divisionRepository;
    }

    /// Returns complete equalized-value records in stable source order.
    @Override
    public List<TaxRateEqualizedValue> findEqualizedValuesInSourceOrder() {
        return equalizedValueRepository.findAllByOrderBySourceOrderAsc().stream()
                .map(TaxRateInputReferenceDataRepositoryAdapter::toDomain)
                .toList();
    }

    /// Returns complete parcel-to-division records in stable source order.
    @Override
    public List<TaxRateDivision> findDivisionsInSourceOrder() {
        return divisionRepository.findAllByOrderBySourceOrderAsc().stream()
                .map(TaxRateInputReferenceDataRepositoryAdapter::toDomain)
                .toList();
    }

    /// Preserves every stored parcel-value field and its optimistic-lock version.
    private static TaxRateEqualizedValue toDomain(TaxRateEqualizedValueEntity source) {
        return new TaxRateEqualizedValue(
                source.getSourceOrder(),
                source.getVolumeNumber(),
                source.getParcelNumber(),
                source.getTaxCode(),
                source.getAssessedValue(),
                source.getEqualizedValue(),
                source.getTaxType(),
                source.getVersion());
    }

    /// Preserves every stored parcel-to-division field and its optimistic-lock version.
    private static TaxRateDivision toDomain(TaxRateDivisionEntity source) {
        return new TaxRateDivision(
                source.getSourceOrder(),
                source.getVolumeNumber(),
                source.getParcelNumber(),
                source.getDivisionNumber(),
                source.getVersion());
    }
}
