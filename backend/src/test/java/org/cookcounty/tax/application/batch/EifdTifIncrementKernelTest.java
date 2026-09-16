package org.cookcounty.tax.application.batch;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AgencySummary;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AllocatedFrozen;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.DivisionActionInput;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.DivisionTotal;
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
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference.AgencySlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Synthetic inputs in this class are rule examples, not captured-parity claims. */
class EifdTifIncrementKernelTest {

    private final EifdTifIncrementKernel kernel = new EifdTifIncrementKernel();

    @Test
    void ownedDomainValuesExposeOnlyImmutableRecordState() {
        assertThat(FrozenValuation.class.isRecord()).isTrue();
        assertThat(AgencyEqualizedValuation.class.isRecord()).isTrue();
        assertThat(
                        List.of(FrozenValuation.class.getMethods()).stream()
                                .map(java.lang.reflect.Method::getName)
                                .anyMatch(name -> name.startsWith("set")))
                .isFalse();
    }

    @Test
    void controlsValidateAllStartupRulesAndCenturyPivot() {
        var controls = kernel.validateControls("260181202526", "26", "10000");

        assertThat(controls.fromAge()).isEqualTo(1);
        assertThat(controls.toAge()).isEqualTo(81);
        assertThat(controls.from288Year()).isEqualTo(2025);
        assertThat(controls.to288Year()).isEqualTo(2026);
        assertThat(controls.equalizationFactor()).isEqualTo(new BigDecimal("1.0000"));
        assertThat(kernel.expandYear("99")).isEqualTo(1999);
        assertThat(kernel.expandYear("00")).isEqualTo(2000);
        assertThat(kernel.expandYear("60")).isEqualTo(2060);
        assertThat(kernel.expandYear("61")).isEqualTo(1961);
    }

    @ParameterizedTest(name = "{0}: invalid controls select {4}")
    @CsvSource({
        "nondigit start, X60181202526, 26, 10000, asrea740-001",
        "descending age, 268101202526, 26, 10000, asrea740-002",
        "short year, 260181202526, 2, 10000, asrea742-005",
        "nondigit year, 260181202526, 2X, 10000, asrea742-006",
        "zero factor, 260181202526, 26, 00000, asrea748-001"
    })
    void controlsRejectBeforeProcessingWithTheAcceptedRule(
            String caseName,
            String reassessmentControl,
            String processingYear,
            String factor,
            String expectedRule) {
        RuleViolation exception =
                assertThrows(
                        RuleViolation.class,
                        () -> kernel.validateControls(reassessmentControl, processingYear, factor));
        assertThat(exception.ruleId()).isEqualTo(expectedRule);
    }

    @ParameterizedTest(name = "tax code {0} derives town {1}")
    @CsvSource({"10001, 10", "39099, 39", "70001, 70", "77001, 77"})
    void townNumberUsesTheFirstTwoTaxCodeDigits(String taxCode, String townNumber) {
        assertThat(kernel.deriveTownNumber(taxCode)).isEqualTo(townNumber);
    }

    @Test
    void priorImprovementTruncatesAtEveryPositiveFactorStage() {
        // Rule example: 101 * 100 / 80 = 126.25 -> 126; * 75% = 94.5 -> 94;
        // * 33% = 31.02 -> 31. A single final truncation would produce 31 as well,
        // while the second assertion distinguishes missing-factor pass-through.
        assertThat(
                        kernel.derivePriorImprovement(
                                bd(101),
                                new BigDecimal("80"),
                                new BigDecimal("75"),
                                new BigDecimal("33")))
                .isEqualTo(bd(31));
        assertThat(
                        kernel.derivePriorImprovement(
                                bd(101), BigDecimal.ZERO, new BigDecimal("50"), null))
                .isEqualTo(bd(50));
    }

    @Test
    void classificationRequiresEveryCurrent288GateAndExcludesQuestionnaireCompanionsByInput() {
        assertThat(kernel.qualifiesCurrent288(3, 288, 2026, 26, true)).isTrue();
        assertThat(kernel.qualifiesCurrent288(3, 288, 2026, 26, false)).isFalse();
        assertThat(kernel.qualifiesCurrent288(2, 288, 2026, 26, true)).isFalse();
        assertThat(kernel.qualifiesFirstTime(2, 1, 200)).isTrue();
        assertThat(kernel.qualifiesFirstTime(5, 1, 288)).isFalse();
        assertThat(kernel.qualifiesFirstTime(6, 1, 200)).isFalse();
    }

