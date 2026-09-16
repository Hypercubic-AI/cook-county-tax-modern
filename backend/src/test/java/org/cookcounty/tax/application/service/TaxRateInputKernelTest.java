package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.application.service.TaxRateInputKernel.AgencyAssessment;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Comparison;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Division;
import org.cookcounty.tax.application.service.TaxRateInputKernel.EqualizedValue;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Input;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Operation;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Segment;
import org.cookcounty.tax.application.service.TaxRateInputKernel.TaxCode;
import org.junit.jupiter.api.Test;

class TaxRateInputKernelTest {

    private final TaxRateInputKernel kernel = new TaxRateInputKernel();

    @Test
    void clrtm751StampsMatchedDivisionAndDefaultsUnmatchedPropertyWithoutChangingValues() {
        // Synthetic rule example, not captured parity.
        Input input = new Input(
                List.of(eq(10, 1, 100, "10001", 90), eq(10, 1, 101, "10002", 91)),
                List.of(new Division(1, 100, 700)),
                Map.of(),
                List.of());

        Result result = kernel.process(input, insertingStore());

        assertEquals(0, result.returnCode());
        assertEquals(700, result.dividedValues().get(0).divisionNumber());
        assertEquals(100, result.dividedValues().get(0).assessedValue());
        assertEquals(90, result.dividedValues().get(0).equalizedValue());
        assertEquals(101, result.dividedValues().get(1).divisionNumber());
        assertEquals(2, result.divisionStamping().outputRecordsWritten());
        assertEquals(2, result.divisionStamping().recordsStamped());
        assertEquals(0, result.divisionStamping().divisionRecordsUnmatched());
    }

    @Test
    void clrtm751AcceptsEqualizedDuplicateButRejectsDuplicateDivisionKey() {
        // Synthetic boundary example, not captured parity.
        EqualizedValue duplicate = eq(10, 1, 100, "10001", 90);
        Result equalizedDuplicate = kernel.process(
                new Input(List.of(duplicate, duplicate), List.of(), Map.of(), List.of()),
                insertingStore());
        Result divisionDuplicate = kernel.process(
                new Input(
                        List.of(duplicate),
                        List.of(new Division(1, 100, 7), new Division(1, 100, 8)),
                        Map.of(),
                        List.of()),
                insertingStore());

        assertEquals(2, equalizedDuplicate.dividedValues().size());
        assertEquals(
                "clrtm752-001",
                equalizedDuplicate.messages().get(equalizedDuplicate.messages().size() - 1).ruleId());
        assertEquals(TaxRateInputKernel.ERROR_RETURN_CODE, divisionDuplicate.returnCode());
        assertEquals("clrtm751-001", divisionDuplicate.messages().get(0).ruleId());
    }

