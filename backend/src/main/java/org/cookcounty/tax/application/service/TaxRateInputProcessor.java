package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.application.service.TaxRateInputKernel.Division;
import org.cookcounty.tax.application.service.TaxRateInputKernel.EqualizedValue;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Input;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Operation;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;
import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.cookcounty.tax.domain.port.out.TaxRateInputReferenceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Loads the distinct CLRTM batch roots and delegates processing to the rule kernel. */
@Component
public class TaxRateInputProcessor implements TaxRateInputKernel.FrozenAgencyStore {

    private final TaxRateInputReferenceDataRepository referenceDataRepository;
    private final FrozenAgencyAdjustmentRepository frozenAgencyAdjustmentRepository;
    private final TaxRateInputKernel kernel;

    @Autowired
    public TaxRateInputProcessor(
            TaxRateInputReferenceDataRepository referenceDataRepository,
            FrozenAgencyAdjustmentRepository frozenAgencyAdjustmentRepository) {
        this(referenceDataRepository, frozenAgencyAdjustmentRepository, new TaxRateInputKernel());
    }

    TaxRateInputProcessor(
            TaxRateInputReferenceDataRepository referenceDataRepository,
            FrozenAgencyAdjustmentRepository frozenAgencyAdjustmentRepository,
            TaxRateInputKernel kernel) {
        this.referenceDataRepository = referenceDataRepository;
        this.frozenAgencyAdjustmentRepository = frozenAgencyAdjustmentRepository;
        this.kernel = kernel;
    }

    /** Executes against the app's persisted, source-ordered PS.EQUALVAL and PS.DIVSION roots. */
    public Result process() {
        List<EqualizedValue> equalizedValues = referenceDataRepository
                .findEqualizedValuesInSourceOrder().stream()
                .map(TaxRateInputProcessor::toEqualizedValue)
                .toList();
        List<Division> divisions = referenceDataRepository.findDivisionsInSourceOrder().stream()
                .map(TaxRateInputProcessor::toDivision)
                .toList();

        // No positive tax-code or prior-agency producer is disclosed for this bullet.
        return process(new Input(equalizedValues, divisions, Map.of(), List.of()));
    }

    /** Executes rule-grounded synthetic inputs without changing the production input contract. */
    public Result process(Input input) {
        return kernel.process(input, this);
    }

    @Override
    public synchronized Operation post(
            String taxCode, String agencyNumber, long value, boolean annex) {
        FrozenAgencyAdjustment adjustment = frozenAgencyAdjustmentRepository
                .findByTaxCodeAndAgencyNumber(taxCode, agencyNumber)
                .orElse(null);
        Operation operation;
        if (adjustment == null) {
            adjustment = newAdjustment(taxCode, agencyNumber, value, annex);
            operation = Operation.INSERT;
        } else {
            if (annex) {
                adjustment.setAnnexedEqualizedValue(
                        zeroIfNull(adjustment.getAnnexedEqualizedValue()) + value);
            } else {
                adjustment.setDisconnectedEqualizedValue(
                        zeroIfNull(adjustment.getDisconnectedEqualizedValue()) + value);
            }
            operation = Operation.REWRITE;
        }
        frozenAgencyAdjustmentRepository.save(adjustment);
        return operation;
    }

    private static EqualizedValue toEqualizedValue(TaxRateEqualizedValue source) {
        String taxCode = String.format("%05d", source.taxCode());
        return new EqualizedValue(
                Integer.parseInt(taxCode.substring(0, 2)),
                source.volumeNumber(),
                source.parcelNumber(),
                source.taxType(),
                taxCode,
                source.assessedValue(),
                source.equalizedValue());
    }

    private static Division toDivision(TaxRateDivision source) {
        return new Division(
                source.volumeNumber(), source.parcelNumber(), source.divisionNumber());
    }

    private static FrozenAgencyAdjustment newAdjustment(
            String taxCode, String agencyNumber, long value, boolean annex) {
        FrozenAgencyAdjustment adjustment = new FrozenAgencyAdjustment();
        adjustment.setTaxCode(taxCode);
        adjustment.setAgencyNumber(agencyNumber);
        adjustment.setTaxRate(BigDecimal.ZERO.setScale(3));
        adjustment.setCurrent288Value(0L);
        adjustment.setExpired288Value(0L);
        adjustment.setFirstTimeValue(0L);
        adjustment.setTotalFrozenValue(0L);
        adjustment.setFrozenEqualizedValue(0L);
        adjustment.setFrozenTaxAmount(BigDecimal.ZERO.setScale(2));
        adjustment.setAnnexedAssessedValue(0L);
        adjustment.setAnnexedEqualizedValue(annex ? value : 0L);
        adjustment.setDisconnectedAssessedValue(0L);
        adjustment.setDisconnectedEqualizedValue(annex ? 0L : value);
        adjustment.setTifPriorFrozenEqualizedValue(0L);
        adjustment.setTifDifferenceEqualizedValue(0L);
        adjustment.setTifCurrentEqualizedValue(0L);
        adjustment.setExpiredIncentiveValue(0L);
        adjustment.setExpiredIncentiveEqualizedValue(0L);
        // FA-EXPINCTX is intentionally not initialized by CLRTM755.
        return adjustment;
    }

    private static long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }
}
