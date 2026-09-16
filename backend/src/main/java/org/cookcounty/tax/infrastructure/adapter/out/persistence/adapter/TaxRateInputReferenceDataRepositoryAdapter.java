package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import java.util.List;

import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;
import org.cookcounty.tax.domain.port.out.TaxRateInputReferenceDataRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateDivisionEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.TaxRateEqualizedValueEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTaxRateDivisionRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTaxRateEqualizedValueRepository;
import org.springframework.stereotype.Component;

// GENERATED-IMPORTS:start
// GENERATED-IMPORTS:end

@Component
public class TaxRateInputReferenceDataRepositoryAdapter
        implements TaxRateInputReferenceDataRepository {

    private final JpaTaxRateEqualizedValueRepository equalizedValueRepository;
    private final JpaTaxRateDivisionRepository divisionRepository;

    public TaxRateInputReferenceDataRepositoryAdapter(
            JpaTaxRateEqualizedValueRepository equalizedValueRepository,
            JpaTaxRateDivisionRepository divisionRepository) {
        this.equalizedValueRepository = equalizedValueRepository;
        this.divisionRepository = divisionRepository;
    }

    // GENERATED-OVERRIDES:start
    // GENERATED-OVERRIDES:end

    @Override
    public List<TaxRateEqualizedValue> findEqualizedValuesInSourceOrder() {
        return equalizedValueRepository.findAllByOrderBySourceOrderAsc().stream()
                .map(TaxRateInputReferenceDataRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<TaxRateDivision> findDivisionsInSourceOrder() {
        return divisionRepository.findAllByOrderBySourceOrderAsc().stream()
                .map(TaxRateInputReferenceDataRepositoryAdapter::toDomain)
                .toList();
    }

    private static TaxRateEqualizedValue toDomain(TaxRateEqualizedValueEntity source) {
        return new TaxRateEqualizedValue(
                source.getSourceOrder(),
                source.getVolumeNumber(),
                source.getParcelNumber(),
                source.getTaxCode(),
                source.getAssessedValue(),
                source.getEqualizedValue(),
                source.getTaxType());
    }

    private static TaxRateDivision toDomain(TaxRateDivisionEntity source) {
        return new TaxRateDivision(
                source.getSourceOrder(),
                source.getVolumeNumber(),
                source.getParcelNumber(),
                source.getDivisionNumber());
    }
}
