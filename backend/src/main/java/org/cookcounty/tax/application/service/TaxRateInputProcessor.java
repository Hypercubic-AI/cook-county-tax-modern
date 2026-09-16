package org.cookcounty.tax.application.service;

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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/// Loads maintained source-order inputs and applies the tax-rate preparation rules.
///
/// The production path always reads both repository roots. It does not replace them with fixture
/// data. Posting updates save complete immutable frozen-agency snapshots.
@Component
public class TaxRateInputProcessor implements TaxRateInputKernel.FrozenAgencyStore {
    private final TaxRateInputReferenceDataRepository referenceDataRepository;
    private final FrozenAgencyAdjustmentRepository frozenAgencyAdjustmentRepository;
    private final TaxRateInputKernel kernel;

    /// Creates the processor with its maintained repositories and stateless rule kernel.
    public TaxRateInputProcessor(
            TaxRateInputReferenceDataRepository referenceDataRepository,
            FrozenAgencyAdjustmentRepository frozenAgencyAdjustmentRepository,
            TaxRateInputKernel kernel) {
        this.referenceDataRepository = referenceDataRepository;
        this.frozenAgencyAdjustmentRepository = frozenAgencyAdjustmentRepository;
        this.kernel = kernel;
    }

    /// Executes against complete, source-ordered repository inputs.
    ///
    /// The disclosed production inputs contain no positive tax-code or prior-year agency producer,
    /// so those kernel inputs are empty. The transaction commits successful posting mutations that
    /// the kernel records before a later typed failure.
    @Transactional
    public Result process() {
        List<EqualizedValue> equalizedValues =
                referenceDataRepository.findEqualizedValuesInSourceOrder().stream()
                        .map(TaxRateInputProcessor::toEqualizedValue)
                        .toList();
        List<Division> divisions =
                referenceDataRepository.findDivisionsInSourceOrder().stream()
                        .map(TaxRateInputProcessor::toDivision)
                        .toList();
        return process(new Input(equalizedValues, divisions, Map.of(), List.of()));
    }

    /// Executes evidence-backed synthetic inputs while retaining the production store behavior.
    ///
    /// This entry point exists for focused rule tests. It must not replace maintained repository
    /// inputs in the production path.
    @Transactional
    public Result process(Input input) {
        return kernel.process(input, this);
    }

    /// Inserts or replaces one complete frozen-agency snapshot for a posting operation.
    ///
    /// The value has scale zero because the kernel applies [`java.math.RoundingMode.HALF_UP`]. The
    /// operation reports whether the composite tax-code and agency identity existed before this
    /// call.
    ///
    /// @param taxCode five-character tax-code identity
    /// @param agencyNumber agency identity within the tax code
    /// @param value exact scale-zero amount after half-up rounding
    /// @param annex true for annexed value or false for disconnected value
    /// @return persistent insert or replacement operation
    @Override
    public Operation post(String taxCode, String agencyNumber, BigDecimal value, boolean annex) {
        FrozenAgencyAdjustment current =
                frozenAgencyAdjustmentRepository
                        .findByTaxCodeAndAgencyNumber(taxCode, agencyNumber)
                        .orElse(null);
        Operation operation = current == null ? Operation.INSERT : Operation.REWRITE;
        FrozenAgencyAdjustment replacement =
                current == null
                        ? newAdjustment(taxCode, agencyNumber, value, annex)
                        : withPostedValue(current, value, annex);
        frozenAgencyAdjustmentRepository.save(replacement);
        return operation;
    }

    /// Converts the numeric source tax code to its five-character processing identity.
    private static EqualizedValue toEqualizedValue(TaxRateEqualizedValue source) {
        String taxCode = source.taxCode();
        return new EqualizedValue(
                Integer.parseInt(taxCode.substring(0, 2)),
                Integer.parseInt(source.volumeNumber()),
                Long.parseLong(source.parcelNumber()),
                source.taxType(),
                taxCode,
                source.assessedValue(),
                source.equalizedValue());
    }

    /// Removes persistence metadata while preserving the parcel and division identity.
    private static Division toDivision(TaxRateDivision source) {
        return new Division(
                Integer.parseInt(source.volumeNumber()),
                Long.parseLong(source.parcelNumber()),
                Long.parseLong(source.divisionNumber()));
    }

    /// Returns a complete replacement with one exact posting accumulator increased.
    ///
    /// Decimal addition performs no rounding and preserves the larger operand scale.
    private static FrozenAgencyAdjustment withPostedValue(
            FrozenAgencyAdjustment current, BigDecimal value, boolean annex) {
        return new FrozenAgencyAdjustment(
                current.id(),
                current.version(),
                current.agencyNumber(),
                current.annexedAssessedValue(),
                annex
                        ? current.annexedEqualizedValue().add(value)
                        : current.annexedEqualizedValue(),
                current.current288Value(),
                current.disconnectedAssessedValue(),
                annex
                        ? current.disconnectedEqualizedValue()
                        : current.disconnectedEqualizedValue().add(value),
                current.expired288Value(),
                current.expiredIncentiveEqualizedValue(),
                current.expiredIncentiveTaxAmount(),
                current.expiredIncentiveValue(),
                current.firstTimeValue(),
                current.frozenEqualizedValue(),
                current.frozenTaxAmount(),
                current.taxCode(),
                current.taxRate(),
                current.tifCurrentEqualizedValue(),
                current.tifDifferenceEqualizedValue(),
                current.tifPriorFrozenEqualizedValue(),
                current.totalFrozenValue());
    }

    /// Creates a complete first snapshot while preserving the unspecified incentive tax amount.
    ///
    /// Decimal values use scale zero except for established tax fields with scale two or three.
    /// This method performs no rounding.
    private static FrozenAgencyAdjustment newAdjustment(
            String taxCode, String agencyNumber, BigDecimal value, boolean annex) {
        return new FrozenAgencyAdjustment(
                null,
                null,
                agencyNumber,
                BigDecimal.ZERO,
                annex ? value : BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                annex ? BigDecimal.ZERO : value,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY),
                taxCode,
                BigDecimal.ZERO.setScale(3, RoundingMode.UNNECESSARY),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }
}
