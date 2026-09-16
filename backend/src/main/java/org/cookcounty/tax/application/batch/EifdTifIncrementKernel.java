package org.cookcounty.tax.application.batch;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Pure calculation and grouping rules for the nine ordered increment steps.
///
/// Monetary inputs and whole-unit valuations use `BigDecimal`. Whole-unit calculations use scale
/// zero. Decimal shares use scale seven with truncation toward zero. Tax amounts use scale two with
/// half-up rounding. Methods return immutable results and perform no persistence.
@Component
public final class EifdTifIncrementKernel {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO_AMOUNT =
            BigDecimal.ZERO.setScale(0, RoundingMode.UNNECESSARY);

    /// Validates all launch controls before any assessment or reference input is read.
    public ControlRanges validateControls(
            @Nullable String reassessmentControl,
            @Nullable String processingYear,
            @Nullable String annualEqualizationFactor) {
        if (reassessmentControl == null
                || reassessmentControl.length() != 12
                || !digits(reassessmentControl.substring(0, 2))) {
            throw new RuleViolation(
                    "INVALID_REASSESSMENT_CONTROL",
                    "reassessmentControl must be a complete twelve-character value with a numeric"
                            + " current year.",
                    "asrea740-001");
        }

        String fromAge = reassessmentControl.substring(2, 4);
        String toAge = reassessmentControl.substring(4, 6);
        String from288 = reassessmentControl.substring(8, 10);
        String to288 = reassessmentControl.substring(10, 12);
        if (!digits(fromAge)
                || !digits(toAge)
                || !digits(from288)
                || !digits(to288)
                || Integer.parseInt(fromAge) > Integer.parseInt(toAge)
                || expandYear(from288) > expandYear(to288)) {
            throw new RuleViolation(
                    "INVALID_REASSESSMENT_RANGES",
                    "Improvement-age and Class 288 year bounds must be numeric and ascending.",
                    "asrea740-002");
        }
        if (processingYear == null || processingYear.length() != 2) {
            throw new RuleViolation(
                    "INVALID_PROCESSING_YEAR",
                    "processingYear must contain exactly two characters.",
                    "asrea742-005");
        }
        if (!digits(processingYear)) {
            throw new RuleViolation(
                    "INVALID_PROCESSING_YEAR",
                    "processingYear must contain digits only.",
                    "asrea742-006");
        }
        if (annualEqualizationFactor == null
                || annualEqualizationFactor.length() != 5
                || !digits(annualEqualizationFactor)
                || Long.parseLong(annualEqualizationFactor) == 0L) {
            throw new RuleViolation(
                    "INVALID_EQUALIZATION_FACTOR",
                    "annualEqualizationFactor must be five numeric characters and greater than"
                            + " zero.",
                    "asrea748-001");
        }
        return new ControlRanges(
                Integer.parseInt(reassessmentControl.substring(0, 2)),
                Integer.parseInt(fromAge),
                Integer.parseInt(toAge),
                expandYear(from288),
                expandYear(to288),
                Integer.parseInt(processingYear),
                new BigDecimal(annualEqualizationFactor).movePointLeft(4));
    }

    /// Expands a two-digit year with the reviewed 60/61 century boundary.
    public int expandYear(@Nullable String twoDigitYear) {
        if (twoDigitYear == null || twoDigitYear.length() != 2 || !digits(twoDigitYear)) {
            throw new IllegalArgumentException("twoDigitYear must contain two digits");
        }
        int year = Integer.parseInt(twoDigitYear);
        return year > 60 ? 1900 + year : 2000 + year;
    }

    /// Returns the town code stored in the first two positions of a five-digit tax code.
    public String deriveTownNumber(@Nullable String taxCode) {
        if (taxCode == null || taxCode.length() != 5 || !digits(taxCode)) {
            throw new IllegalArgumentException("taxCode must contain exactly five digits");
        }
        return taxCode.substring(0, 2);
    }

    /// Applies each positive factor in order and truncates to scale zero after every factor.
    public BigDecimal derivePriorImprovement(
            BigDecimal reproductionCost,
            @Nullable BigDecimal occupancyFactor,
            @Nullable BigDecimal conditionPercent,
            @Nullable BigDecimal assessmentPercent) {
        BigDecimal stage = wholeUnits(reproductionCost, "reproductionCost");
        if (occupancyFactor != null && occupancyFactor.signum() > 0) {
            stage =
                    truncate(
                            stage.multiply(ONE_HUNDRED)
                                    .divide(occupancyFactor, 20, RoundingMode.DOWN),
                            0);
        }
        if (conditionPercent != null && conditionPercent.signum() > 0) {
            stage =
                    truncate(
                            stage.multiply(conditionPercent)
                                    .divide(ONE_HUNDRED, 20, RoundingMode.DOWN),
                            0);
        }
        if (assessmentPercent != null && assessmentPercent.signum() > 0) {
            stage =
                    truncate(
                            stage.multiply(assessmentPercent)
                                    .divide(ONE_HUNDRED, 20, RoundingMode.DOWN),
                            0);
        }
        return stage;
    }

    /// Reports whether all current Class 288 selection conditions are true.
    public boolean qualifiesCurrent288(
            int category,
            int assessmentClass,
            int improvementYear,
            int processingYear,
            boolean materialPermitWithBuilding) {
        return category == 3
                && assessmentClass == 288
                && normalizeYear(improvementYear) == normalizeYear(processingYear)
                && materialPermitWithBuilding;
    }

    /// Reports whether an improvement belongs to the first-time category.
    public boolean qualifiesFirstTime(int category, int age, int assessmentClass) {
        return category >= 2 && category <= 5 && age == 1 && assessmentClass != 288;
    }

    /// Compares scale-zero prior and current improvement components with their totals.
    public boolean hasReconciliationDifference(ReconciliationValues values) {
        Objects.requireNonNull(values, "values");
        return values.priorBeyondFirstYear()
                                .add(values.priorFirstYear())
                                .compareTo(values.priorImprovementTotal())
                        != 0
                || values.currentNonFrozen()
                                .add(values.current288())
                                .add(values.currentFirstYear())
                                .add(values.currentOther())
                                .compareTo(values.currentImprovementTotal())
                        != 0;
    }