    @Test
    void reconciliationReportsEitherPriorOrCurrentDifferenceWithoutBlockingPersistence() {
        assertThat(
                        kernel.hasReconciliationDifference(
                                new ReconciliationValues(
                                        bd(10), bd(5), bd(15), bd(2), bd(3), bd(4), bd(1), bd(10))))
                .isFalse();
        assertThat(
                        kernel.hasReconciliationDifference(
                                new ReconciliationValues(
                                        bd(10), bd(5), bd(16), bd(2), bd(3), bd(4), bd(1), bd(10))))
                .isTrue();
    }

    @Test
    void reassessmentClassificationSeparatesAllPriorAndCurrentCategories() {
        var controls = kernel.validateControls("260181202526", "26", "10000");

        assertThat(
                        kernel.classifyImprovement(
                                        new ImprovementClassInput(
                                                true, bd(10), 1, 200, 2024, false),
                                        controls)
                                .currentFirstYear())
                .isEqualTo(bd(10));
        assertThat(
                        kernel.classifyImprovement(
                                        new ImprovementClassInput(
                                                true, bd(11), 2, 200, 2024, false),
                                        controls)
                                .currentNonFrozen())
                .isEqualTo(bd(11));
        assertThat(
                        kernel.classifyImprovement(
                                        new ImprovementClassInput(true, bd(12), 2, 288, 2026, true),
                                        controls)
                                .current288())
                .isEqualTo(bd(12));
        assertThat(
                        kernel.classifyImprovement(
                                        new ImprovementClassInput(
                                                false, bd(13), 1, 200, 2024, false),
                                        controls)
                                .priorFirstYear())
                .isEqualTo(bd(13));
        assertThat(
                        kernel.classifyImprovement(
                                        new ImprovementClassInput(
                                                false, bd(14), 2, 288, 2025, false),
                                        controls)
                                .prior288())
                .isEqualTo(bd(14));
    }