    @Test
    void clrtm751RetainsEarlierOutputWhenEqualizedInputDescendsAndRejectsWideDefault() {
        // Synthetic boundary examples, not captured parity.
        Result descending = kernel.process(
                new Input(
                        List.of(eq(10, 2, 200, "10001", 10), eq(10, 1, 100, "10001", 20)),
                        List.of(), Map.of(), List.of()),
                insertingStore());
        Result tooWide = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100_000_000_000_000L, "10001", 10)),
                        List.of(), Map.of(), List.of()),
                insertingStore());

        assertEquals(1, descending.dividedValues().size());
        assertEquals(TaxRateInputKernel.ERROR_RETURN_CODE, descending.returnCode());
        assertEquals("DIVISION_NUMBER_TOO_WIDE", tooWide.messages().get(0).code());
    }

    @Test
    void clrtm752CarriesRateFortyAgenciesAndParcelValuesWithoutCalculatingRate() {
        // Synthetic matched example, not captured parity.
        List<String> agencies = agencyNames("A", 45);
        TaxCode taxCode = new TaxCode("10001", new BigDecimal("12.345"), agencies);
        Result result = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 900)),
                        List.of(),
                        Map.of("10001", taxCode),
                        List.of()),
                insertingStore());

        AgencyAssessment output = result.agencyAssessments().get(0);
        assertEquals(new BigDecimal("12.345"), output.rate());
        assertEquals(100, output.assessedValue());
        assertEquals(900, output.equalizedValue());
        assertEquals(40, output.agencies().size());
        assertEquals("A040", output.agencies().get(39));
    }

    @Test
    void clrtm752ReportsEveryMissingTaxCodeContinuesAndBalancesNormalCompletion() {
        // Synthetic missing-master example, not captured parity.
        Result result = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 90), eq(10, 1, 101, "10002", 91)),
                        List.of(), Map.of(), List.of()),
                insertingStore());

        assertEquals(0, result.returnCode());
        assertEquals(2, result.agencyAttachment().assessmentRecordsRead());
        assertEquals(0, result.agencyAttachment().assessmentRecordsWritten());
        assertEquals(2, result.agencyAttachment().assessmentRecordsUnmatched());
        assertTrue(result.agencyAttachment().normalCompletionBalanced());
        assertTrue(result.messages().get(0).text().contains("property 100"));
    }

    @Test
    void clrtm753TotalsDeduplicatesAndClassifiesMatchedDivision() {
        // Synthetic matched-year example, not captured parity.
        AgencyAssessment prior = agency(700, 99, "10000", 40, List.of("000000001", "000000002", "000000002"));
        TaxCode currentTaxCode = new TaxCode(
                "10001", BigDecimal.ONE, List.of("000000002", "000000003"));
        Result result = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 60)),
                        List.of(new Division(1, 100, 700)),
                        Map.of("10001", currentTaxCode),
                        List.of(prior)),
                insertingStore());

        Comparison comparison = result.comparisons().get(0);
        assertEquals(40, comparison.priorTotalEqualizedValue());
        assertEquals(60, comparison.currentTotalEqualizedValue());
        assertEquals(new BigDecimal("100.0"), comparison.percentChange());
        assertEquals(List.of(new Segment("000000001", 'D'), new Segment("000000003", 'A')),
                comparison.segments());
        assertEquals(1, result.agencyComparison().disconnectSegments());
        assertEquals(1, result.agencyComparison().annexSegments());
    }

    @Test
    void clrtm753WritesZeroDifferenceAndReportsUnmatchedDivisions() {
        // Synthetic grouping example, not captured parity.
        TaxCode currentTaxCode = new TaxCode(
                "10001", BigDecimal.ONE, List.of("000000001"));
        Result matched = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 60)),
                        List.of(new Division(1, 100, 700)),
                        Map.of("10001", currentTaxCode),
                        List.of(agency(700, 99, "10000", 40, List.of("000000001")))),
                insertingStore());
        Result unmatched = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 60)),
                        List.of(),
                        Map.of("10001", currentTaxCode),
                        List.of(agency(99, 99, "10000", 40, List.of("000000001")))),
                insertingStore());

        assertEquals(1, matched.comparisons().size());
        assertTrue(matched.comparisons().get(0).segments().isEmpty());
        assertEquals(1, unmatched.agencyComparison().priorOnlyDivisions());
        assertEquals(1, unmatched.agencyComparison().currentOnlyDivisions());
        assertTrue(unmatched.comparisons().isEmpty());
    }

    @Test
    void clrtm753RetainsMatchedComparisonAfterLateDescendingGroupRead() {
        // Synthetic partial-output ordering example, not captured parity.
        TaxCode currentTaxCode = new TaxCode(
                "10001", BigDecimal.ONE, List.of("000000001"));
        List<AgencyAssessment> prior = List.of(
                agency(700, 90, "10000", 20, List.of("000000001")),
                agency(700, 91, "10000", 30, List.of("000000001")),
                agency(600, 92, "10000", 40, List.of("000000001")));
        Result result = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 60)),
                        List.of(new Division(1, 100, 700)),
                        Map.of("10001", currentTaxCode),
                        prior),
                insertingStore());

        assertEquals(TaxRateInputKernel.ERROR_RETURN_CODE, result.returnCode());
        assertEquals(1, result.comparisons().size());
        assertEquals(50, result.comparisons().get(0).priorTotalEqualizedValue());
        assertEquals(3, result.agencyComparison().priorRecordsRead());
    }

    @Test
    void clrtm753CapsEachUniqueTableAtFortyAndReportsCombinedSegmentOverflow() {
        // Synthetic capacity boundary example, not captured parity.
        List<String> priorAgencies = agencyNames("P", 40);
        List<String> currentAgencies = agencyNames("C", 40);
        Result result = kernel.process(
                new Input(
                        List.of(eq(10, 1, 100, "10001", 60)),
                        List.of(new Division(1, 100, 700)),
                        Map.of("10001", new TaxCode("10001", BigDecimal.ONE, currentAgencies)),
                        List.of(agency(700, 99, "10000", 40, priorAgencies))),
                insertingStore());

        assertEquals(TaxRateInputKernel.ERROR_RETURN_CODE, result.returnCode());
        assertTrue(result.comparisons().isEmpty());
        assertEquals("ANNEX_DISCONNECT_CAPACITY_EXCEEDED", result.messages().get(0).code());
    }

    @Test
    void clrtm755SearchesOnlyFirstFortyClassifiedEntriesAndIgnoresUnknownTypes() {
        // Synthetic declared-segment boundary example, not captured parity.
        List<Segment> segments = new ArrayList<>();
        for (int index = 1; index <= 49; index++) {
            segments.add(new Segment(String.format("A%03d", index), 'A'));
        }
        segments.add(new Segment("UNKNOWN", 'X'));
        Comparison comparison = new Comparison(700, 0, 30, BigDecimal.valueOf(100), segments);
        List<AgencyAssessment> current = List.of(
                agency(700, 1, "10001", 10, List.of("A040")),
                agency(700, 2, "10001", 20, List.of("A041")));

        Result result = kernel.postPrepared(List.of(), current, List.of(comparison), insertingStore());

        assertEquals(1, result.postings().size());
        assertEquals("A040", result.postings().get(0).agencyNumber());
    }

    @Test
    void clrtm755PostsDuplicateParcelSlotsRoundsAndCountsEveryOperation() {
        // Synthetic posting and rounding example, not captured parity.
        Comparison comparison = new Comparison(
                700, 0, 10, new BigDecimal("33.3"), List.of(new Segment("000000001", 'A')));
        AgencyAssessment current = agency(
                700, 1, "10001", 10, List.of("000000001", "000000001"));
        AccumulatingStore store = new AccumulatingStore();

        Result result = kernel.postPrepared(List.of(), List.of(current), List.of(comparison), store);

        assertEquals(2, result.postings().size());
        assertEquals(3, result.postings().get(0).annexedValue());
        assertEquals(6, store.values.get("10001|000000001"));
        assertEquals(1, result.frozenAgencyPosting().insertOperations());
        assertEquals(1, result.frozenAgencyPosting().rewriteOperations());
    }

    @Test
    void clrtm755RetainsSuccessfulMutationsAndContinuesActiveRecordAfterFailure() {
        // Synthetic partial-error example, not captured parity.
        Comparison comparison = new Comparison(
                700, 0, 10, new BigDecimal("100.0"), List.of(new Segment("000000001", 'A')));
        AgencyAssessment current = agency(
                700, 1, "10001", 10,
                List.of("000000001", "000000001", "000000001"));
        FailingSecondStore store = new FailingSecondStore();

        Result result = kernel.postPrepared(List.of(), List.of(current), List.of(comparison), store);

        assertEquals(TaxRateInputKernel.ERROR_RETURN_CODE, result.returnCode());
        assertEquals(2, result.postings().size());
        assertEquals(2, result.frozenAgencyPosting().insertOperations());
        assertEquals(3, store.attempts);
    }

    @Test
    void clrtm755RetainsActiveDivisionPostingWhenDescendingInputSetsReturnCode16() {
        // Synthetic ordering-error example, not captured parity.
        Comparison comparison = new Comparison(
                700, 20, 0, new BigDecimal("100.0"), List.of(new Segment("000000001", 'D')));
        List<AgencyAssessment> prior = List.of(
                agency(700, 1, "10001", 20, List.of("000000001")),
                agency(600, 2, "10001", 30, List.of("000000001")));

        Result result = kernel.postPrepared(prior, List.of(), List.of(comparison), insertingStore());

        assertEquals(TaxRateInputKernel.ERROR_RETURN_CODE, result.returnCode());
        assertEquals(1, result.postings().size());
        assertEquals(2, result.frozenAgencyPosting().priorRecordsRead());
    }

    private static EqualizedValue eq(int town, int volume, long property, String taxCode, long eav) {
        return new EqualizedValue(town, volume, property, "0", taxCode, 100, eav);
    }

    private static AgencyAssessment agency(
            long division, long property, String taxCode, long eav, List<String> agencies) {
        return new AgencyAssessment(
                division, 10, 1, property, "0", taxCode, 100, eav, BigDecimal.ONE, agencies);
    }

    private static List<String> agencyNames(String prefix, int count) {
        List<String> agencies = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            agencies.add(String.format("%s%03d", prefix, index));
        }
        return agencies;
    }

    private static TaxRateInputKernel.FrozenAgencyStore insertingStore() {
        return (taxCode, agency, value, annex) -> Operation.INSERT;
    }

    private static final class AccumulatingStore implements TaxRateInputKernel.FrozenAgencyStore {
        private final Map<String, Long> values = new HashMap<>();

        @Override
        public Operation post(String taxCode, String agency, long value, boolean annex) {
            String key = taxCode + "|" + agency;
            boolean exists = values.containsKey(key);
            values.merge(key, value, Long::sum);
            return exists ? Operation.REWRITE : Operation.INSERT;
        }
    }

    private static final class FailingSecondStore implements TaxRateInputKernel.FrozenAgencyStore {
        private int attempts;

        @Override
        public Operation post(String taxCode, String agency, long value, boolean annex) {
            attempts++;
            if (attempts == 2) {
                throw new IllegalStateException("synthetic persistence failure");
            }
            return Operation.INSERT;
        }
    }
}