    /// Classifies one scale-zero improvement without rounding.
    public ClassifiedImprovement classifyImprovement(
            ImprovementClassInput input, ControlRanges controls) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(controls, "controls");
        BigDecimal priorBeyondFirstYear = ZERO_AMOUNT;
        BigDecimal priorFirstYear = ZERO_AMOUNT;
        BigDecimal prior288 = ZERO_AMOUNT;
        BigDecimal currentNonFrozen = ZERO_AMOUNT;
        BigDecimal currentFirstYear = ZERO_AMOUNT;
        BigDecimal current288 = ZERO_AMOUNT;
        BigDecimal currentOther = ZERO_AMOUNT;
        if (input.current()) {
            if (input.assessmentClass() == 288) {
                if (normalizeYear(input.improvementYear())
                                == normalizeYear(controls.processingYear())
                        && input.materialPermitWithBuilding()) {
                    current288 = input.value();
                } else {
                    currentOther = input.value();
                }
            } else if (input.age() == 1) {
                currentFirstYear = input.value();
            } else if (input.age() >= controls.fromAge() && input.age() <= controls.toAge()) {
                currentNonFrozen = input.value();
            } else {
                currentOther = input.value();
            }
        } else if (input.assessmentClass() == 288) {
            int year = normalizeYear(input.improvementYear());
            if (year >= controls.from288Year() && year <= controls.to288Year()) {
                prior288 = input.value();
            }
        } else if (input.age() == 1) {
            priorFirstYear = input.value();
        } else {
            priorBeyondFirstYear = input.value();
        }
        return new ClassifiedImprovement(
                priorBeyondFirstYear,
                priorFirstYear,
                prior288,
                currentNonFrozen,
                currentFirstYear,
                current288,
                currentOther);
    }

    /// Classifies one scale-zero division delta without mutating partial domain state.
    public FrozenValuation selectActionTotals(DivisionActionInput input) {
        Objects.requireNonNull(input, "input");
        boolean exemptTransition = input.fullyExemptPriorBecameNonexempt();
        BigDecimal proposedImprovement =
                exemptTransition ? input.currentTotal() : input.proposedImprovement();
        BigDecimal proposedExpired288 = exemptTransition ? ZERO_AMOUNT : input.proposedExpired288();
        BigDecimal proposedCurrent288 = exemptTransition ? ZERO_AMOUNT : input.proposedCurrent288();
        // The qualifying-new-value gate owns all eight change-action amounts.
        boolean action = !exemptTransition && input.qualifyingNewValue().signum() > 0;
        return new FrozenValuation(
                null,
                null,
                action ? input.currentImprovement() : ZERO_AMOUNT,
                action ? input.currentLand() : ZERO_AMOUNT,
                action ? input.currentParcelCount() : 0L,
                action ? input.currentTotal() : ZERO_AMOUNT,
                action ? input.priorImprovement() : ZERO_AMOUNT,
                action ? input.priorLand() : ZERO_AMOUNT,
                action ? input.priorParcelCount() : 0L,
                action ? input.priorTotal() : ZERO_AMOUNT,
                input.currentImprovement(),
                input.currentLand(),
                input.currentParcelCount(),
                input.currentTotal(),
                input.division(),
                ZERO_AMOUNT,
                ZERO_AMOUNT,
                0L,
                ZERO_AMOUNT,
                ZERO_AMOUNT,
                ZERO_AMOUNT,
                0L,
                ZERO_AMOUNT,
                input.priorImprovement(),
                input.priorLand(),
                input.priorParcelCount(),
                input.priorTotal(),
                ZERO_AMOUNT,
                proposedCurrent288,
                proposedExpired288,
                proposedImprovement,
                proposedImprovement.add(proposedExpired288).add(proposedCurrent288));
    }

    /// Adds a complete division delta to an optional stored snapshot.
    ///
    /// A missing stored snapshot starts at zero. The returned snapshot is independent of both
    /// inputs and retains the stored persistence identifier and version.
    public FrozenValuation accumulateFrozen(
            @Nullable FrozenValuation stored, FrozenValuation delta) {
        Objects.requireNonNull(delta, "delta");
        return new FrozenValuation(
                stored == null ? null : stored.id(),
                stored == null ? null : stored.version(),
                add(
                        stored == null
                                ? BigDecimal.ZERO
                                : stored.changeActionCurrentImprovementValue(),
                        delta.changeActionCurrentImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.changeActionCurrentLandValue(),
                        delta.changeActionCurrentLandValue()),
                add(
                        stored == null ? 0L : stored.changeActionCurrentParcelCount(),
                        delta.changeActionCurrentParcelCount()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.changeActionCurrentTotalValue(),
                        delta.changeActionCurrentTotalValue()),
                add(
                        stored == null
                                ? BigDecimal.ZERO
                                : stored.changeActionPriorImprovementValue(),
                        delta.changeActionPriorImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.changeActionPriorLandValue(),
                        delta.changeActionPriorLandValue()),
                add(
                        stored == null ? 0L : stored.changeActionPriorParcelCount(),
                        delta.changeActionPriorParcelCount()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.changeActionPriorTotalValue(),
                        delta.changeActionPriorTotalValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.currentImprovementValue(),
                        delta.currentImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.currentLandValue(),
                        delta.currentLandValue()),
                add(stored == null ? 0L : stored.currentParcelCount(), delta.currentParcelCount()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.currentTotalValue(),
                        delta.currentTotalValue()),
                stored == null ? delta.divisionNumber() : stored.divisionNumber(),
                add(
                        stored == null
                                ? BigDecimal.ZERO
                                : stored.noChangeActionCurrentImprovementValue(),
                        delta.noChangeActionCurrentImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.noChangeActionCurrentLandValue(),
                        delta.noChangeActionCurrentLandValue()),
                add(
                        stored == null ? 0L : stored.noChangeActionCurrentParcelCount(),
                        delta.noChangeActionCurrentParcelCount()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.noChangeActionCurrentTotalValue(),
                        delta.noChangeActionCurrentTotalValue()),
                add(
                        stored == null
                                ? BigDecimal.ZERO
                                : stored.noChangeActionPriorImprovementValue(),
                        delta.noChangeActionPriorImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.noChangeActionPriorLandValue(),
                        delta.noChangeActionPriorLandValue()),
                add(
                        stored == null ? 0L : stored.noChangeActionPriorParcelCount(),
                        delta.noChangeActionPriorParcelCount()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.noChangeActionPriorTotalValue(),
                        delta.noChangeActionPriorTotalValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.priorImprovementValue(),
                        delta.priorImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.priorLandValue(),
                        delta.priorLandValue()),
                add(stored == null ? 0L : stored.priorParcelCount(), delta.priorParcelCount()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.priorTotalValue(),
                        delta.priorTotalValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.proposedActualValue(),
                        delta.proposedActualValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.proposedCurrent288Value(),
                        delta.proposedCurrent288Value()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.proposedExpired288Value(),
                        delta.proposedExpired288Value()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.proposedImprovementValue(),
                        delta.proposedImprovementValue()),
                add(
                        stored == null ? BigDecimal.ZERO : stored.proposedTotalValue(),
                        delta.proposedTotalValue()));
    }

    /// Reports whether a missing division has any source-authorized nonzero value.
    public boolean shouldInsertFrozen(FrozenValuation value) {
        return nonzero(value.proposedImprovementValue())
                || nonzero(value.proposedExpired288Value())
                || nonzero(value.proposedCurrent288Value())
                || nonzero(value.proposedTotalValue())
                || nonzero(value.proposedActualValue())
                || nonzero(value.changeActionPriorLandValue())
                || nonzero(value.changeActionPriorImprovementValue())
                || nonzero(value.changeActionPriorTotalValue())
                || nonzero(value.changeActionPriorParcelCount())
                || nonzero(value.changeActionCurrentLandValue())
                || nonzero(value.changeActionCurrentImprovementValue())
                || nonzero(value.changeActionCurrentTotalValue())
                || nonzero(value.changeActionCurrentParcelCount());
    }

    /// Groups source-ordered selections and independently flushes eligible trailing groups.
    public RollupResult rollup(List<Selection> selections) {
        Objects.requireNonNull(selections, "selections");
        List<TaxCodeTotal> taxTotals = new ArrayList<>();
        List<DivisionTotal> divisionTotals = new ArrayList<>();
        if (selections.isEmpty()) {
            return new RollupResult(List.of(), List.of(), 0, false, null);
        }

        Selection first = selections.get(0);
        String division = first.division();
        String taxCode = first.taxCode();
        BigDecimal taxCurrent = first.current288();
        BigDecimal taxFirst = first.firstTime();
        BigDecimal divisionCurrent = first.current288();
        BigDecimal divisionFirst = first.firstTime();
        int groupCount = 1;
        int read = 1;
        boolean failed = false;
        String offendingKey = null;

        for (int index = 1; index < selections.size(); index++) {
            Selection current = selections.get(index);
            read++;
            if (compare(current.division(), current.taxCode(), division, taxCode) < 0) {
                failed = true;
                offendingKey = current.division() + "/" + current.taxCode();
                break;
            }
            if (!current.division().equals(division)) {
                taxTotals.add(new TaxCodeTotal(division, taxCode, taxCurrent, taxFirst));
                divisionTotals.add(
                        new DivisionTotal(
                                division, taxCode, groupCount, divisionCurrent, divisionFirst));
                division = current.division();
                taxCode = current.taxCode();
                taxCurrent = current.current288();
                taxFirst = current.firstTime();
                divisionCurrent = current.current288();
                divisionFirst = current.firstTime();
                groupCount = 1;
            } else if (!current.taxCode().equals(taxCode)) {
                taxTotals.add(new TaxCodeTotal(division, taxCode, taxCurrent, taxFirst));
                taxCode = current.taxCode();
                taxCurrent = current.current288();
                taxFirst = current.firstTime();
                divisionCurrent = divisionCurrent.add(current.current288());
                divisionFirst = divisionFirst.add(current.firstTime());
                groupCount++;
            } else {
                taxCurrent = taxCurrent.add(current.current288());
                taxFirst = taxFirst.add(current.firstTime());
                divisionCurrent = divisionCurrent.add(current.current288());
                divisionFirst = divisionFirst.add(current.firstTime());
            }
        }

        if (taxFirst.signum() > 0) {
            taxTotals.add(new TaxCodeTotal(division, taxCode, taxCurrent, taxFirst));
        }
        if (divisionFirst.signum() > 0) {
            divisionTotals.add(
                    new DivisionTotal(
                            division, taxCode, groupCount, divisionCurrent, divisionFirst));
        }
        return new RollupResult(
                List.copyOf(taxTotals), List.copyOf(divisionTotals), read, failed, offendingKey);
    }

    /// Pairs strict ordered totals and truncates each positive share to seven decimal places.
    public PercentageResult percentages(
            List<TaxCodeTotal> taxCodeTotals, List<DivisionTotal> divisionTotals) {
        validateStrictTaxTotals(taxCodeTotals);
        validateStrictDivisionTotals(divisionTotals);
        List<PercentageAllocation> percentages = new ArrayList<>();
        List<TaxCodeTotal> unmatched = new ArrayList<>();
        int taxIndex = 0;
        int divisionIndex = 0;
        while (taxIndex < taxCodeTotals.size() && divisionIndex < divisionTotals.size()) {
            TaxCodeTotal tax = taxCodeTotals.get(taxIndex);
            DivisionTotal division = divisionTotals.get(divisionIndex);
            int comparison = tax.division().compareTo(division.division());
            if (comparison < 0) {
                unmatched.add(tax);
                taxIndex++;
            } else if (comparison > 0) {
                divisionIndex++;
            } else {
                percentages.add(
                        new PercentageAllocation(
                                tax.division(),
                                tax.taxCode(),
                                division.taxCodeCount(),
                                share(tax.current288(), division.current288()),
                                share(tax.firstTime(), division.firstTime()),
                                ZERO_AMOUNT,
                                ZERO_AMOUNT,
                                ZERO_AMOUNT,
                                ZERO_AMOUNT));
                taxIndex++;
            }
        }
        while (taxIndex < taxCodeTotals.size()) {
            unmatched.add(taxCodeTotals.get(taxIndex++));
        }
        return new PercentageResult(List.copyOf(percentages), List.copyOf(unmatched));
    }

    /// Allocates frozen values and counts rows whose division has no valuation.
    public AllocationResult allocate(
            List<PercentageAllocation> percentages, Map<String, FrozenValuation> valuations) {
        List<AllocatedFrozen> allocated = new ArrayList<>();
        int unmatched = 0;
        for (PercentageAllocation percentage : percentages) {
            FrozenValuation valuation = valuations.get(percentage.division());
            if (valuation == null) {
                unmatched++;
                continue;
            }
            BigDecimal firstTime;
            BigDecimal current288;
            BigDecimal expired288;
            if (percentage.taxCodeCount() == 1) {
                firstTime = valuation.proposedImprovementValue();
                current288 = valuation.proposedCurrent288Value();
                expired288 = valuation.proposedExpired288Value();
            } else {
                firstTime =
                        allocatePositive(
                                valuation.proposedImprovementValue(), percentage.firstTimeShare());
                current288 =
                        allocatePositive(
                                valuation.proposedCurrent288Value(), percentage.current288Share());
                expired288 =
                        valuation
                                .proposedExpired288Value()
                                .divide(
                                        BigDecimal.valueOf(percentage.taxCodeCount()),
                                        0,
                                        RoundingMode.DOWN);
            }
            allocated.add(
                    new AllocatedFrozen(
                            percentage.division(),
                            percentage.taxCode(),
                            percentage.taxCodeCount(),
                            percentage.current288Share(),
                            percentage.firstTimeShare(),
                            current288,
                            expired288,
                            firstTime,
                            ZERO_AMOUNT));
        }
        return new AllocationResult(List.copyOf(allocated), unmatched);
    }

    /// Expands the sequential allocation handoff through all 40 agency positions.
    ///
    /// The first missing tax-code master stops later tax codes and retains earlier output. Frozen
    /// equalized values round half away from zero to whole units. Tax rounds to two decimals.
    public FrozenAgencyResult expandFrozenAgencies(
            List<AllocatedFrozen> sequentialHandoff,
            Map<String, TaxCodeMaster> taxCodeMasters,
            BigDecimal factor) {
        List<FrozenAgencyDetail> details = new ArrayList<>();
        int read = 0;
        for (AllocatedFrozen frozen : sequentialHandoff) {
            read++;
            TaxCodeMaster master = taxCodeMasters.get(frozen.taxCode());
            if (master == null) {
                return new FrozenAgencyResult(List.copyOf(details), read, true, frozen.taxCode());
            }
            BigDecimal assessed =
                    frozen.current288().add(frozen.expired288()).add(frozen.firstTime());
            BigDecimal equalized = assessed.multiply(factor).setScale(0, RoundingMode.HALF_UP);
            BigDecimal tax =
                    assessed.multiply(master.taxRate())
                            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal expiredIncentiveEqualized =
                    frozen.expiredIncentive().multiply(factor).setScale(0, RoundingMode.HALF_UP);
            for (Long agency : master.agencyPositions()) {
                if (agency > 0) {
                    details.add(
                            new FrozenAgencyDetail(
                                    frozen.taxCode(),
                                    agency,
                                    master.town(),
                                    master.taxRate(),
                                    frozen.current288(),
                                    frozen.expired288(),
                                    frozen.firstTime(),
                                    frozen.expiredIncentive(),
                                    assessed,
                                    equalized,
                                    tax,
                                    ZERO_AMOUNT,
                                    ZERO_AMOUNT,
                                    ZERO_AMOUNT,
                                    ZERO_AMOUNT,
                                    ZERO_AMOUNT,
                                    ZERO_AMOUNT,
                                    expiredIncentiveEqualized));
                }
            }
        }
        return new FrozenAgencyResult(List.copyOf(details), read, false, null);
    }

    /// Flushes each referenced agency group, including the final open group.
    public AgencySummaryResult summarizeAgencies(
            List<FrozenAgencyDetail> details, Map<Long, String> agencyDescriptions) {
        List<AgencySummary> summaries = new ArrayList<>();
        long previousAgency = Long.MIN_VALUE;
        MutableAgencySummary current = null;
        int read = 0;
        for (FrozenAgencyDetail detail : details) {
            read++;
            if (detail.agency() < previousAgency) {
                return new AgencySummaryResult(List.copyOf(summaries), read, true, detail.agency());
            }
            if (current == null || detail.agency() != current.agency) {
                if (current != null) {
                    String description = agencyDescriptions.get(current.agency);
                    if (description == null) {
                        return new AgencySummaryResult(
                                List.copyOf(summaries), read, true, current.agency);
                    }
                    summaries.add(current.freeze(description));
                }
                current = new MutableAgencySummary(detail.agency());
            }
            current.add(detail);
            previousAgency = detail.agency();
        }
        if (current != null) {
            String description = agencyDescriptions.get(current.agency);
            if (description == null) {
                return new AgencySummaryResult(List.copyOf(summaries), read, true, current.agency);
            }
            summaries.add(current.freeze(description));
        }
        return new AgencySummaryResult(List.copyOf(summaries), read, false, null);
    }

    /// Groups ordered agency-town details and flushes both town and agency totals.
    public TownReportResult reportTowns(
            List<FrozenAgencyDetail> details, Map<Integer, String> townNames) {
        Map<AgencyTownKey, TownTotal> townTotals = new LinkedHashMap<>();
        AgencyTownKey previous = null;
        for (FrozenAgencyDetail detail : details) {
            AgencyTownKey key = new AgencyTownKey(detail.agency(), detail.town());
            if (previous != null && key.compareTo(previous) < 0) {
                return townReport(townTotals, true, key);
            }
            String townName = townNames.get(detail.town());
            townTotals.compute(
                    key,
                    (unused, existing) -> {
                        TownTotal base =
                                existing == null
                                        ? new TownTotal(
                                                detail.agency(),
                                                detail.town(),
                                                townName,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT)
                                        : existing;
                        return new TownTotal(
                                base.agency(),
                                base.town(),
                                base.townName(),
                                base.newPropertyEqualized().add(detail.frozenEqualized()),
                                base.annexedEqualized().add(detail.annexedEqualized()),
                                base.disconnectedEqualized().add(detail.disconnectedEqualized()),
                                base.recoveredTifEqualized().add(detail.recoveredTifEqualized()),
                                base.expiredIncentiveEqualized()
                                        .add(detail.expiredIncentiveEqualized()));
                    });
            previous = key;
        }
        return townReport(townTotals, false, null);
    }

    /// Builds town and agency totals from the ordered open groups.
    private TownReportResult townReport(
            Map<AgencyTownKey, TownTotal> townTotals,
            boolean failed,
            @Nullable AgencyTownKey offendingKey) {
        Map<Long, TownTotal> agencyTotals = new LinkedHashMap<>();
        for (TownTotal town : townTotals.values()) {
            agencyTotals.compute(
                    town.agency(),
                    (unused, existing) -> {
                        TownTotal base =
                                existing == null
                                        ? new TownTotal(
                                                town.agency(),
                                                0,
                                                null,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT,
                                                ZERO_AMOUNT)
                                        : existing;
                        return new TownTotal(
                                base.agency(),
                                0,
                                null,
                                base.newPropertyEqualized().add(town.newPropertyEqualized()),
                                base.annexedEqualized().add(town.annexedEqualized()),
                                base.disconnectedEqualized().add(town.disconnectedEqualized()),
                                base.recoveredTifEqualized().add(town.recoveredTifEqualized()),
                                base.expiredIncentiveEqualized()
                                        .add(town.expiredIncentiveEqualized()));
                    });
        }
        return new TownReportResult(
                List.copyOf(townTotals.values()),
                List.copyOf(agencyTotals.values()),
                failed,
                offendingKey);
    }

    /// Replaces the four posting amounts for each matched agency in strict source order.
    ///
    /// An unmatched agency produces no insertion. A duplicate or descending agency stops before
    /// that agency is applied, while earlier replacements remain in the returned partial result.
    public PostingResult postAgencySummaries(
            List<AgencySummary> summaries, Map<String, AgencyEqualizedValuation> existing) {
        Map<String, AgencyEqualizedValuation> updated = new LinkedHashMap<>();
        List<Long> unmatched = new ArrayList<>();
        long previous = Long.MIN_VALUE;
        int read = 0;
        for (AgencySummary summary : summaries) {
            read++;
            if (summary.agency() <= previous) {
                return new PostingResult(
                        orderedCopy(updated), List.copyOf(unmatched), read, true, summary.agency());
            }
            String key = String.format("%09d", summary.agency());
            AgencyEqualizedValuation stored = existing.get(key);
            if (stored == null) {
                unmatched.add(summary.agency());
            } else {
                AgencyEqualizedValuation replacement =
                        new AgencyEqualizedValuation(
                                stored.id(),
                                stored.version(),
                                stored.agencyNumber(),
                                summary.annexedEqualized(),
                                stored.burdenPercent(),
                                stored.connectingAgency1(),
                                stored.connectingAgency2(),
                                stored.connectingAgency3(),
                                stored.connectingAgency4(),
                                stored.cookCountyAirPollutionValue(),
                                stored.cookCountyRailroadValue(),
                                stored.cookCountyRealEstateValue(),
                                stored.cookCountyUseTaxValue(),
                                stored.deKalbCountyEqualizedValue(),
                                summary.disconnectedEqualized(),
                                summary.recoveredTifEqualized(),
                                stored.duPageCountyEqualizedValue(),
                                stored.grundyCountyEqualizedValue(),
                                stored.kaneCountyEqualizedValue(),
                                stored.kankakeeCountyEqualizedValue(),
                                stored.kendallCountyEqualizedValue(),
                                stored.laSalleCountyEqualizedValue(),
                                stored.lakeCountyEqualizedValue(),
                                stored.limitingTaxRateOverride(),
                                stored.livingstonCountyEqualizedValue(),
                                stored.mcHenryCountyEqualizedValue(),
                                summary.newPropertyEqualized()
                                        .add(summary.expiredIncentiveEqualized()),
                                stored.overlapAnnexedPropertyEqualizedValue(),
                                stored.overlapDisconnectedPropertyEqualizedValue(),
                                stored.overlapDisconnectedTifDifference(),
                                stored.overlapNewPropertyEqualizedValue(),
                                stored.parentAgency1(),
                                stored.parentAgency2(),
                                stored.parentAgency3(),
                                stored.parentAgency4(),
                                stored.parentAgency5(),
                                stored.previousTaxYear1(),
                                stored.previousTaxYear1Extension(),
                                stored.previousTaxYear2(),
                                stored.previousTaxYear2Extension(),
                                stored.previousTaxYear3(),
                                stored.previousTaxYear3Extension(),
                                stored.taxCapIndicator(),
                                stored.taxYear(),
                                stored.willCountyEqualizedValue());
                updated.put(key, replacement);
            }
            previous = summary.agency();
        }
        return new PostingResult(orderedCopy(updated), List.copyOf(unmatched), read, false, null);
    }

    /// Preserves insertion order while preventing later map changes.
    private static <K, V> Map<K, V> orderedCopy(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    /// Rejects duplicate or descending tax-code total keys before allocation.
    private void validateStrictTaxTotals(List<TaxCodeTotal> totals) {
        for (int index = 1; index < totals.size(); index++) {
            TaxCodeTotal previous = totals.get(index - 1);
            TaxCodeTotal current = totals.get(index);
            if (compare(
                            current.division(),
                            current.taxCode(),
                            previous.division(),
                            previous.taxCode())
                    <= 0) {
                throw new RuleViolation(
                        "OUT_OF_SEQUENCE",
                        "Tax-code totals must be strictly ascending by division and tax code.",
                        "asrea744-001");
            }
        }
    }

    /// Rejects duplicate or descending division total keys before allocation.
    private void validateStrictDivisionTotals(List<DivisionTotal> totals) {
        for (int index = 1; index < totals.size(); index++) {
            DivisionTotal previous = totals.get(index - 1);
            DivisionTotal current = totals.get(index);
            if (compare(
                            current.division(),
                            current.lastTaxCode(),
                            previous.division(),
                            previous.lastTaxCode())
                    <= 0) {
                throw new RuleViolation(
                        "OUT_OF_SEQUENCE",
                        "Division totals must be strictly ascending by division and tax code.",
                        "asrea744-001");
            }
        }
    }

    /// Returns a scale-seven truncated share, or scale-seven zero for a nonpositive denominator.
    private BigDecimal share(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.signum() <= 0) {
            return BigDecimal.ZERO.setScale(7, RoundingMode.UNNECESSARY);
        }
        return numerator.divide(denominator, 7, RoundingMode.DOWN);
    }

    /// Allocates only a positive share and truncates toward zero to scale zero.
    private BigDecimal allocatePositive(BigDecimal amount, BigDecimal percentage) {
        if (percentage.signum() <= 0) {
            return ZERO_AMOUNT;
        }
        return amount.multiply(percentage).setScale(0, RoundingMode.DOWN);
    }

    /// Compares the division and tax-code composite key in source order.
    private int compare(
            String division, String taxCode, String otherDivision, String otherTaxCode) {
        int divisionComparison = division.compareTo(otherDivision);
        return divisionComparison != 0 ? divisionComparison : taxCode.compareTo(otherTaxCode);
    }

    /// Expands two-digit years with the reviewed century boundary.
    private int normalizeYear(int year) {
        return year < 100 ? (year > 60 ? 1900 + year : 2000 + year) : year;
    }

    /// Reports whether a nonempty control contains only decimal digits.
    private static boolean digits(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    /// Truncates an exact decimal toward zero at the requested scale.
    private static BigDecimal truncate(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.DOWN);
    }

    /// Reports whether an integral source count is nonzero.
    private static boolean nonzero(@Nullable Long value) {
        return value != null && value != 0L;
    }

    /// Reports whether an exact whole-unit valuation is nonzero.
    private static boolean nonzero(@Nullable BigDecimal value) {
        return value != null && value.signum() != 0;
    }

    /// Returns the complete integral source count.
    private static long value(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    /// Normalizes a required whole-unit monetary amount to scale zero without rounding.
    private static BigDecimal wholeUnits(BigDecimal value, String name) {
        return Objects.requireNonNull(value, name).setScale(0, RoundingMode.UNNECESSARY);
    }

    /// Adds two complete integral counts without rounding.
    private static Long add(Long left, Long right) {
        return value(left) + value(right);
    }

    /// Adds two exact whole-unit valuations without narrowing their representation.
    private static BigDecimal add(BigDecimal left, BigDecimal right) {
        return left.add(right);
    }

    /// Accumulates one source-ordered agency group in scale-zero monetary values.
    private static final class MutableAgencySummary {
        private final long agency;
        private BigDecimal newAssessed = ZERO_AMOUNT;
        private BigDecimal newEqualized = ZERO_AMOUNT;
        private BigDecimal annexedAssessed = ZERO_AMOUNT;
        private BigDecimal annexedEqualized = ZERO_AMOUNT;
        private BigDecimal disconnectedAssessed = ZERO_AMOUNT;
        private BigDecimal disconnectedEqualized = ZERO_AMOUNT;
        private BigDecimal recoveredTif = ZERO_AMOUNT;
        private BigDecimal expiredAssessed = ZERO_AMOUNT;
        private BigDecimal expiredEqualized = ZERO_AMOUNT;

        /// Starts a source group for one agency classification code.
        private MutableAgencySummary(long agency) {
            this.agency = agency;
        }

        /// Adds one source-ordered tax-code detail without rounding or primitive conversion.
        private void add(FrozenAgencyDetail detail) {
            newAssessed = newAssessed.add(detail.totalFrozen());
            newEqualized = newEqualized.add(detail.frozenEqualized());
            annexedAssessed = annexedAssessed.add(detail.annexedAssessed());
            annexedEqualized = annexedEqualized.add(detail.annexedEqualized());
            disconnectedAssessed = disconnectedAssessed.add(detail.disconnectedAssessed());
            disconnectedEqualized = disconnectedEqualized.add(detail.disconnectedEqualized());
            recoveredTif = recoveredTif.add(detail.recoveredTifEqualized());
            expiredAssessed = expiredAssessed.add(detail.expiredIncentive());
            expiredEqualized = expiredEqualized.add(detail.expiredIncentiveEqualized());
        }

        /// Closes the group with its description and exact scale-zero totals.
        private AgencySummary freeze(String description) {
            return new AgencySummary(
                    agency,
                    description,
                    newAssessed,
                    newEqualized,
                    annexedAssessed,
                    annexedEqualized,
                    disconnectedAssessed,
                    disconnectedEqualized,
                    recoveredTif,
                    expiredAssessed,
                    expiredEqualized);
        }
    }

    /// Validated launch ranges and the exact scale-four equalization factor.
    public record ControlRanges(
            int currentYear,
            int fromAge,
            int toAge,
            int from288Year,
            int to288Year,
            int processingYear,
            BigDecimal equalizationFactor) {
        /// Requires and normalizes the equalization factor to its source-implied scale of four.
        public ControlRanges {
            equalizationFactor =
                    Objects.requireNonNull(equalizationFactor, "equalizationFactor")
                            .setScale(4, RoundingMode.UNNECESSARY);
        }
    }

    /// Scale-zero monetary input used to classify one improvement.
    public record ImprovementClassInput(
            boolean current,
            BigDecimal value,
            int age,
            int assessmentClass,
            int improvementYear,
            boolean materialPermitWithBuilding) {
        /// Requires an exact whole-unit value and normalizes it to scale zero without rounding.
        public ImprovementClassInput {
            value = wholeUnits(value, "value");
        }
    }

    /// Scale-zero improvement categories produced without rounding.
    public record ClassifiedImprovement(
            BigDecimal priorBeyondFirstYear,
            BigDecimal priorFirstYear,
            BigDecimal prior288,
            BigDecimal currentNonFrozen,
            BigDecimal currentFirstYear,
            BigDecimal current288,
            BigDecimal currentOther) {
        /// Requires every category and normalizes each exact whole-unit value to scale zero.
        public ClassifiedImprovement {
            priorBeyondFirstYear = wholeUnits(priorBeyondFirstYear, "priorBeyondFirstYear");
            priorFirstYear = wholeUnits(priorFirstYear, "priorFirstYear");
            prior288 = wholeUnits(prior288, "prior288");
            currentNonFrozen = wholeUnits(currentNonFrozen, "currentNonFrozen");
            currentFirstYear = wholeUnits(currentFirstYear, "currentFirstYear");
            current288 = wholeUnits(current288, "current288");
            currentOther = wholeUnits(currentOther, "currentOther");
        }
    }

    /// Scale-zero prior, current, proposed, and action inputs for one division.
    public record DivisionActionInput(
            String division,
            String townshipPrefix,
            BigDecimal priorLand,
            BigDecimal priorImprovement,
            BigDecimal priorTotal,
            long priorParcelCount,
            BigDecimal currentLand,
            BigDecimal currentImprovement,
            BigDecimal currentTotal,
            long currentParcelCount,
            BigDecimal proposedImprovement,
            BigDecimal proposedExpired288,
            BigDecimal proposedCurrent288,
            BigDecimal qualifyingNewValue,
            boolean permitChanged,
            boolean fullyExemptPriorBecameNonexempt) {
        /// Requires identifiers and exact whole-unit amounts, then normalizes amounts to scale
        /// zero.
        public DivisionActionInput {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(townshipPrefix, "townshipPrefix");
            priorLand = wholeUnits(priorLand, "priorLand");
            priorImprovement = wholeUnits(priorImprovement, "priorImprovement");
            priorTotal = wholeUnits(priorTotal, "priorTotal");
            currentLand = wholeUnits(currentLand, "currentLand");
            currentImprovement = wholeUnits(currentImprovement, "currentImprovement");
            currentTotal = wholeUnits(currentTotal, "currentTotal");
            proposedImprovement = wholeUnits(proposedImprovement, "proposedImprovement");
            proposedExpired288 = wholeUnits(proposedExpired288, "proposedExpired288");
            proposedCurrent288 = wholeUnits(proposedCurrent288, "proposedCurrent288");
            qualifyingNewValue = wholeUnits(qualifyingNewValue, "qualifyingNewValue");
        }
    }

    /// Scale-zero components compared by the reconciliation rule.
    public record ReconciliationValues(
            BigDecimal priorBeyondFirstYear,
            BigDecimal priorFirstYear,
            BigDecimal priorImprovementTotal,
            BigDecimal currentNonFrozen,
            BigDecimal current288,
            BigDecimal currentFirstYear,
            BigDecimal currentOther,
            BigDecimal currentImprovementTotal) {
        /// Requires every exact whole-unit component and normalizes each one to scale zero.
        public ReconciliationValues {
            priorBeyondFirstYear = wholeUnits(priorBeyondFirstYear, "priorBeyondFirstYear");
            priorFirstYear = wholeUnits(priorFirstYear, "priorFirstYear");
            priorImprovementTotal = wholeUnits(priorImprovementTotal, "priorImprovementTotal");
            currentNonFrozen = wholeUnits(currentNonFrozen, "currentNonFrozen");
            current288 = wholeUnits(current288, "current288");
            currentFirstYear = wholeUnits(currentFirstYear, "currentFirstYear");
            currentOther = wholeUnits(currentOther, "currentOther");
            currentImprovementTotal =
                    wholeUnits(currentImprovementTotal, "currentImprovementTotal");
        }
    }

    /// One source-ordered division and tax-code selection with scale-zero monetary values.
    public record Selection(
            String division,
            String taxCode,
            BigDecimal current288,
            BigDecimal firstTime,
            BigDecimal expired288) {
        /// Requires identifiers and normalizes exact whole-unit monetary values to scale zero.
        public Selection {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(taxCode, "taxCode");
            current288 = wholeUnits(current288, "current288");
            firstTime = wholeUnits(firstTime, "firstTime");
            expired288 = wholeUnits(expired288, "expired288");
        }
    }

    /// Scale-zero totals for one division and tax-code group.
    public record TaxCodeTotal(
            String division, String taxCode, BigDecimal current288, BigDecimal firstTime) {
        /// Requires identifiers and normalizes exact whole-unit totals to scale zero.
        public TaxCodeTotal {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(taxCode, "taxCode");
            current288 = wholeUnits(current288, "current288");
            firstTime = wholeUnits(firstTime, "firstTime");
        }
    }

    /// Scale-zero totals and the integral tax-code count for one division group.
    public record DivisionTotal(
            String division,
            String lastTaxCode,
            int taxCodeCount,
            BigDecimal current288,
            BigDecimal firstTime) {
        /// Requires identifiers and normalizes exact whole-unit totals to scale zero.
        public DivisionTotal {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(lastTaxCode, "lastTaxCode");
            current288 = wholeUnits(current288, "current288");
            firstTime = wholeUnits(firstTime, "firstTime");
        }
    }

    /// Eligible source-ordered rollups, including partial trailing products on failure.
    public record RollupResult(
            List<TaxCodeTotal> taxCodeTotals,
            List<DivisionTotal> divisionTotals,
            int recordsRead,
            boolean failed,
            @Nullable String offendingKey) {
        /// Copies both result lists so later caller mutations cannot alter the rollup snapshot.
        public RollupResult {
            taxCodeTotals = List.copyOf(taxCodeTotals);
            divisionTotals = List.copyOf(divisionTotals);
        }
    }

    /// Scale-seven shares and scale-zero allocation fields for one tax code.
    public record PercentageAllocation(
            String division,
            String taxCode,
            int taxCodeCount,
            BigDecimal current288Share,
            BigDecimal firstTimeShare,
            BigDecimal current288,
            BigDecimal expired288,
            BigDecimal firstTime,
            BigDecimal expiredIncentive) {
        /// Requires identifiers and exact values, then normalizes every field to its source scale.
        public PercentageAllocation {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(taxCode, "taxCode");
            current288Share =
                    Objects.requireNonNull(current288Share, "current288Share")
                            .setScale(7, RoundingMode.UNNECESSARY);
            firstTimeShare =
                    Objects.requireNonNull(firstTimeShare, "firstTimeShare")
                            .setScale(7, RoundingMode.UNNECESSARY);
            current288 = wholeUnits(current288, "current288");
            expired288 = wholeUnits(expired288, "expired288");
            firstTime = wholeUnits(firstTime, "firstTime");
            expiredIncentive = wholeUnits(expiredIncentive, "expiredIncentive");
        }
    }

    /// Matched percentage rows and source-ordered unmatched tax-code totals.
    public record PercentageResult(
            List<PercentageAllocation> percentages, List<TaxCodeTotal> unmatchedTaxCodeTotals) {
        /// Copies both lists so later caller mutations cannot alter the percentage snapshot.
        public PercentageResult {
            percentages = List.copyOf(percentages);
            unmatchedTaxCodeTotals = List.copyOf(unmatchedTaxCodeTotals);
        }
    }

    /// Scale-zero frozen values allocated to one division and tax code.
    public record AllocatedFrozen(
            String division,
            String taxCode,
            int taxCodeCount,
            BigDecimal current288Share,
            BigDecimal firstTimeShare,
            BigDecimal current288,
            BigDecimal expired288,
            BigDecimal firstTime,
            BigDecimal expiredIncentive) {
        /// Requires identifiers and exact values, then normalizes every field to its source scale.
        public AllocatedFrozen {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(taxCode, "taxCode");
            current288Share =
                    Objects.requireNonNull(current288Share, "current288Share")
                            .setScale(7, RoundingMode.UNNECESSARY);
            firstTimeShare =
                    Objects.requireNonNull(firstTimeShare, "firstTimeShare")
                            .setScale(7, RoundingMode.UNNECESSARY);
            current288 = wholeUnits(current288, "current288");
            expired288 = wholeUnits(expired288, "expired288");
            firstTime = wholeUnits(firstTime, "firstTime");
            expiredIncentive = wholeUnits(expiredIncentive, "expiredIncentive");
        }
    }

    /// Source-ordered allocations and the count of missing valuation divisions.
    public record AllocationResult(List<AllocatedFrozen> allocations, int unmatchedCount) {
        /// Copies allocations so later caller mutations cannot alter the result.
        public AllocationResult {
            allocations = List.copyOf(allocations);
        }
    }

    /// Exact tax rate, town code, and exactly 40 agency classification positions.
    public record TaxCodeMaster(
            String taxCode, BigDecimal taxRate, int town, List<Long> agencyPositions) {
        /// Requires identifiers and rate, copies positions, and enforces all 40 source positions.
        public TaxCodeMaster {
            Objects.requireNonNull(taxCode, "taxCode");
            Objects.requireNonNull(taxRate, "taxRate");
            agencyPositions = List.copyOf(agencyPositions);
            if (agencyPositions.size() != 40) {
                throw new IllegalArgumentException(
                        "A tax-code master must expose exactly 40 agency positions");
            }
        }
    }

    /// Scale-zero assessed and equalized categories plus scale-two tax for one agency.
    public record FrozenAgencyDetail(
            String taxCode,
            long agency,
            int town,
            BigDecimal taxRate,
            BigDecimal current288,
            BigDecimal expired288,
            BigDecimal firstTime,
            BigDecimal expiredIncentive,
            BigDecimal totalFrozen,
            BigDecimal frozenEqualized,
            BigDecimal frozenTax,
            BigDecimal annexedAssessed,
            BigDecimal annexedEqualized,
            BigDecimal disconnectedAssessed,
            BigDecimal disconnectedEqualized,
            BigDecimal recoveredTifEqualized,
            BigDecimal tifCurrentEqualized,
            BigDecimal expiredIncentiveEqualized) {
        /// Requires all monetary values and normalizes whole-unit categories and scale-two tax.
        public FrozenAgencyDetail {
            Objects.requireNonNull(taxCode, "taxCode");
            Objects.requireNonNull(taxRate, "taxRate");
            current288 = wholeUnits(current288, "current288");
            expired288 = wholeUnits(expired288, "expired288");
            firstTime = wholeUnits(firstTime, "firstTime");
            expiredIncentive = wholeUnits(expiredIncentive, "expiredIncentive");
            totalFrozen = wholeUnits(totalFrozen, "totalFrozen");
            frozenEqualized = wholeUnits(frozenEqualized, "frozenEqualized");
            frozenTax =
                    Objects.requireNonNull(frozenTax, "frozenTax")
                            .setScale(2, RoundingMode.UNNECESSARY);
            annexedAssessed = wholeUnits(annexedAssessed, "annexedAssessed");
            annexedEqualized = wholeUnits(annexedEqualized, "annexedEqualized");
            disconnectedAssessed = wholeUnits(disconnectedAssessed, "disconnectedAssessed");
            disconnectedEqualized = wholeUnits(disconnectedEqualized, "disconnectedEqualized");
            recoveredTifEqualized = wholeUnits(recoveredTifEqualized, "recoveredTifEqualized");
            tifCurrentEqualized = wholeUnits(tifCurrentEqualized, "tifCurrentEqualized");
            expiredIncentiveEqualized =
                    wholeUnits(expiredIncentiveEqualized, "expiredIncentiveEqualized");
        }
    }

    /// Source-ordered agency details or partial details before a missing master failure.
    public record FrozenAgencyResult(
            List<FrozenAgencyDetail> details,
            int recordsRead,
            boolean failed,
            @Nullable String missingTaxCode) {
        /// Copies details so later caller mutations cannot alter the partial or complete result.
        public FrozenAgencyResult {
            details = List.copyOf(details);
        }
    }

    /// Nine scale-zero monetary categories for one completed referenced agency group.
    public record AgencySummary(
            long agency,
            String description,
            BigDecimal newPropertyAssessed,
            BigDecimal newPropertyEqualized,
            BigDecimal annexedAssessed,
            BigDecimal annexedEqualized,
            BigDecimal disconnectedAssessed,
            BigDecimal disconnectedEqualized,
            BigDecimal recoveredTifEqualized,
            BigDecimal expiredIncentiveAssessed,
            BigDecimal expiredIncentiveEqualized) {
        /// Requires the description and normalizes every exact whole-unit amount to scale zero.
        public AgencySummary {
            Objects.requireNonNull(description, "description");
            newPropertyAssessed = wholeUnits(newPropertyAssessed, "newPropertyAssessed");
            newPropertyEqualized = wholeUnits(newPropertyEqualized, "newPropertyEqualized");
            annexedAssessed = wholeUnits(annexedAssessed, "annexedAssessed");
            annexedEqualized = wholeUnits(annexedEqualized, "annexedEqualized");
            disconnectedAssessed = wholeUnits(disconnectedAssessed, "disconnectedAssessed");
            disconnectedEqualized = wholeUnits(disconnectedEqualized, "disconnectedEqualized");
            recoveredTifEqualized = wholeUnits(recoveredTifEqualized, "recoveredTifEqualized");
            expiredIncentiveAssessed =
                    wholeUnits(expiredIncentiveAssessed, "expiredIncentiveAssessed");
            expiredIncentiveEqualized =
                    wholeUnits(expiredIncentiveEqualized, "expiredIncentiveEqualized");
        }
    }

    /// Source-ordered completed agency groups or partial groups before failure.
    public record AgencySummaryResult(
            List<AgencySummary> summaries,
            int recordsRead,
            boolean failed,
            @Nullable Long offendingAgency) {
        /// Copies summaries so later caller mutations cannot alter the partial or complete result.
        public AgencySummaryResult {
            summaries = List.copyOf(summaries);
        }
    }

    /// Agency and town classification key in source order.
    public record AgencyTownKey(long agency, int town) implements Comparable<AgencyTownKey> {
        /// Compares agency first and town second to preserve source grouping order.
        @Override
        public int compareTo(AgencyTownKey other) {
            int agencyComparison = Long.compare(agency, other.agency);
            return agencyComparison != 0 ? agencyComparison : Integer.compare(town, other.town);
        }
    }

    /// Five scale-zero equalized categories for one town or agency total.
    public record TownTotal(
            long agency,
            int town,
            @Nullable String townName,
            BigDecimal newPropertyEqualized,
            BigDecimal annexedEqualized,
            BigDecimal disconnectedEqualized,
            BigDecimal recoveredTifEqualized,
            BigDecimal expiredIncentiveEqualized) {
        /// Normalizes every exact whole-unit amount to scale zero and retains the optional name.
        public TownTotal {
            newPropertyEqualized = wholeUnits(newPropertyEqualized, "newPropertyEqualized");
            annexedEqualized = wholeUnits(annexedEqualized, "annexedEqualized");
            disconnectedEqualized = wholeUnits(disconnectedEqualized, "disconnectedEqualized");
            recoveredTifEqualized = wholeUnits(recoveredTifEqualized, "recoveredTifEqualized");
            expiredIncentiveEqualized =
                    wholeUnits(expiredIncentiveEqualized, "expiredIncentiveEqualized");
        }
    }

    /// Completed town and agency groups, including the final open groups.
    public record TownReportResult(
            List<TownTotal> townTotals,
            List<TownTotal> agencyTotals,
            boolean failed,
            @Nullable AgencyTownKey offendingKey) {
        /// Copies both lists so later caller mutations cannot alter the report result.
        public TownReportResult {
            townTotals = List.copyOf(townTotals);
            agencyTotals = List.copyOf(agencyTotals);
        }
    }

    /// Source-ordered immutable replacements, unmatched agencies, and failure position.
    public record PostingResult(
            Map<String, AgencyEqualizedValuation> updated,
            List<Long> unmatchedAgencies,
            int recordsRead,
            boolean failed,
            @Nullable Long offendingAgency) {
        /// Copies the ordered map and unmatched list so callers cannot alter posting results.
        public PostingResult {
            updated = Collections.unmodifiableMap(new LinkedHashMap<>(updated));
            unmatchedAgencies = List.copyOf(unmatchedAgencies);
        }
    }

    /// Expected business-rule failure that stops the current step without hiding prior effects.
    public static final class RuleViolation extends RuntimeException {
        private final String error;
        private final String ruleId;

        /// Creates a typed rule failure with a safe message and governing rule identifier.
        public RuleViolation(String error, String message, String ruleId) {
            super(Objects.requireNonNull(message, "message"));
            this.error = Objects.requireNonNull(error, "error");
            this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        }

        /// Returns the diagnostic required when this failure was constructed.
        @Override
        public String getMessage() {
            return Objects.requireNonNull(super.getMessage());
        }

        /// Returns the stable caller-visible failure code.
        public String error() {
            return error;
        }

        /// Returns the governing business-rule identifier.
        public String ruleId() {
            return ruleId;
        }
    }
}