    @Test
    void actionBucketsFollowTheReachableCobolScope() {
        FrozenValuation noQualifyingNewValue =
                kernel.selectActionTotals(actionInput("12", "1200", bd(0), false, false));
        assertThat(noQualifyingNewValue.changeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));
        assertThat(noQualifyingNewValue.noChangeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));

        FrozenValuation qualifyingNewValue =
                kernel.selectActionTotals(actionInput("12", "1200", bd(5), false, false));
        assertThat(qualifyingNewValue.changeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(30));
        assertThat(qualifyingNewValue.noChangeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));

        assertThat(
                        kernel.selectActionTotals(
                                        actionInput("12", "1200", bd(0), false, false, bd(9)))
                                .changeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));
        assertThat(
                        kernel.selectActionTotals(actionInput("12", "1200", bd(0), true, false))
                                .changeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));
        assertThat(
                        kernel.selectActionTotals(actionInput("12", "0000", bd(0), false, false))
                                .changeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));

        FrozenValuation exemptTransition =
                kernel.selectActionTotals(actionInput("12", "1200", bd(5), true, true, bd(9)));
        assertThat(exemptTransition.proposedImprovementValue()).isEqualTo(BigDecimal.valueOf(30));
        assertThat(exemptTransition.proposedTotalValue()).isEqualTo(BigDecimal.valueOf(30));
        assertThat(exemptTransition.proposedCurrent288Value()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(exemptTransition.changeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));
        assertThat(exemptTransition.noChangeActionCurrentTotalValue())
                .isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    void frozenValuesAccumulateAndAllZeroMissingDivisionIsSuppressed() {
        FrozenValuation stored = frozen("0001", bd(10), bd(20), bd(30));
        FrozenValuation delta = frozen("0001", bd(5), bd(7), bd(9), bd(2L));

        FrozenValuation accumulated = kernel.accumulateFrozen(stored, delta);

        assertThat(accumulated.proposedImprovementValue()).isEqualTo(BigDecimal.valueOf(15));
        assertThat(accumulated.proposedExpired288Value()).isEqualTo(BigDecimal.valueOf(27));
        assertThat(accumulated.proposedCurrent288Value()).isEqualTo(BigDecimal.valueOf(39));
        assertThat(accumulated.changeActionCurrentTotalValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(kernel.shouldInsertFrozen(accumulated)).isTrue();
        assertThat(kernel.shouldInsertFrozen(frozen("0000", bd(0), bd(0), bd(0)))).isFalse();
    }

    @Test
    void rollupGroupsDuplicatesAndClosesInnerAndOuterGroups() {
        var result =
                kernel.rollup(
                        List.of(
                                new Selection("01", "100", bd(2), bd(3), bd(99)),
                                new Selection("01", "100", bd(5), bd(7), bd(99)),
                                new Selection("01", "200", bd(11), bd(13), bd(99)),
                                new Selection("02", "300", bd(17), bd(19), bd(99))));

        assertThat(result.failed()).isFalse();
        assertThat(result.taxCodeTotals().size()).isEqualTo(3);
        assertThat(result.taxCodeTotals().get(0))
                .isEqualTo(new TaxCodeTotal("01", "100", bd(7), bd(10)));
        assertThat(result.divisionTotals().size()).isEqualTo(2);
        assertThat(result.divisionTotals().get(0).taxCodeCount()).isEqualTo(2);
        assertThat(result.divisionTotals().get(0).lastTaxCode()).isEqualTo("200");
    }

    @Test
    void orderingFailureExcludesOffenderButKeepsEarlierAndEligibleTrailingProducts() {
        var result =
                kernel.rollup(
                        List.of(
                                new Selection("01", "100", bd(1), bd(2), bd(0)),
                                new Selection("02", "200", bd(3), bd(4), bd(0)),
                                new Selection("01", "300", bd(1000), bd(1000), bd(0))));

        assertThat(result.failed()).isTrue();
        assertThat(result.recordsRead()).isEqualTo(3);
        assertThat(result.taxCodeTotals().size()).isEqualTo(2);
        assertThat(result.divisionTotals().size()).isEqualTo(2);
        assertThat(result.taxCodeTotals().get(1).current288()).isEqualTo(bd(3));
    }

    @Test
    void trailingRollupsUseOnlyThePositiveFirstTimeGate() {
        var suppressed = kernel.rollup(List.of(new Selection("01", "100", bd(9), bd(0), bd(0))));
        var emitted = kernel.rollup(List.of(new Selection("01", "100", bd(0), bd(1), bd(0))));

        assertThat(suppressed.taxCodeTotals().isEmpty()).isTrue();
        assertThat(suppressed.divisionTotals().isEmpty()).isTrue();
        assertThat(emitted.taxCodeTotals().size()).isEqualTo(1);
        assertThat(emitted.divisionTotals().size()).isEqualTo(1);
    }

    @Test
    void percentageMergeTruncatesSharesAndReportsOnlyLowerTaxTotals() {
        var result =
                kernel.percentages(
                        List.of(
                                new TaxCodeTotal("01", "100", bd(2), bd(1)),
                                new TaxCodeTotal("02", "200", bd(1), bd(2)),
                                new TaxCodeTotal("04", "400", bd(1), bd(1))),
                        List.of(
                                new DivisionTotal("02", "200", 1, bd(3), bd(3)),
                                new DivisionTotal("03", "300", 1, bd(1), bd(1)),
                                new DivisionTotal("04", "400", 1, bd(0), bd(-1))));

        assertThat(result.unmatchedTaxCodeTotals())
                .isEqualTo(List.of(new TaxCodeTotal("01", "100", bd(2), bd(1))));
        assertThat(result.percentages().get(0).current288Share())
                .isEqualTo(new BigDecimal("0.3333333"));
        assertThat(result.percentages().get(0).firstTimeShare())
                .isEqualTo(new BigDecimal("0.6666666"));
        assertThat(result.percentages().get(1).current288Share())
                .isEqualTo(new BigDecimal("0.0000000"));
    }

    @Test
    void strictPercentageInputsRejectDuplicatesBeforeLaterOutput() {
        assertThat(
                        assertThrows(
                                        RuleViolation.class,
                                        () ->
                                                kernel.percentages(
                                                        List.of(
                                                                new TaxCodeTotal(
                                                                        "01", "100", bd(1), bd(1)),
                                                                new TaxCodeTotal(
                                                                        "01", "100", bd(2), bd(2))),
                                                        List.of(
                                                                new DivisionTotal(
                                                                        "01", "100", 1, bd(1),
                                                                        bd(1)))))
                                .ruleId())
                .isEqualTo("asrea744-001");
    }

    @Test
    void allocationUsesFullSingleValuesPercentageMultiplicationAndCountSplit() {
        FrozenValuation valuation = frozen("01", bd(101), bd(11), bd(51));
        Map<String, FrozenValuation> values = Map.of("01", valuation);
        var single =
                kernel.allocate(
                        List.of(
                                new PercentageAllocation(
                                        "01",
                                        "100",
                                        1,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        bd(0),
                                        bd(0),
                                        bd(0),
                                        bd(0))),
                        values);
        var multiple =
                kernel.allocate(
                        List.of(
                                new PercentageAllocation(
                                        "01",
                                        "100",
                                        2,
                                        new BigDecimal("0.5000000"),
                                        new BigDecimal("0.3333333"),
                                        bd(0),
                                        bd(0),
                                        bd(0),
                                        bd(0))),
                        values);
        var missing =
                kernel.allocate(
                        List.of(
                                new PercentageAllocation(
                                        "02",
                                        "200",
                                        1,
                                        BigDecimal.ONE,
                                        BigDecimal.ONE,
                                        bd(0),
                                        bd(0),
                                        bd(0),
                                        bd(0))),
                        values);

        assertThat(single.allocations().get(0).firstTime()).isEqualTo(bd(101));
        assertThat(single.allocations().get(0).current288()).isEqualTo(bd(51));
        assertThat(multiple.allocations().get(0).firstTime()).isEqualTo(bd(33));
        assertThat(multiple.allocations().get(0).current288()).isEqualTo(bd(25));
        assertThat(multiple.allocations().get(0).expired288()).isEqualTo(bd(5));
        assertThat(missing.unmatchedCount()).isEqualTo(1);
        assertThat(missing.allocations().isEmpty()).isTrue();
    }

    @Test
    void taxCodeReferenceRetainsSignedSparseSourcePositions() {
        TaxCodeMasterReference reference =
                new TaxCodeMasterReference(
                        "10001",
                        new BigDecimal("6.735"),
                        List.of(new AgencySlot(1, "000000001"), new AgencySlot(40, "-00000001")));

        assertThat(reference.agencySlots().stream().map(AgencySlot::position).toList())
                .isEqualTo(List.of(1, 40));
        assertThat(reference.agencySlots().get(1).agencyNumber()).isEqualTo("-00000001");
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
        var allocation =
                new AllocatedFrozen(
                        "01",
                        "100",
                        1,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        bd(10),
                        bd(5),
                        bd(10),
                        bd(0));
        var result =
                kernel.expandFrozenAgencies(
                        List.of(allocation),
                        Map.of(
                                "100",
                                new TaxCodeMaster("100", new BigDecimal("1.235"), 10, positions)),
                        new BigDecimal("1.0200"));

        assertThat(result.failed()).isFalse();
        assertThat(result.details().size()).isEqualTo(2);
        assertThat(result.details().get(0).frozenEqualized()).isEqualTo(bd(26));
        assertThat(result.details().get(0).frozenTax()).isEqualTo(new BigDecimal("0.31"));
    }

    @Test
    void missingTaxMasterKeepsEarlierSequentialRecordsAndStopsLaterCodes() {
        List<Long> positions = fortyPositions(1L);
        var result =
                kernel.expandFrozenAgencies(
                        List.of(
                                allocated("01", "100", bd(10)),
                                allocated("02", "200", bd(20)),
                                allocated("03", "300", bd(30))),
                        Map.of(
                                "100",
                                new TaxCodeMaster("100", BigDecimal.ZERO, 10, positions),
                                "300",
                                new TaxCodeMaster("300", BigDecimal.ZERO, 12, positions)),
                        BigDecimal.ONE);

        assertThat(result.failed()).isTrue();
        assertThat(result.missingTaxCode()).isEqualTo("200");
        assertThat(result.details().size()).isEqualTo(1);
        assertThat(result.recordsRead()).isEqualTo(2);
    }

    @Test
    void agencySummaryTotalsNineFieldsAndRequiresOrderedReferencedGroups() {
        FrozenAgencyDetail first = detail(1, 10, bd(10));
        FrozenAgencyDetail duplicate = detail(1, 10, bd(5));
        FrozenAgencyDetail second = detail(2, 12, bd(7));
        var result =
                kernel.summarizeAgencies(
                        List.of(first, duplicate, second), Map.of(1L, "One", 2L, "Two"));

        assertThat(result.failed()).isFalse();
        assertThat(result.summaries().size()).isEqualTo(2);
        assertThat(result.summaries().get(0).newPropertyEqualized()).isEqualTo(bd(15));
        assertThat(result.summaries().get(0).annexedEqualized()).isEqualTo(bd(4));
        assertThat(result.summaries().get(0).disconnectedEqualized()).isEqualTo(bd(8));
        assertThat(result.summaries().get(0).recoveredTifEqualized()).isEqualTo(bd(10));
        assertThat(result.summaries().get(0).expiredIncentiveEqualized()).isEqualTo(bd(14));
        assertThat(result.summaries().get(0).description()).isEqualTo("One");

        assertThat(
                        kernel.summarizeAgencies(
                                        List.of(second, first), Map.of(1L, "One", 2L, "Two"))
                                .failed())
                .isTrue();
        assertThat(kernel.summarizeAgencies(List.of(first), Map.of()).failed()).isTrue();
    }

    @Test
    void townReportUsesPublishedTownNumbersAndRejectsADecrease() {
        var report =
                kernel.reportTowns(
                        List.of(detail(1, 10, bd(10)), detail(1, 10, bd(5)), detail(20, 77, bd(7))),
                        Map.of(10, "BARRINGTON", 77, "WEST"));

        assertThat(report.failed()).isFalse();
        assertThat(report.townTotals().size()).isEqualTo(2);
        assertThat(report.townTotals().get(0).townName()).isEqualTo("BARRINGTON");
        assertThat(report.townTotals().get(1).townName()).isEqualTo("WEST");
        assertThat(report.townTotals().get(0).newPropertyEqualized()).isEqualTo(bd(15));
        assertThat(report.agencyTotals().size()).isEqualTo(2);
        assertThat(report.agencyTotals().get(0).newPropertyEqualized()).isEqualTo(bd(15));
        assertThat(report.agencyTotals().get(0).annexedEqualized()).isEqualTo(bd(4));
        assertThat(
                        kernel.reportTowns(
                                        List.of(detail(2, 12, bd(1)), detail(1, 10, bd(1))),
                                        Map.of())
                                .failed())
                .isTrue();
    }

    @Test
    void postingUpdatesOnlyFourFieldsSkipsMissingAndKeepsEarlierDurableWorkOnFailure() {
        AgencyEqualizedValuation one = agency("000000001", bd(999L));
        AgencyEqualizedValuation three = agency("000000003", bd(777L));
        Map<String, AgencyEqualizedValuation> existing = new HashMap<>();
        existing.put(one.agencyNumber(), one);
        existing.put(three.agencyNumber(), three);
        AgencySummary first = summary(1, bd(10), bd(2), bd(3), bd(4), bd(5));
        AgencySummary missing = summary(2, bd(20), bd(6), bd(7), bd(8), bd(9));
        AgencySummary third = summary(3, bd(30), bd(10), bd(11), bd(12), bd(13));

        var posted = kernel.postAgencySummaries(List.of(first, missing, third), existing);

        assertThat(posted.failed()).isFalse();
        assertThat(posted.unmatchedAgencies()).isEqualTo(List.of(2L));
        assertThat(posted.updated().size()).isEqualTo(2);
        AgencyEqualizedValuation replacement =
                requireNonNull(posted.updated().get("000000001"), "updated agency");
        assertThat(replacement.newPropertyEqualizedValue()).isEqualTo(BigDecimal.valueOf(15));
        assertThat(replacement.annexedPropertyEqualizedValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(replacement.disconnectedPropertyEqualizedValue())
                .isEqualTo(BigDecimal.valueOf(3));
        assertThat(replacement.disconnectedTifDifference()).isEqualTo(BigDecimal.valueOf(4));
        assertThat(replacement.cookCountyRealEstateValue()).isEqualTo(BigDecimal.valueOf(999));

        var partial =
                kernel.postAgencySummaries(
                        List.of(first, third, summary(3, bd(100), bd(0), bd(0), bd(0), bd(0))),
                        existing);
        assertThat(partial.failed()).isTrue();
        assertThat(partial.updated().size()).isEqualTo(2);
    }

    private DivisionActionInput actionInput(
            String division,
            String township,
            BigDecimal qualifyingNew,
            boolean permitChanged,
            boolean exemptTransition) {
        return actionInput(
                division, township, qualifyingNew, permitChanged, exemptTransition, bd(0));
    }

    private DivisionActionInput actionInput(
            String division,
            String township,
            BigDecimal qualifyingNew,
            boolean permitChanged,
            boolean exemptTransition,
            BigDecimal proposedCurrent288) {
        return new DivisionActionInput(
                String.format("%014d", Long.parseLong(division)),
                township,
                bd(10),
                bd(20),
                bd(30),
                1,
                bd(10),
                bd(20),
                bd(30),
                1,
                bd(7),
                bd(8),
                proposedCurrent288,
                qualifyingNew,
                permitChanged,
                exemptTransition);
    }

    private FrozenValuation frozen(
            String division, BigDecimal firstTime, BigDecimal expired, BigDecimal current) {
        return frozen(division, firstTime, expired, current, bd(0));
    }

    private FrozenValuation frozen(
            String division,
            BigDecimal firstTime,
            BigDecimal expired,
            BigDecimal current,
            BigDecimal actionCurrentTotal) {
        return new FrozenValuation(
                null,
                null,
                bd(0),
                bd(0),
                0L,
                actionCurrentTotal,
                bd(0),
                bd(0),
                0L,
                bd(0),
                bd(0),
                bd(0),
                0L,
                bd(0),
                String.format("%014d", Long.parseLong(division)),
                bd(0),
                bd(0),
                0L,
                bd(0),
                bd(0),
                bd(0),
                0L,
                bd(0),
                bd(0),
                bd(0),
                0L,
                bd(0),
                bd(0),
                current,
                expired,
                firstTime,
                bd(0));
    }

    private AllocatedFrozen allocated(String division, String taxCode, BigDecimal firstTime) {
        return new AllocatedFrozen(
                division,
                taxCode,
                1,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                bd(0),
                bd(0),
                firstTime,
                bd(0));
    }

    private List<Long> fortyPositions(long agency) {
        List<Long> result = new ArrayList<>();
        result.add(agency);
        while (result.size() < 40) {
            result.add(0L);
        }
        return result;
    }

    private FrozenAgencyDetail detail(long agency, int town, BigDecimal value) {
        return new FrozenAgencyDetail(
                "100",
                agency,
                town,
                BigDecimal.ZERO,
                bd(0),
                bd(0),
                value,
                bd(0),
                value,
                value,
                BigDecimal.ZERO,
                bd(1),
                bd(2),
                bd(3),
                bd(4),
                bd(5),
                bd(6),
                bd(7));
    }

    private AgencyEqualizedValuation agency(String number, BigDecimal unrelatedValue) {
        return new AgencyEqualizedValuation(
                null,
                null,
                String.format("%09d", Long.parseLong(number)),
                bd(0),
                new BigDecimal("0.00"),
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                bd(0),
                bd(0),
                unrelatedValue,
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                new BigDecimal("0.000000"),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                bd(0),
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                "000000000",
                2024,
                new BigDecimal("0.00"),
                2023,
                new BigDecimal("0.00"),
                2022,
                new BigDecimal("0.00"),
                false,
                2024,
                bd(0));
    }

    private AgencySummary summary(
            long agency,
            BigDecimal newValue,
            BigDecimal annexed,
            BigDecimal disconnected,
            BigDecimal tifDifference,
            BigDecimal expired) {
        return new AgencySummary(
                agency,
                "Agency",
                bd(0),
                newValue,
                bd(0),
                annexed,
                bd(0),
                disconnected,
                tifDifference,
                bd(0),
                expired);
    }

    private static BigDecimal bd(long value) {
        return BigDecimal.valueOf(value);
    }
}
