package org.cookcounty.tax.application.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AgencySummary;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AllocatedFrozen;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.DivisionTotal;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.DivisionActionInput;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.FrozenAgencyDetail;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.ImprovementClassInput;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.PercentageAllocation;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.ReconciliationValues;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.RuleViolation;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.Selection;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.TaxCodeMaster;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.TaxCodeTotal;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.junit.jupiter.api.Test;

/** Synthetic inputs in this class are rule examples, not captured-parity claims. */
class EifdTifIncrementKernelTest {

    private final EifdTifIncrementKernel kernel = new EifdTifIncrementKernel();

    @Test
    void controlsValidateAllStartupRulesAndCenturyPivot() {
        var controls = kernel.validateControls("260181202526", "26", "10000");

        assertEquals(1, controls.fromAge());
        assertEquals(81, controls.toAge());
        assertEquals(2025, controls.from288Year());
        assertEquals(2026, controls.to288Year());
        assertEquals(new BigDecimal("1.0000"), controls.equalizationFactor());
        assertEquals(1999, kernel.expandYear("99"));
        assertEquals(2000, kernel.expandYear("00"));
        assertEquals(2060, kernel.expandYear("60"));
        assertEquals(1961, kernel.expandYear("61"));
    }

    @Test
    void controlsRejectBeforeProcessingWithTheAcceptedRule() {
        assertEquals("asrea740-001",
                assertThrows(RuleViolation.class,
                        () -> kernel.validateControls("X60181202526", "26", "10000"))
                        .ruleId());
        assertEquals("asrea740-002",
                assertThrows(RuleViolation.class,
                        () -> kernel.validateControls("268101202526", "26", "10000"))
                        .ruleId());
        assertEquals("asrea742-005",
                assertThrows(RuleViolation.class,
                        () -> kernel.validateControls("260181202526", "2", "10000"))
                        .ruleId());
        assertEquals("asrea742-006",
                assertThrows(RuleViolation.class,
                        () -> kernel.validateControls("260181202526", "2X", "10000"))
                        .ruleId());
        assertEquals("asrea748-001",
                assertThrows(RuleViolation.class,
                        () -> kernel.validateControls("260181202526", "26", "00000"))
                        .ruleId());
    }

    @Test
    void priorImprovementTruncatesAtEveryPositiveFactorStage() {
        // Rule example: 101 * 100 / 80 = 126.25 -> 126; * 75% = 94.5 -> 94;
        // * 33% = 31.02 -> 31. A single final truncation would produce 31 as well,
        // while the second assertion distinguishes missing-factor pass-through.
        assertEquals(31L, kernel.derivePriorImprovement(
                101, new BigDecimal("80"), new BigDecimal("75"), new BigDecimal("33")));
        assertEquals(50L, kernel.derivePriorImprovement(
                101, BigDecimal.ZERO, new BigDecimal("50"), null));
    }

    @Test
    void classificationRequiresEveryCurrent288GateAndExcludesQuestionnaireCompanionsByInput() {
        assertTrue(kernel.qualifiesCurrent288(3, 288, 2026, 26, true));
        assertFalse(kernel.qualifiesCurrent288(3, 288, 2026, 26, false));
        assertFalse(kernel.qualifiesCurrent288(2, 288, 2026, 26, true));
        assertTrue(kernel.qualifiesFirstTime(2, 1, 200));
        assertFalse(kernel.qualifiesFirstTime(5, 1, 288));
        assertFalse(kernel.qualifiesFirstTime(6, 1, 200));
    }

    @Test
    void reconciliationReportsEitherPriorOrCurrentDifferenceWithoutBlockingPersistence() {
        assertFalse(kernel.hasReconciliationDifference(
                new ReconciliationValues(10, 5, 15, 2, 3, 4, 1, 10)));
        assertTrue(kernel.hasReconciliationDifference(
                new ReconciliationValues(10, 5, 16, 2, 3, 4, 1, 10)));
    }

