package org.cookcounty.tax.application.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.FrozenValuation;

/** Pure business-rule kernel for the nine sequential EIFD/TIF batch steps. */
public final class EifdTifIncrementKernel {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public ControlRanges validateControls(
            String reassessmentControl,
            String processingYear,
            String annualEqualizationFactor) {
        if (reassessmentControl == null || reassessmentControl.length() != 12
                || !digits(reassessmentControl.substring(0, Math.min(2, reassessmentControl.length())))) {
            throw new RuleViolation(
                    "INVALID_REASSESSMENT_CONTROL",
                    "reassessmentControl must be a complete twelve-character value with a numeric current year.",
                    "asrea740-001");
        }

        String fromAge = reassessmentControl.substring(2, 4);
        String toAge = reassessmentControl.substring(4, 6);
        String from288 = reassessmentControl.substring(8, 10);
        String to288 = reassessmentControl.substring(10, 12);
        if (!digits(fromAge) || !digits(toAge) || !digits(from288) || !digits(to288)
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
        if (annualEqualizationFactor == null || annualEqualizationFactor.length() != 5
                || !digits(annualEqualizationFactor)
                || Long.parseLong(annualEqualizationFactor) == 0L) {
            throw new RuleViolation(
                    "INVALID_EQUALIZATION_FACTOR",
                    "annualEqualizationFactor must be five numeric characters and greater than zero.",
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

    public int expandYear(String twoDigitYear) {
        if (twoDigitYear == null || twoDigitYear.length() != 2 || !digits(twoDigitYear)) {
            throw new IllegalArgumentException("twoDigitYear must contain two digits");
        }
        int year = Integer.parseInt(twoDigitYear);
        return year > 60 ? 1900 + year : 2000 + year;
    }

    /** ASREA740 truncates after every factor, rather than once after the composed expression. */
    public long derivePriorImprovement(
            long reproductionCost,
            BigDecimal occupancyFactor,
            BigDecimal conditionPercent,
            BigDecimal assessmentPercent) {
        BigDecimal stage = BigDecimal.valueOf(reproductionCost);
        if (positive(occupancyFactor)) {
            stage = truncate(stage.multiply(ONE_HUNDRED).divide(occupancyFactor, 20, RoundingMode.DOWN), 0);
        }
        if (positive(conditionPercent)) {
            stage = truncate(stage.multiply(conditionPercent).divide(ONE_HUNDRED, 20, RoundingMode.DOWN), 0);
        }
        if (positive(assessmentPercent)) {
            stage = truncate(stage.multiply(assessmentPercent).divide(ONE_HUNDRED, 20, RoundingMode.DOWN), 0);
        }
        return stage.longValueExact();
    }

    public boolean qualifiesCurrent288(
            int category,
            int assessmentClass,
            int improvementYear,
            int processingYear,
            boolean materialPermitWithBuilding) {
        return category == 3 && assessmentClass == 288
                && normalizeYear(improvementYear) == normalizeYear(processingYear)
                && materialPermitWithBuilding;
    }

    public boolean qualifiesFirstTime(int category, int age, int assessmentClass) {
        return category >= 2 && category <= 5 && age == 1 && assessmentClass != 288;
    }

    public boolean hasReconciliationDifference(ReconciliationValues values) {
        Objects.requireNonNull(values, "values");
        return values.priorBeyondFirstYear() + values.priorFirstYear() != values.priorImprovementTotal()
                || values.currentNonFrozen() + values.current288() + values.currentFirstYear()
                        + values.currentOther() != values.currentImprovementTotal();
    }

    public ClassifiedImprovement classifyImprovement(
            ImprovementClassInput input,
            ControlRanges controls) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(controls, "controls");
        long priorBeyondFirstYear = 0;
        long priorFirstYear = 0;
        long prior288 = 0;
        long currentNonFrozen = 0;
        long currentFirstYear = 0;
        long current288 = 0;
        long currentOther = 0;
        if (input.current()) {
            if (input.assessmentClass() == 288) {
                if (normalizeYear(input.improvementYear()) == normalizeYear(controls.processingYear())
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
        return new ClassifiedImprovement(priorBeyondFirstYear, priorFirstYear, prior288,
                currentNonFrozen, currentFirstYear, current288, currentOther);
    }

    public FrozenValuation selectActionTotals(DivisionActionInput input) {
        Objects.requireNonNull(input, "input");
        FrozenValuation delta = new FrozenValuation();
        delta.setDivisionNumber(input.division());
        delta.setPriorLandValue(input.priorLand());
        delta.setPriorImprovementValue(input.priorImprovement());
        delta.setPriorTotalValue(input.priorTotal());
        delta.setPriorParcelCount(input.priorParcelCount());
        delta.setCurrentLandValue(input.currentLand());
        delta.setCurrentImprovementValue(input.currentImprovement());
        delta.setCurrentTotalValue(input.currentTotal());
        delta.setCurrentParcelCount(input.currentParcelCount());
        boolean exemptTransition = input.fullyExemptPriorBecameNonexempt();
        long proposedImprovement = exemptTransition
                ? input.currentTotal() : input.proposedImprovement();
        long proposedExpired288 = exemptTransition ? 0 : input.proposedExpired288();
        long proposedCurrent288 = exemptTransition ? 0 : input.proposedCurrent288();
        delta.setProposedImprovementValue(proposedImprovement);
        delta.setProposedExpired288Value(proposedExpired288);
        delta.setProposedCurrent288Value(proposedCurrent288);
        delta.setProposedTotalValue(
                proposedImprovement + proposedExpired288 + proposedCurrent288);
        delta.setChangeActionPriorLandValue(0L);
        delta.setChangeActionPriorImprovementValue(0L);
        delta.setChangeActionPriorTotalValue(0L);
        delta.setChangeActionPriorParcelCount(0L);
        delta.setChangeActionCurrentLandValue(0L);
        delta.setChangeActionCurrentImprovementValue(0L);
        delta.setChangeActionCurrentTotalValue(0L);
        delta.setChangeActionCurrentParcelCount(0L);
        delta.setNoChangeActionPriorLandValue(0L);
        delta.setNoChangeActionPriorImprovementValue(0L);
        delta.setNoChangeActionPriorTotalValue(0L);
        delta.setNoChangeActionPriorParcelCount(0L);
        delta.setNoChangeActionCurrentLandValue(0L);
        delta.setNoChangeActionCurrentImprovementValue(0L);
        delta.setNoChangeActionCurrentTotalValue(0L);
        delta.setNoChangeActionCurrentParcelCount(0L);

        /*
         * In ASREA740, the action-routing IF at 00884 is still inside
         * IF WS-C5 GREATER ZERO at 00878. Because WS-C5 itself is one of the
         * change-action alternatives, that reachable branch can only update
         * FV-CA; FV-WOCA is unreachable. A permit/Class-288 marker or the
         * zero four-digit township prefix cannot update either bucket by itself.
         */
        if (!exemptTransition && input.qualifyingNewValue() > 0) {
            delta.setChangeActionPriorLandValue(input.priorLand());
            delta.setChangeActionPriorImprovementValue(input.priorImprovement());
            delta.setChangeActionPriorTotalValue(input.priorTotal());
            delta.setChangeActionPriorParcelCount(input.priorParcelCount());
            delta.setChangeActionCurrentLandValue(input.currentLand());
            delta.setChangeActionCurrentImprovementValue(input.currentImprovement());
            delta.setChangeActionCurrentTotalValue(input.currentTotal());
            delta.setChangeActionCurrentParcelCount(input.currentParcelCount());
        }
        return delta;
    }

    public FrozenValuation accumulateFrozen(FrozenValuation stored, FrozenValuation delta) {
        Objects.requireNonNull(delta, "delta");
        FrozenValuation result = copy(stored == null ? new FrozenValuation() : stored);
        if (result.getDivisionNumber() == null) {
            result.setDivisionNumber(delta.getDivisionNumber());
        }
        result.setPriorLandValue(add(result.getPriorLandValue(), delta.getPriorLandValue()));
        result.setPriorImprovementValue(add(result.getPriorImprovementValue(), delta.getPriorImprovementValue()));
        result.setPriorTotalValue(add(result.getPriorTotalValue(), delta.getPriorTotalValue()));
        result.setPriorParcelCount(add(result.getPriorParcelCount(), delta.getPriorParcelCount()));
        result.setCurrentLandValue(add(result.getCurrentLandValue(), delta.getCurrentLandValue()));
        result.setCurrentImprovementValue(add(result.getCurrentImprovementValue(), delta.getCurrentImprovementValue()));
        result.setCurrentTotalValue(add(result.getCurrentTotalValue(), delta.getCurrentTotalValue()));
        result.setCurrentParcelCount(add(result.getCurrentParcelCount(), delta.getCurrentParcelCount()));
        result.setProposedImprovementValue(add(result.getProposedImprovementValue(), delta.getProposedImprovementValue()));
        result.setProposedExpired288Value(add(result.getProposedExpired288Value(), delta.getProposedExpired288Value()));
        result.setProposedCurrent288Value(add(result.getProposedCurrent288Value(), delta.getProposedCurrent288Value()));
        result.setProposedTotalValue(add(result.getProposedTotalValue(), delta.getProposedTotalValue()));
        result.setProposedActualValue(add(result.getProposedActualValue(), delta.getProposedActualValue()));
        result.setChangeActionPriorLandValue(add(result.getChangeActionPriorLandValue(), delta.getChangeActionPriorLandValue()));
        result.setChangeActionPriorImprovementValue(add(result.getChangeActionPriorImprovementValue(), delta.getChangeActionPriorImprovementValue()));
        result.setChangeActionPriorTotalValue(add(result.getChangeActionPriorTotalValue(), delta.getChangeActionPriorTotalValue()));
        result.setChangeActionPriorParcelCount(add(result.getChangeActionPriorParcelCount(), delta.getChangeActionPriorParcelCount()));
        result.setChangeActionCurrentLandValue(add(result.getChangeActionCurrentLandValue(), delta.getChangeActionCurrentLandValue()));
        result.setChangeActionCurrentImprovementValue(add(result.getChangeActionCurrentImprovementValue(), delta.getChangeActionCurrentImprovementValue()));
        result.setChangeActionCurrentTotalValue(add(result.getChangeActionCurrentTotalValue(), delta.getChangeActionCurrentTotalValue()));
        result.setChangeActionCurrentParcelCount(add(result.getChangeActionCurrentParcelCount(), delta.getChangeActionCurrentParcelCount()));
        result.setNoChangeActionPriorLandValue(add(result.getNoChangeActionPriorLandValue(), delta.getNoChangeActionPriorLandValue()));
        result.setNoChangeActionPriorImprovementValue(add(result.getNoChangeActionPriorImprovementValue(), delta.getNoChangeActionPriorImprovementValue()));
        result.setNoChangeActionPriorTotalValue(add(result.getNoChangeActionPriorTotalValue(), delta.getNoChangeActionPriorTotalValue()));
        result.setNoChangeActionPriorParcelCount(add(result.getNoChangeActionPriorParcelCount(), delta.getNoChangeActionPriorParcelCount()));
        result.setNoChangeActionCurrentLandValue(add(result.getNoChangeActionCurrentLandValue(), delta.getNoChangeActionCurrentLandValue()));
        result.setNoChangeActionCurrentImprovementValue(add(result.getNoChangeActionCurrentImprovementValue(), delta.getNoChangeActionCurrentImprovementValue()));
        result.setNoChangeActionCurrentTotalValue(add(result.getNoChangeActionCurrentTotalValue(), delta.getNoChangeActionCurrentTotalValue()));
        result.setNoChangeActionCurrentParcelCount(add(result.getNoChangeActionCurrentParcelCount(), delta.getNoChangeActionCurrentParcelCount()));
        return result;
    }

    public boolean shouldInsertFrozen(FrozenValuation value) {
        return nonzero(value.getProposedImprovementValue())
                || nonzero(value.getProposedExpired288Value())
                || nonzero(value.getProposedCurrent288Value())
                || nonzero(value.getProposedTotalValue())
                || nonzero(value.getProposedActualValue())
                || nonzero(value.getChangeActionPriorLandValue())
                || nonzero(value.getChangeActionPriorImprovementValue())
                || nonzero(value.getChangeActionPriorTotalValue())
                || nonzero(value.getChangeActionPriorParcelCount())
                || nonzero(value.getChangeActionCurrentLandValue())
                || nonzero(value.getChangeActionCurrentImprovementValue())
                || nonzero(value.getChangeActionCurrentTotalValue())
                || nonzero(value.getChangeActionCurrentParcelCount());
    }

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
        long taxCurrent = first.current288();
        long taxFirst = first.firstTime();
        long divisionCurrent = first.current288();
        long divisionFirst = first.firstTime();
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
                divisionTotals.add(new DivisionTotal(
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
                divisionCurrent += current.current288();
                divisionFirst += current.firstTime();
                groupCount++;
            } else {
                taxCurrent += current.current288();
                taxFirst += current.firstTime();
                divisionCurrent += current.current288();
                divisionFirst += current.firstTime();
            }
        }

        if (taxFirst > 0) {
            taxTotals.add(new TaxCodeTotal(division, taxCode, taxCurrent, taxFirst));
        }
        if (divisionFirst > 0) {
            divisionTotals.add(new DivisionTotal(
                    division, taxCode, groupCount, divisionCurrent, divisionFirst));
        }
        return new RollupResult(List.copyOf(taxTotals), List.copyOf(divisionTotals), read, failed, offendingKey);
    }

    public PercentageResult percentages(
            List<TaxCodeTotal> taxCodeTotals,
            List<DivisionTotal> divisionTotals) {
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
                percentages.add(new PercentageAllocation(
                        tax.division(),
                        tax.taxCode(),
                        division.taxCodeCount(),
                        share(tax.current288(), division.current288()),
                        share(tax.firstTime(), division.firstTime()),
                        0L,
                        0L,
                        0L,
                        0L));
                taxIndex++;
            }
        }
        while (taxIndex < taxCodeTotals.size()) {
            unmatched.add(taxCodeTotals.get(taxIndex++));
        }
        return new PercentageResult(List.copyOf(percentages), List.copyOf(unmatched));
    }

    public AllocationResult allocate(
            List<PercentageAllocation> percentages,
            Map<String, FrozenValuation> valuations) {
        List<AllocatedFrozen> allocated = new ArrayList<>();
        int unmatched = 0;
        for (PercentageAllocation percentage : percentages) {
            FrozenValuation valuation = valuations.get(percentage.division());
            if (valuation == null) {
                unmatched++;
                continue;
            }
            long firstTime;
            long current288;
            long expired288;
            if (percentage.taxCodeCount() == 1) {
                firstTime = value(valuation.getProposedImprovementValue());
                current288 = value(valuation.getProposedCurrent288Value());
                expired288 = value(valuation.getProposedExpired288Value());
            } else {
                firstTime = allocatePositive(value(valuation.getProposedImprovementValue()), percentage.firstTimeShare());
                current288 = allocatePositive(value(valuation.getProposedCurrent288Value()), percentage.current288Share());
                expired288 = value(valuation.getProposedExpired288Value()) / percentage.taxCodeCount();
            }
            allocated.add(new AllocatedFrozen(
                    percentage.division(), percentage.taxCode(), percentage.taxCodeCount(),
                    percentage.current288Share(), percentage.firstTimeShare(),
                    current288, expired288, firstTime, 0L));
        }
        return new AllocationResult(List.copyOf(allocated), unmatched);
    }

    /** ASREA748 consumes the sequential ASREA745 handoff; it never reads the indexed FRZAGIN store. */
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
            long assessed = frozen.current288() + frozen.expired288() + frozen.firstTime();
            long equalized = BigDecimal.valueOf(assessed).multiply(factor)
                    .setScale(0, RoundingMode.HALF_UP).longValueExact();
            BigDecimal tax = BigDecimal.valueOf(assessed).multiply(master.taxRate())
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            long expiredIncentiveEqualized = BigDecimal.valueOf(frozen.expiredIncentive())
                    .multiply(factor).setScale(0, RoundingMode.HALF_UP).longValueExact();
            for (Long agency : master.agencyPositions()) {
                if (agency != null && agency > 0) {
                    details.add(new FrozenAgencyDetail(
                            frozen.taxCode(), agency, master.town(), master.taxRate(),
                            frozen.current288(), frozen.expired288(), frozen.firstTime(),
                            frozen.expiredIncentive(), assessed, equalized, tax,
                            0L, 0L, 0L, 0L, 0L, 0L, expiredIncentiveEqualized));
                }
            }
        }
        return new FrozenAgencyResult(List.copyOf(details), read, false, null);
    }

    public AgencySummaryResult summarizeAgencies(
            List<FrozenAgencyDetail> details,
            Map<Long, String> agencyDescriptions) {
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
                        return new AgencySummaryResult(List.copyOf(summaries), read, true, current.agency);
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

    public TownReportResult reportTowns(
            List<FrozenAgencyDetail> details,
            Map<Integer, String> townNames) {
        Map<AgencyTownKey, TownTotal> townTotals = new LinkedHashMap<>();
        AgencyTownKey previous = null;
        for (FrozenAgencyDetail detail : details) {
            AgencyTownKey key = new AgencyTownKey(detail.agency(), detail.town());
            if (previous != null && key.compareTo(previous) < 0) {
                return townReport(townTotals, true, key);
            }
            int position = detail.town() >= 10 && detail.town() <= 39
                    ? detail.town() - 9 : detail.town() - 39;
            String townName = townNames.get(position);
            townTotals.compute(key, (unused, existing) -> {
                TownTotal base = existing == null
                        ? new TownTotal(detail.agency(), detail.town(), townName, 0, 0, 0, 0, 0)
                        : existing;
                return new TownTotal(base.agency(), base.town(), base.townName(),
                        base.newPropertyEqualized() + detail.frozenEqualized(),
                        base.annexedEqualized() + detail.annexedEqualized(),
                        base.disconnectedEqualized() + detail.disconnectedEqualized(),
                        base.recoveredTifEqualized() + detail.recoveredTifEqualized(),
                        base.expiredIncentiveEqualized() + detail.expiredIncentiveEqualized());
            });
            previous = key;
        }
        return townReport(townTotals, false, null);
    }

    private TownReportResult townReport(
            Map<AgencyTownKey, TownTotal> townTotals,
            boolean failed,
            AgencyTownKey offendingKey) {
        Map<Long, TownTotal> agencyTotals = new LinkedHashMap<>();
        for (TownTotal town : townTotals.values()) {
            agencyTotals.compute(town.agency(), (unused, existing) -> {
                TownTotal base = existing == null
                        ? new TownTotal(town.agency(), 0, null, 0, 0, 0, 0, 0)
                        : existing;
                return new TownTotal(base.agency(), 0, null,
                        base.newPropertyEqualized() + town.newPropertyEqualized(),
                        base.annexedEqualized() + town.annexedEqualized(),
                        base.disconnectedEqualized() + town.disconnectedEqualized(),
                        base.recoveredTifEqualized() + town.recoveredTifEqualized(),
                        base.expiredIncentiveEqualized() + town.expiredIncentiveEqualized());
            });
        }
        return new TownReportResult(
                List.copyOf(townTotals.values()),
                List.copyOf(agencyTotals.values()),
                failed,
                offendingKey);
    }

    public PostingResult postAgencySummaries(
            List<AgencySummary> summaries,
            Map<String, AgencyEqualizedValuation> existing) {
        Map<String, AgencyEqualizedValuation> updated = new LinkedHashMap<>();
        List<Long> unmatched = new ArrayList<>();
        long previous = Long.MIN_VALUE;
        int read = 0;
        for (AgencySummary summary : summaries) {
            read++;
            if (summary.agency() <= previous) {
                return new PostingResult(orderedCopy(updated), List.copyOf(unmatched), read, true, summary.agency());
            }
            String key = String.format("%09d", summary.agency());
            AgencyEqualizedValuation stored = existing.get(key);
            if (stored == null) {
                unmatched.add(summary.agency());
            } else {
                AgencyEqualizedValuation replacement = copy(stored);
                replacement.setNewPropertyEqualizedValue(
                        summary.newPropertyEqualized() + summary.expiredIncentiveEqualized());
                replacement.setAnnexedPropertyEqualizedValue(summary.annexedEqualized());
                replacement.setDisconnectedPropertyEqualizedValue(summary.disconnectedEqualized());
                replacement.setDisconnectedTifDifference(summary.recoveredTifEqualized());
                updated.put(key, replacement);
            }
            previous = summary.agency();
        }
        return new PostingResult(orderedCopy(updated), List.copyOf(unmatched), read, false, null);
    }

    private static <K, V> Map<K, V> orderedCopy(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private void validateStrictTaxTotals(List<TaxCodeTotal> totals) {
        for (int index = 1; index < totals.size(); index++) {
            TaxCodeTotal previous = totals.get(index - 1);
            TaxCodeTotal current = totals.get(index);
            if (compare(current.division(), current.taxCode(), previous.division(), previous.taxCode()) <= 0) {
                throw new RuleViolation("OUT_OF_SEQUENCE",
                        "Tax-code totals must be strictly ascending by division and tax code.", "asrea744-001");
            }
        }
    }

    private void validateStrictDivisionTotals(List<DivisionTotal> totals) {
        for (int index = 1; index < totals.size(); index++) {
            DivisionTotal previous = totals.get(index - 1);
            DivisionTotal current = totals.get(index);
            if (compare(current.division(), current.lastTaxCode(), previous.division(), previous.lastTaxCode()) <= 0) {
                throw new RuleViolation("OUT_OF_SEQUENCE",
                        "Division totals must be strictly ascending by division and tax code.", "asrea744-001");
            }
        }
    }

    private BigDecimal share(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(7);
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 7, RoundingMode.DOWN);
    }

    private long allocatePositive(long amount, BigDecimal percentage) {
        if (percentage == null || percentage.signum() <= 0) {
            return 0L;
        }
        return BigDecimal.valueOf(amount).multiply(percentage).setScale(0, RoundingMode.DOWN).longValueExact();
    }

    private int compare(String division, String taxCode, String otherDivision, String otherTaxCode) {
        int divisionComparison = division.compareTo(otherDivision);
        return divisionComparison != 0 ? divisionComparison : taxCode.compareTo(otherTaxCode);
    }

    private int normalizeYear(int year) {
        return year < 100 ? (year > 60 ? 1900 + year : 2000 + year) : year;
    }

    private static boolean digits(String value) {
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

    private static boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private static BigDecimal truncate(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.DOWN);
    }

    private static boolean nonzero(Long value) {
        return value != null && value != 0L;
    }

    private static long value(Long value) {
        return value == null ? 0L : value;
    }

    private static Long add(Long left, Long right) {
        return value(left) + value(right);
    }

    private static FrozenValuation copy(FrozenValuation source) {
        FrozenValuation target = new FrozenValuation();
        target.setId(source.getId());
        target.setDivisionNumber(source.getDivisionNumber());
        target.setPriorLandValue(source.getPriorLandValue());
        target.setPriorImprovementValue(source.getPriorImprovementValue());
        target.setPriorTotalValue(source.getPriorTotalValue());
        target.setPriorParcelCount(source.getPriorParcelCount());
        target.setCurrentLandValue(source.getCurrentLandValue());
        target.setCurrentImprovementValue(source.getCurrentImprovementValue());
        target.setCurrentTotalValue(source.getCurrentTotalValue());
        target.setCurrentParcelCount(source.getCurrentParcelCount());
        target.setProposedImprovementValue(source.getProposedImprovementValue());
        target.setProposedExpired288Value(source.getProposedExpired288Value());
        target.setProposedCurrent288Value(source.getProposedCurrent288Value());
        target.setProposedTotalValue(source.getProposedTotalValue());
        target.setProposedActualValue(source.getProposedActualValue());
        target.setChangeActionPriorLandValue(source.getChangeActionPriorLandValue());
        target.setChangeActionPriorImprovementValue(source.getChangeActionPriorImprovementValue());
        target.setChangeActionPriorTotalValue(source.getChangeActionPriorTotalValue());
        target.setChangeActionPriorParcelCount(source.getChangeActionPriorParcelCount());
        target.setChangeActionCurrentLandValue(source.getChangeActionCurrentLandValue());
        target.setChangeActionCurrentImprovementValue(source.getChangeActionCurrentImprovementValue());
        target.setChangeActionCurrentTotalValue(source.getChangeActionCurrentTotalValue());
        target.setChangeActionCurrentParcelCount(source.getChangeActionCurrentParcelCount());
        target.setNoChangeActionPriorLandValue(source.getNoChangeActionPriorLandValue());
        target.setNoChangeActionPriorImprovementValue(source.getNoChangeActionPriorImprovementValue());
        target.setNoChangeActionPriorTotalValue(source.getNoChangeActionPriorTotalValue());
        target.setNoChangeActionPriorParcelCount(source.getNoChangeActionPriorParcelCount());
        target.setNoChangeActionCurrentLandValue(source.getNoChangeActionCurrentLandValue());
        target.setNoChangeActionCurrentImprovementValue(source.getNoChangeActionCurrentImprovementValue());
        target.setNoChangeActionCurrentTotalValue(source.getNoChangeActionCurrentTotalValue());
        target.setNoChangeActionCurrentParcelCount(source.getNoChangeActionCurrentParcelCount());
        return target;
    }

    private static AgencyEqualizedValuation copy(AgencyEqualizedValuation source) {
        AgencyEqualizedValuation target = new AgencyEqualizedValuation();
        target.setId(source.getId());
        target.setAgencyNumber(source.getAgencyNumber());
        target.setCookCountyRealEstateValue(source.getCookCountyRealEstateValue());
        target.setCookCountyAirPollutionValue(source.getCookCountyAirPollutionValue());
        target.setCookCountyUseTaxValue(source.getCookCountyUseTaxValue());
        target.setCookCountyRailroadValue(source.getCookCountyRailroadValue());
        target.setDuPageCountyEqualizedValue(source.getDuPageCountyEqualizedValue());
        target.setLakeCountyEqualizedValue(source.getLakeCountyEqualizedValue());
        target.setKankakeeCountyEqualizedValue(source.getKankakeeCountyEqualizedValue());
        target.setKendallCountyEqualizedValue(source.getKendallCountyEqualizedValue());
        target.setLaSalleCountyEqualizedValue(source.getLaSalleCountyEqualizedValue());
        target.setMcHenryCountyEqualizedValue(source.getMcHenryCountyEqualizedValue());
        target.setGrundyCountyEqualizedValue(source.getGrundyCountyEqualizedValue());
        target.setDeKalbCountyEqualizedValue(source.getDeKalbCountyEqualizedValue());
        target.setLivingstonCountyEqualizedValue(source.getLivingstonCountyEqualizedValue());
        target.setKaneCountyEqualizedValue(source.getKaneCountyEqualizedValue());
        target.setWillCountyEqualizedValue(source.getWillCountyEqualizedValue());
        target.setParentAgency1(source.getParentAgency1());
        target.setParentAgency2(source.getParentAgency2());
        target.setParentAgency3(source.getParentAgency3());
        target.setParentAgency4(source.getParentAgency4());
        target.setParentAgency5(source.getParentAgency5());
        target.setBurdenPercent(source.getBurdenPercent());
        target.setTaxYear(source.getTaxYear());
        target.setTaxCapIndicator(source.getTaxCapIndicator());
        target.setConnectingAgency1(source.getConnectingAgency1());
        target.setConnectingAgency2(source.getConnectingAgency2());
        target.setConnectingAgency3(source.getConnectingAgency3());
        target.setConnectingAgency4(source.getConnectingAgency4());
        target.setNewPropertyEqualizedValue(source.getNewPropertyEqualizedValue());
        target.setAnnexedPropertyEqualizedValue(source.getAnnexedPropertyEqualizedValue());
        target.setDisconnectedPropertyEqualizedValue(source.getDisconnectedPropertyEqualizedValue());
        target.setDisconnectedTifDifference(source.getDisconnectedTifDifference());
        target.setOverlapNewPropertyEqualizedValue(source.getOverlapNewPropertyEqualizedValue());
        target.setOverlapAnnexedPropertyEqualizedValue(source.getOverlapAnnexedPropertyEqualizedValue());
        target.setOverlapDisconnectedPropertyEqualizedValue(source.getOverlapDisconnectedPropertyEqualizedValue());
        target.setOverlapDisconnectedTifDifference(source.getOverlapDisconnectedTifDifference());
        target.setPreviousTaxYear1(source.getPreviousTaxYear1());
        target.setPreviousTaxYear1Extension(source.getPreviousTaxYear1Extension());
        target.setPreviousTaxYear2(source.getPreviousTaxYear2());
        target.setPreviousTaxYear2Extension(source.getPreviousTaxYear2Extension());
        target.setPreviousTaxYear3(source.getPreviousTaxYear3());
        target.setPreviousTaxYear3Extension(source.getPreviousTaxYear3Extension());
        target.setLimitingTaxRateOverride(source.getLimitingTaxRateOverride());
        return target;
    }

    private static final class MutableAgencySummary {
        private final long agency;
        private long newAssessed;
        private long newEqualized;
        private long annexedAssessed;
        private long annexedEqualized;
        private long disconnectedAssessed;
        private long disconnectedEqualized;
        private long recoveredTif;
        private long expiredAssessed;
        private long expiredEqualized;

        private MutableAgencySummary(long agency) {
            this.agency = agency;
        }

        private void add(FrozenAgencyDetail detail) {
            newAssessed += detail.totalFrozen();
            newEqualized += detail.frozenEqualized();
            annexedAssessed += detail.annexedAssessed();
            annexedEqualized += detail.annexedEqualized();
            disconnectedAssessed += detail.disconnectedAssessed();
            disconnectedEqualized += detail.disconnectedEqualized();
            recoveredTif += detail.recoveredTifEqualized();
            expiredAssessed += detail.expiredIncentive();
            expiredEqualized += detail.expiredIncentiveEqualized();
        }

        private AgencySummary freeze(String description) {
            return new AgencySummary(agency, description, newAssessed, newEqualized,
                    annexedAssessed, annexedEqualized, disconnectedAssessed,
                    disconnectedEqualized, recoveredTif, expiredAssessed, expiredEqualized);
        }
    }

    public record ControlRanges(int currentYear, int fromAge, int toAge, int from288Year,
            int to288Year, int processingYear, BigDecimal equalizationFactor) {}

    public record ImprovementClassInput(boolean current, long value, int age,
            int assessmentClass, int improvementYear, boolean materialPermitWithBuilding) {}

    public record ClassifiedImprovement(long priorBeyondFirstYear, long priorFirstYear,
            long prior288, long currentNonFrozen, long currentFirstYear,
            long current288, long currentOther) {}

    public record DivisionActionInput(String division, String townshipPrefix,
            long priorLand, long priorImprovement, long priorTotal, long priorParcelCount,
            long currentLand, long currentImprovement, long currentTotal, long currentParcelCount,
            long proposedImprovement, long proposedExpired288, long proposedCurrent288,
            long qualifyingNewValue, boolean permitChanged,
            boolean fullyExemptPriorBecameNonexempt) {
        public DivisionActionInput {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(townshipPrefix, "townshipPrefix");
        }
    }

    public record ReconciliationValues(long priorBeyondFirstYear, long priorFirstYear,
            long priorImprovementTotal, long currentNonFrozen, long current288,
            long currentFirstYear, long currentOther, long currentImprovementTotal) {}

    public record Selection(String division, String taxCode, long current288, long firstTime,
            long expired288) {
        public Selection {
            Objects.requireNonNull(division, "division");
            Objects.requireNonNull(taxCode, "taxCode");
        }
    }

    public record TaxCodeTotal(String division, String taxCode, long current288, long firstTime) {}

    public record DivisionTotal(String division, String lastTaxCode, int taxCodeCount,
            long current288, long firstTime) {}

    public record RollupResult(List<TaxCodeTotal> taxCodeTotals,
            List<DivisionTotal> divisionTotals, int recordsRead, boolean failed,
            String offendingKey) {}

    public record PercentageAllocation(String division, String taxCode, int taxCodeCount,
            BigDecimal current288Share, BigDecimal firstTimeShare, long current288,
            long expired288, long firstTime, long expiredIncentive) {}

    public record PercentageResult(List<PercentageAllocation> percentages,
            List<TaxCodeTotal> unmatchedTaxCodeTotals) {}

    public record AllocatedFrozen(String division, String taxCode, int taxCodeCount,
            BigDecimal current288Share, BigDecimal firstTimeShare, long current288,
            long expired288, long firstTime, long expiredIncentive) {}

    public record AllocationResult(List<AllocatedFrozen> allocations, int unmatchedCount) {}

    public record TaxCodeMaster(String taxCode, BigDecimal taxRate, int town,
            List<Long> agencyPositions) {
        public TaxCodeMaster {
            Objects.requireNonNull(taxCode, "taxCode");
            Objects.requireNonNull(taxRate, "taxRate");
            agencyPositions = List.copyOf(agencyPositions);
            if (agencyPositions.size() != 40) {
                throw new IllegalArgumentException("A tax-code master must expose exactly 40 agency positions");
            }
        }
    }

    public record FrozenAgencyDetail(String taxCode, long agency, int town, BigDecimal taxRate,
            long current288, long expired288, long firstTime, long expiredIncentive,
            long totalFrozen, long frozenEqualized, BigDecimal frozenTax,
            long annexedAssessed, long annexedEqualized, long disconnectedAssessed,
            long disconnectedEqualized, long recoveredTifEqualized,
            long tifCurrentEqualized, long expiredIncentiveEqualized) {}

    public record FrozenAgencyResult(List<FrozenAgencyDetail> details, int recordsRead,
            boolean failed, String missingTaxCode) {}

    public record AgencySummary(long agency, String description, long newPropertyAssessed,
            long newPropertyEqualized, long annexedAssessed, long annexedEqualized,
            long disconnectedAssessed, long disconnectedEqualized, long recoveredTifEqualized,
            long expiredIncentiveAssessed, long expiredIncentiveEqualized) {}

    public record AgencySummaryResult(List<AgencySummary> summaries, int recordsRead,
            boolean failed, Long offendingAgency) {}

    public record AgencyTownKey(long agency, int town) implements Comparable<AgencyTownKey> {
        @Override
        public int compareTo(AgencyTownKey other) {
            int agencyComparison = Long.compare(agency, other.agency);
            return agencyComparison != 0 ? agencyComparison : Integer.compare(town, other.town);
        }
    }

    public record TownTotal(long agency, int town, String townName,
            long newPropertyEqualized, long annexedEqualized, long disconnectedEqualized,
            long recoveredTifEqualized, long expiredIncentiveEqualized) {}

    public record TownReportResult(List<TownTotal> townTotals,
            List<TownTotal> agencyTotals, boolean failed, AgencyTownKey offendingKey) {}

    public record PostingResult(Map<String, AgencyEqualizedValuation> updated,
            List<Long> unmatchedAgencies, int recordsRead, boolean failed,
            Long offendingAgency) {}

    public static final class RuleViolation extends RuntimeException {
        private final String error;
        private final String ruleId;

        public RuleViolation(String error, String message, String ruleId) {
            super(message);
            this.error = Objects.requireNonNull(error, "error");
            this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        }

        public String error() {
            return error;
        }

        public String ruleId() {
            return ruleId;
        }
    }
}