    @Test
    void reassessmentClassificationSeparatesAllPriorAndCurrentCategories() {
        var controls = kernel.validateControls("260181202526", "26", "10000");

        assertEquals(10L, kernel.classifyImprovement(
                new ImprovementClassInput(true, 10, 1, 200, 2024, false), controls)
                .currentFirstYear());
        assertEquals(11L, kernel.classifyImprovement(
                new ImprovementClassInput(true, 11, 2, 200, 2024, false), controls)
                .currentNonFrozen());
        assertEquals(12L, kernel.classifyImprovement(
                new ImprovementClassInput(true, 12, 2, 288, 2026, true), controls)
                .current288());
        assertEquals(13L, kernel.classifyImprovement(
                new ImprovementClassInput(false, 13, 1, 200, 2024, false), controls)
                .priorFirstYear());
        assertEquals(14L, kernel.classifyImprovement(
                new ImprovementClassInput(false, 14, 2, 288, 2025, false), controls)
                .prior288());
    }

    @Test
    void actionBucketsFollowTheReachableCobolScope() {
        FrozenValuation noQualifyingNewValue =
                kernel.selectActionTotals(actionInput("12", "1200", 0, false, false));
        assertEquals(0L, noQualifyingNewValue.getChangeActionCurrentTotalValue());
        assertEquals(0L, noQualifyingNewValue.getNoChangeActionCurrentTotalValue());

        FrozenValuation qualifyingNewValue =
                kernel.selectActionTotals(actionInput("12", "1200", 5, false, false));
        assertEquals(30L, qualifyingNewValue.getChangeActionCurrentTotalValue());
        assertEquals(0L, qualifyingNewValue.getNoChangeActionCurrentTotalValue());

        assertEquals(0L, kernel.selectActionTotals(
                actionInput("12", "1200", 0, false, false, 9))
                .getChangeActionCurrentTotalValue());
        assertEquals(0L, kernel.selectActionTotals(
                actionInput("12", "1200", 0, true, false))
                .getChangeActionCurrentTotalValue());
        assertEquals(0L, kernel.selectActionTotals(
                actionInput("12", "0000", 0, false, false))
                .getChangeActionCurrentTotalValue());

        FrozenValuation exemptTransition =
                kernel.selectActionTotals(actionInput("12", "1200", 5, true, true, 9));
        assertEquals(30L, exemptTransition.getProposedImprovementValue());
        assertEquals(30L, exemptTransition.getProposedTotalValue());
        assertEquals(0L, exemptTransition.getProposedCurrent288Value());
        assertEquals(0L, exemptTransition.getChangeActionCurrentTotalValue());
        assertEquals(0L, exemptTransition.getNoChangeActionCurrentTotalValue());
    }

    @Test
    void frozenValuesAccumulateAndAllZeroMissingDivisionIsSuppressed() {
        FrozenValuation stored = frozen("0001", 10, 20, 30);
        FrozenValuation delta = frozen("0001", 5, 7, 9);
        delta.setChangeActionCurrentTotalValue(2L);

        FrozenValuation accumulated = kernel.accumulateFrozen(stored, delta);

        assertEquals(15L, accumulated.getProposedImprovementValue());
        assertEquals(27L, accumulated.getProposedExpired288Value());
        assertEquals(39L, accumulated.getProposedCurrent288Value());
        assertEquals(2L, accumulated.getChangeActionCurrentTotalValue());
        assertTrue(kernel.shouldInsertFrozen(accumulated));
        assertFalse(kernel.shouldInsertFrozen(new FrozenValuation()));
    }

    @Test
    void rollupGroupsDuplicatesAndClosesInnerAndOuterGroups() {
        var result = kernel.rollup(List.of(
                new Selection("01", "100", 2, 3, 99),
                new Selection("01", "100", 5, 7, 99),
                new Selection("01", "200", 11, 13, 99),
                new Selection("02", "300", 17, 19, 99)));

        assertFalse(result.failed());
        assertEquals(3, result.taxCodeTotals().size());
        assertEquals(new TaxCodeTotal("01", "100", 7, 10), result.taxCodeTotals().get(0));
        assertEquals(2, result.divisionTotals().size());
        assertEquals(2, result.divisionTotals().get(0).taxCodeCount());
        assertEquals("200", result.divisionTotals().get(0).lastTaxCode());
    }

    @Test
    void orderingFailureExcludesOffenderButKeepsEarlierAndEligibleTrailingProducts() {
        var result = kernel.rollup(List.of(
                new Selection("01", "100", 1, 2, 0),
                new Selection("02", "200", 3, 4, 0),
                new Selection("01", "300", 1000, 1000, 0)));

        assertTrue(result.failed());
        assertEquals(3, result.recordsRead());
        assertEquals(2, result.taxCodeTotals().size());
        assertEquals(2, result.divisionTotals().size());
        assertEquals(3L, result.taxCodeTotals().get(1).current288());
    }

    @Test
    void trailingRollupsUseOnlyThePositiveFirstTimeGate() {
        var suppressed = kernel.rollup(List.of(new Selection("01", "100", 9, 0, 0)));
        var emitted = kernel.rollup(List.of(new Selection("01", "100", 0, 1, 0)));

        assertTrue(suppressed.taxCodeTotals().isEmpty());
        assertTrue(suppressed.divisionTotals().isEmpty());
        assertEquals(1, emitted.taxCodeTotals().size());
        assertEquals(1, emitted.divisionTotals().size());
    }

    @Test
    void percentageMergeTruncatesSharesAndReportsOnlyLowerTaxTotals() {
        var result = kernel.percentages(
                List.of(
                        new TaxCodeTotal("01", "100", 2, 1),
                        new TaxCodeTotal("02", "200", 1, 2),
                        new TaxCodeTotal("04", "400", 1, 1)),
                List.of(
                        new DivisionTotal("02", "200", 1, 3, 3),
                        new DivisionTotal("03", "300", 1, 1, 1),
                        new DivisionTotal("04", "400", 1, 0, -1)));

        assertEquals(List.of(new TaxCodeTotal("01", "100", 2, 1)),
                result.unmatchedTaxCodeTotals());
        assertEquals(new BigDecimal("0.3333333"),
                result.percentages().get(0).current288Share());
        assertEquals(new BigDecimal("0.6666666"),
                result.percentages().get(0).firstTimeShare());
        assertEquals(new BigDecimal("0.0000000"),
                result.percentages().get(1).current288Share());
    }

    @Test
    void strictPercentageInputsRejectDuplicatesBeforeLaterOutput() {
        assertEquals("asrea744-001", assertThrows(RuleViolation.class, () -> kernel.percentages(
                List.of(new TaxCodeTotal("01", "100", 1, 1),
                        new TaxCodeTotal("01", "100", 2, 2)),
                List.of(new DivisionTotal("01", "100", 1, 1, 1))))
                .ruleId());
    }

    @Test
    void allocationUsesFullSingleValuesPercentageMultiplicationAndCountSplit() {
        FrozenValuation valuation = frozen("01", 101, 11, 51);
        Map<String, FrozenValuation> values = Map.of("01", valuation);
        var single = kernel.allocate(List.of(new PercentageAllocation(
                "01", "100", 1, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0, 0)), values);
        var multiple = kernel.allocate(List.of(new PercentageAllocation(
                "01", "100", 2, new BigDecimal("0.5000000"),
                new BigDecimal("0.3333333"), 0, 0, 0, 0)), values);
        var missing = kernel.allocate(List.of(new PercentageAllocation(
                "02", "200", 1, BigDecimal.ONE, BigDecimal.ONE, 0, 0, 0, 0)), values);

        assertEquals(101L, single.allocations().get(0).firstTime());
        assertEquals(51L, single.allocations().get(0).current288());
        assertEquals(33L, multiple.allocations().get(0).firstTime());
        assertEquals(25L, multiple.allocations().get(0).current288());
        assertEquals(5L, multiple.allocations().get(0).expired288());
        assertEquals(1, missing.unmatchedCount());
        assertTrue(missing.allocations().isEmpty());
    }

    @Test
    void sequentialFrozenHandoffRoundsHalfAwayAndExaminesAllFortyPositions() {
        List<Long> positions = new ArrayList<>();
        positions.add(1L);
        positions.add(0L);
        positions.add(-2L);
        positions.add(4L);
        while (positions.size() < 40) {
            positions.add(0L);
        }
        var allocation = new AllocatedFrozen("01", "100", 1,
                BigDecimal.ZERO, BigDecimal.ZERO, 10, 5, 10, 0);
        var result = kernel.expandFrozenAgencies(
                List.of(allocation),
                Map.of("100", new TaxCodeMaster(
                        "100", new BigDecimal("1.235"), 10, positions)),
                new BigDecimal("1.0200"));

        assertFalse(result.failed());
        assertEquals(2, result.details().size());
        assertEquals(26L, result.details().get(0).frozenEqualized());
        assertEquals(new BigDecimal("0.31"), result.details().get(0).frozenTax());
    }

    @Test
    void missingTaxMasterKeepsEarlierSequentialRecordsAndStopsLaterCodes() {
        List<Long> positions = fortyPositions(1L);
        var result = kernel.expandFrozenAgencies(
                List.of(
                        allocated("01", "100", 10),
                        allocated("02", "200", 20),
                        allocated("03", "300", 30)),
                Map.of("100", new TaxCodeMaster("100", BigDecimal.ZERO, 10, positions),
                        "300", new TaxCodeMaster("300", BigDecimal.ZERO, 12, positions)),
                BigDecimal.ONE);

        assertTrue(result.failed());
        assertEquals("200", result.missingTaxCode());
        assertEquals(1, result.details().size());
        assertEquals(2, result.recordsRead());
    }

    @Test
    void agencySummaryTotalsNineFieldsAndRequiresOrderedReferencedGroups() {
        FrozenAgencyDetail first = detail(1, 10, 10);
        FrozenAgencyDetail duplicate = detail(1, 10, 5);
        FrozenAgencyDetail second = detail(2, 12, 7);
        var result = kernel.summarizeAgencies(
                List.of(first, duplicate, second), Map.of(1L, "One", 2L, "Two"));

        assertFalse(result.failed());
        assertEquals(2, result.summaries().size());
        assertEquals(15L, result.summaries().get(0).newPropertyEqualized());
        assertEquals(4L, result.summaries().get(0).annexedEqualized());
        assertEquals(8L, result.summaries().get(0).disconnectedEqualized());
        assertEquals(10L, result.summaries().get(0).recoveredTifEqualized());
        assertEquals(14L, result.summaries().get(0).expiredIncentiveEqualized());
        assertEquals("One", result.summaries().get(0).description());

        assertTrue(kernel.summarizeAgencies(
                List.of(second, first), Map.of(1L, "One", 2L, "Two")).failed());
        assertTrue(kernel.summarizeAgencies(
                List.of(first), Map.of()).failed());
    }

    @Test
    void townReportGroupsEqualKeysAppliesBothOffsetsAndRejectsADecrease() {
        var report = kernel.reportTowns(
                List.of(detail(1, 10, 10), detail(1, 10, 5), detail(2, 71, 7)),
                Map.of(1, "BARRINGTON", 32, "JEFFERSON"));

        assertFalse(report.failed());
        assertEquals(2, report.townTotals().size());
        assertEquals("BARRINGTON", report.townTotals().get(0).townName());
        assertEquals("JEFFERSON", report.townTotals().get(1).townName());
        assertEquals(15L, report.townTotals().get(0).newPropertyEqualized());
        assertEquals(2, report.agencyTotals().size());
        assertEquals(15L, report.agencyTotals().get(0).newPropertyEqualized());
        assertEquals(4L, report.agencyTotals().get(0).annexedEqualized());
        assertTrue(kernel.reportTowns(
                List.of(detail(2, 12, 1), detail(1, 10, 1)), Map.of()).failed());
    }

    @Test
    void postingUpdatesOnlyFourFieldsSkipsMissingAndKeepsEarlierDurableWorkOnFailure() {
        AgencyEqualizedValuation one = agency("000000001", 999L);
        AgencyEqualizedValuation three = agency("000000003", 777L);
        Map<String, AgencyEqualizedValuation> existing = new HashMap<>();
        existing.put(one.getAgencyNumber(), one);
        existing.put(three.getAgencyNumber(), three);
        AgencySummary first = summary(1, 10, 2, 3, 4, 5);
        AgencySummary missing = summary(2, 20, 6, 7, 8, 9);
        AgencySummary third = summary(3, 30, 10, 11, 12, 13);

        var posted = kernel.postAgencySummaries(List.of(first, missing, third), existing);

        assertFalse(posted.failed());
        assertEquals(List.of(2L), posted.unmatchedAgencies());
        assertEquals(2, posted.updated().size());
        AgencyEqualizedValuation replacement = posted.updated().get("000000001");
        assertEquals(15L, replacement.getNewPropertyEqualizedValue());
        assertEquals(2L, replacement.getAnnexedPropertyEqualizedValue());
        assertEquals(3L, replacement.getDisconnectedPropertyEqualizedValue());
        assertEquals(4L, replacement.getDisconnectedTifDifference());
        assertEquals(999L, replacement.getCookCountyRealEstateValue());

        var partial = kernel.postAgencySummaries(
                List.of(first, third, summary(3, 100, 0, 0, 0, 0)), existing);
        assertTrue(partial.failed());
        assertEquals(2, partial.updated().size());
    }

    private DivisionActionInput actionInput(
            String division,
            String township,
            long qualifyingNew,
            boolean permitChanged,
            boolean exemptTransition) {
        return actionInput(
                division, township, qualifyingNew, permitChanged, exemptTransition, 0);
    }

    private DivisionActionInput actionInput(
            String division,
            String township,
            long qualifyingNew,
            boolean permitChanged,
            boolean exemptTransition,
            long proposedCurrent288) {
        return new DivisionActionInput(division, township,
                10, 20, 30, 1,
                10, 20, 30, 1,
                7, 8, proposedCurrent288, qualifyingNew, permitChanged, exemptTransition);
    }

    private FrozenValuation frozen(String division, long firstTime, long expired, long current) {
        FrozenValuation value = new FrozenValuation();
        value.setDivisionNumber(division);
        value.setProposedImprovementValue(firstTime);
        value.setProposedExpired288Value(expired);
        value.setProposedCurrent288Value(current);
        return value;
    }

    private AllocatedFrozen allocated(String division, String taxCode, long firstTime) {
        return new AllocatedFrozen(division, taxCode, 1, BigDecimal.ZERO, BigDecimal.ZERO,
                0, 0, firstTime, 0);
    }

    private List<Long> fortyPositions(long agency) {
        List<Long> result = new ArrayList<>();
        result.add(agency);
        while (result.size() < 40) {
            result.add(0L);
        }
        return result;
    }

    private FrozenAgencyDetail detail(long agency, int town, long value) {
        return new FrozenAgencyDetail("100", agency, town, BigDecimal.ZERO,
                0, 0, value, 0, value, value, BigDecimal.ZERO,
                1, 2, 3, 4, 5, 6, 7);
    }

    private AgencyEqualizedValuation agency(String number, long unrelatedValue) {
        AgencyEqualizedValuation value = new AgencyEqualizedValuation();
        value.setAgencyNumber(number);
        value.setCookCountyRealEstateValue(unrelatedValue);
        return value;
    }

    private AgencySummary summary(long agency, long newValue, long annexed,
            long disconnected, long tifDifference, long expired) {
        return new AgencySummary(agency, "Agency", 0, newValue, 0, annexed,
                0, disconnected, tifDifference, 0, expired);
    }
}
