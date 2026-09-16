package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TaxRateInputKernelTest {

    private final TaxRateInputKernel kernel = new TaxRateInputKernel();

    @Test
    void divisionStampingMatchesDivisionAndDefaultsUnmatchedPropertyWithoutChangingValues() {
        // Synthetic rule example, not captured parity.
        Input input =
                new Input(
                        List.of(
                                eq(10, 1, 100, "10001", BigDecimal.valueOf(90)),
                                eq(10, 1, 101, "10002", BigDecimal.valueOf(91))),
                        List.of(new Division(1, 100, 700)),
                        Map.of(),
                        List.of());

        Result result = kernel.process(input, insertingStore());

        assertThat(result.returnCode()).isEqualTo(0);
        assertThat(result.dividedValues().get(0).divisionNumber()).isEqualTo(700);
        assertThat(result.dividedValues().get(0).assessedValue()).isEqualTo(new BigDecimal("100"));
        assertThat(result.dividedValues().get(0).equalizedValue()).isEqualTo(new BigDecimal("90"));
        assertThat(result.dividedValues().get(1).divisionNumber()).isEqualTo(101);
        assertThat(result.divisionStamping().outputRecordsWritten()).isEqualTo(2);
        assertThat(result.divisionStamping().recordsStamped()).isEqualTo(2);
        assertThat(result.divisionStamping().divisionRecordsUnmatched()).isEqualTo(0);
    }

    @Test
    void divisionStampingAcceptsEqualizedDuplicateButRejectsDuplicateDivisionKey() {
        // Synthetic boundary example, not captured parity.
        EqualizedValue duplicate = eq(10, 1, 100, "10001", BigDecimal.valueOf(90));
        Result equalizedDuplicate =
                kernel.process(
                        new Input(List.of(duplicate, duplicate), List.of(), Map.of(), List.of()),
                        insertingStore());
        Result divisionDuplicate =
                kernel.process(
                        new Input(
                                List.of(duplicate),
                                List.of(new Division(1, 100, 7), new Division(1, 100, 8)),
                                Map.of(),
                                List.of()),
                        insertingStore());

        assertThat(equalizedDuplicate.dividedValues().size()).isEqualTo(2);
        assertThat(
                        equalizedDuplicate
                                .messages()
                                .get(equalizedDuplicate.messages().size() - 1)
                                .ruleId())
                .isEqualTo("clrtm752-001");
        assertThat(divisionDuplicate.returnCode()).isEqualTo(TaxRateInputKernel.ERROR_RETURN_CODE);
        assertThat(divisionDuplicate.messages().get(0).ruleId()).isEqualTo("clrtm751-001");
    }

    @Test
    void divisionStampingRetainsEarlierOutputForDescendingInputAndRejectsWideDefault() {
        // Synthetic boundary examples, not captured parity.
        Result descending =
                kernel.process(
                        new Input(
                                List.of(
                                        eq(10, 2, 200, "10001", BigDecimal.valueOf(10)),
                                        eq(10, 1, 100, "10001", BigDecimal.valueOf(20))),
                                List.of(),
                                Map.of(),
                                List.of()),
                        insertingStore());
        Result tooWide =
                kernel.process(
                        new Input(
                                List.of(
                                        eq(
                                                10,
                                                1,
                                                100_000_000_000_000L,
                                                "10001",
                                                BigDecimal.valueOf(10))),
                                List.of(),
                                Map.of(),
                                List.of()),
                        insertingStore());

        assertThat(descending.dividedValues().size()).isEqualTo(1);
        assertThat(descending.returnCode()).isEqualTo(TaxRateInputKernel.ERROR_RETURN_CODE);
        assertThat(tooWide.messages().get(0).code()).isEqualTo("DIVISION_NUMBER_TOO_WIDE");
    }

    @Test
    void agencyAttachmentCarriesRateFortyAgenciesAndValuesWithoutCalculatingRate() {
        // Synthetic matched example, not captured parity.
        List<String> agencies = agencyNames("A", 45);
        TaxCode taxCode = new TaxCode("10001", new BigDecimal("12.345"), agencies);
        Result result =
                kernel.process(
                        new Input(
                                List.of(eq(10, 1, 100, "10001", BigDecimal.valueOf(900))),
                                List.of(),
                                Map.of("10001", taxCode),
                                List.of()),
                        insertingStore());

        AgencyAssessment output = result.agencyAssessments().get(0);
        assertThat(output.rate()).isEqualTo(new BigDecimal("12.345"));
        assertThat(output.assessedValue()).isEqualTo(new BigDecimal("100"));
        assertThat(output.equalizedValue()).isEqualTo(new BigDecimal("900"));
        assertThat(output.agencies().size()).isEqualTo(40);
        assertThat(output.agencies().get(39)).isEqualTo("A040");
    }

    @Test
    void agencyAttachmentReportsEachMissingTaxCodeAndBalancesNormalCompletion() {
        // Synthetic missing-master example, not captured parity.
        Result result =
                kernel.process(
                        new Input(
                                List.of(
                                        eq(10, 1, 100, "10001", BigDecimal.valueOf(90)),
                                        eq(10, 1, 101, "10002", BigDecimal.valueOf(91))),
                                List.of(),
                                Map.of(),
                                List.of()),
                        insertingStore());

        assertThat(result.returnCode()).isEqualTo(0);
        assertThat(result.agencyAttachment().assessmentRecordsRead()).isEqualTo(2);
        assertThat(result.agencyAttachment().assessmentRecordsWritten()).isEqualTo(0);
        assertThat(result.agencyAttachment().assessmentRecordsUnmatched()).isEqualTo(2);
        assertThat(result.agencyAttachment().normalCompletionBalanced()).isTrue();
        assertThat(result.messages().get(0).text().contains("property 100")).isTrue();
    }

    @Test
    void agencyComparisonTotalsDeduplicatesAndClassifiesMatchedDivision() {
        // Synthetic matched-year example, not captured parity.
        AgencyAssessment prior =
                agency(
                        700,
                        99,
                        "10000",
                        BigDecimal.valueOf(40),
                        List.of("000000001", "000000002", "000000002"));
        TaxCode currentTaxCode =
                new TaxCode("10001", BigDecimal.ONE, List.of("000000002", "000000003"));
        Result result =
                kernel.process(
                        new Input(
                                List.of(eq(10, 1, 100, "10001", BigDecimal.valueOf(60))),
                                List.of(new Division(1, 100, 700)),
                                Map.of("10001", currentTaxCode),
                                List.of(prior)),
                        insertingStore());

        Comparison comparison = result.comparisons().get(0);
        assertThat(comparison.priorTotalEqualizedValue()).isEqualTo(new BigDecimal("40"));
        assertThat(comparison.currentTotalEqualizedValue()).isEqualTo(new BigDecimal("60"));
        assertThat(comparison.percentChange()).isEqualTo(new BigDecimal("100.0"));
        assertThat(comparison.segments())
                .isEqualTo(List.of(new Segment("000000001", 'D'), new Segment("000000003", 'A')));
        assertThat(result.agencyComparison().disconnectSegments()).isEqualTo(1);
        assertThat(result.agencyComparison().annexSegments()).isEqualTo(1);
    }

    @Test
    void agencyComparisonWritesZeroDifferenceAndReportsUnmatchedDivisions() {
        // Synthetic grouping example, not captured parity.
        TaxCode currentTaxCode = new TaxCode("10001", BigDecimal.ONE, List.of("000000001"));
        Result matched =
                kernel.process(
                        new Input(
                                List.of(eq(10, 1, 100, "10001", BigDecimal.valueOf(60))),
                                List.of(new Division(1, 100, 700)),
                                Map.of("10001", currentTaxCode),
                                List.of(
                                        agency(
                                                700,
                                                99,
                                                "10000",
                                                BigDecimal.valueOf(40),
                                                List.of("000000001")))),
                        insertingStore());
        Result unmatched =
                kernel.process(
                        new Input(
                                List.of(eq(10, 1, 100, "10001", BigDecimal.valueOf(60))),
                                List.of(),
                                Map.of("10001", currentTaxCode),
                                List.of(
                                        agency(
                                                99,
                                                99,
                                                "10000",
                                                BigDecimal.valueOf(40),
                                                List.of("000000001")))),
                        insertingStore());

        assertThat(matched.comparisons().size()).isEqualTo(1);
        assertThat(matched.comparisons().get(0).segments().isEmpty()).isTrue();
        assertThat(unmatched.agencyComparison().priorOnlyDivisions()).isEqualTo(1);
        assertThat(unmatched.agencyComparison().currentOnlyDivisions()).isEqualTo(1);
        assertThat(unmatched.comparisons().isEmpty()).isTrue();
    }

    @Test
    void agencyComparisonRetainsMatchedOutputAfterLateDescendingGroupRead() {
        // Synthetic partial-output ordering example, not captured parity.
        TaxCode currentTaxCode = new TaxCode("10001", BigDecimal.ONE, List.of("000000001"));
        List<AgencyAssessment> prior =
                List.of(
                        agency(700, 90, "10000", BigDecimal.valueOf(20), List.of("000000001")),
                        agency(700, 91, "10000", BigDecimal.valueOf(30), List.of("000000001")),
                        agency(600, 92, "10000", BigDecimal.valueOf(40), List.of("000000001")));
        Result result =
                kernel.process(
                        new Input(
                                List.of(eq(10, 1, 100, "10001", BigDecimal.valueOf(60))),
                                List.of(new Division(1, 100, 700)),
                                Map.of("10001", currentTaxCode),
                                prior),
                        insertingStore());

        assertThat(result.returnCode()).isEqualTo(TaxRateInputKernel.ERROR_RETURN_CODE);
        assertThat(result.comparisons().size()).isEqualTo(1);
        assertThat(result.comparisons().get(0).priorTotalEqualizedValue())
                .isEqualTo(new BigDecimal("50"));
        assertThat(result.agencyComparison().priorRecordsRead()).isEqualTo(3);
    }

    @Test
    void agencyComparisonCapsUniqueTablesAndReportsCombinedSegmentOverflow() {
        // Synthetic capacity boundary example, not captured parity.
        List<String> priorAgencies = agencyNames("P", 40);
        List<String> currentAgencies = agencyNames("C", 40);
        Result result =
                kernel.process(
                        new Input(
                                List.of(eq(10, 1, 100, "10001", BigDecimal.valueOf(60))),
                                List.of(new Division(1, 100, 700)),
                                Map.of(
                                        "10001",
                                        new TaxCode("10001", BigDecimal.ONE, currentAgencies)),
                                List.of(
                                        agency(
                                                700,
                                                99,
                                                "10000",
                                                BigDecimal.valueOf(40),
                                                priorAgencies))),
                        insertingStore());

        assertThat(result.returnCode()).isEqualTo(TaxRateInputKernel.ERROR_RETURN_CODE);
        assertThat(result.comparisons().isEmpty()).isTrue();
        assertThat(result.messages().get(0).code()).isEqualTo("ANNEX_DISCONNECT_CAPACITY_EXCEEDED");
    }

    @Test
    void frozenAgencyPostingSearchesFortyClassifiedEntriesAndIgnoresUnknownTypes() {
        // Synthetic declared-segment boundary example, not captured parity.
        List<Segment> segments = new ArrayList<>();
        for (int index = 1; index <= 49; index++) {
            segments.add(new Segment(String.format("A%03d", index), 'A'));
        }
        segments.add(new Segment("UNKNOWN", 'X'));
        Comparison comparison =
                new Comparison(
                        700,
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(30),
                        BigDecimal.valueOf(100),
                        segments);
        List<AgencyAssessment> current =
                List.of(
                        agency(700, 1, "10001", BigDecimal.valueOf(10), List.of("A040")),
                        agency(700, 2, "10001", BigDecimal.valueOf(20), List.of("A041")));

        Result result =
                kernel.postPrepared(List.of(), current, List.of(comparison), insertingStore());

        assertThat(result.postings().size()).isEqualTo(1);
        assertThat(result.postings().get(0).agencyNumber()).isEqualTo("A040");
    }

    @Test
    void frozenAgencyPostingRoundsDuplicateParcelSlotsAndCountsEachOperation() {
        // Synthetic posting and rounding example, not captured parity.
        Comparison comparison =
                new Comparison(
                        700,
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(10),
                        new BigDecimal("33.3"),
                        List.of(new Segment("000000001", 'A')));
        AgencyAssessment current =
                agency(700, 1, "10001", BigDecimal.valueOf(10), List.of("000000001", "000000001"));
        AccumulatingStore store = new AccumulatingStore();

        Result result =
                kernel.postPrepared(List.of(), List.of(current), List.of(comparison), store);

        assertThat(result.postings().size()).isEqualTo(2);
        assertThat(result.postings().get(0).annexedValue()).isEqualTo(new BigDecimal("3"));
        assertThat(store.values.get("10001|000000001")).isEqualTo(new BigDecimal("6"));
        assertThat(result.frozenAgencyPosting().insertOperations()).isEqualTo(1);
        assertThat(result.frozenAgencyPosting().rewriteOperations()).isEqualTo(1);
    }

    @Test
    void frozenAgencyPostingRetainsMutationsAndContinuesActiveRecordAfterFailure() {
        // Synthetic partial-error example, not captured parity.
        Comparison comparison =
                new Comparison(
                        700,
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(10),
                        new BigDecimal("100.0"),
                        List.of(new Segment("000000001", 'A')));
        AgencyAssessment current =
                agency(
                        700,
                        1,
                        "10001",
                        BigDecimal.valueOf(10),
                        List.of("000000001", "000000001", "000000001"));
        FailingSecondStore store = new FailingSecondStore();

        Result result =
                kernel.postPrepared(List.of(), List.of(current), List.of(comparison), store);

        assertThat(result.returnCode()).isEqualTo(TaxRateInputKernel.ERROR_RETURN_CODE);
        assertThat(result.postings().size()).isEqualTo(2);
        assertThat(result.frozenAgencyPosting().insertOperations()).isEqualTo(2);
        assertThat(store.attempts).isEqualTo(3);
        assertThat(store.successfulValue).isEqualTo(new BigDecimal("20"));
    }

    @Test
    void frozenAgencyPostingRetainsActiveDivisionWhenDescendingInputFails() {
        // Synthetic ordering-error example, not captured parity.
        Comparison comparison =
                new Comparison(
                        700,
                        BigDecimal.valueOf(20),
                        BigDecimal.valueOf(0),
                        new BigDecimal("100.0"),
                        List.of(new Segment("000000001", 'D')));
        List<AgencyAssessment> prior =
                List.of(
                        agency(700, 1, "10001", BigDecimal.valueOf(20), List.of("000000001")),
                        agency(600, 2, "10001", BigDecimal.valueOf(30), List.of("000000001")));

        Result result =
                kernel.postPrepared(prior, List.of(), List.of(comparison), insertingStore());

        assertThat(result.returnCode()).isEqualTo(TaxRateInputKernel.ERROR_RETURN_CODE);
        assertThat(result.postings().size()).isEqualTo(1);
        assertThat(result.frozenAgencyPosting().priorRecordsRead()).isEqualTo(2);
    }

    @Test
    void frozenAgencyPostingKeepsWholeUnitValuesBeyondPrimitiveRangeExact() {
        BigDecimal sourceValue = new BigDecimal("9223372036854775808");
        Comparison comparison =
                new Comparison(
                        700,
                        BigDecimal.ZERO,
                        sourceValue,
                        new BigDecimal("100.0"),
                        List.of(new Segment("000000001", 'A')));
        AgencyAssessment current = agency(700, 1, "10001", sourceValue, List.of("000000001"));
        AccumulatingStore store = new AccumulatingStore();

        Result result =
                kernel.postPrepared(List.of(), List.of(current), List.of(comparison), store);

        assertThat(result.postings().get(0).annexedValue()).isEqualTo(sourceValue);
        assertThat(store.values.get("10001|000000001")).isEqualTo(sourceValue);
    }

    private static EqualizedValue eq(
            int town, int volume, long property, String taxCode, BigDecimal equalizedValue) {
        return new EqualizedValue(
                town, volume, property, "0", taxCode, new BigDecimal("100"), equalizedValue);
    }

    private static AgencyAssessment agency(
            long division,
            long property,
            String taxCode,
            BigDecimal equalizedValue,
            List<String> agencies) {
        return new AgencyAssessment(
                division,
                10,
                1,
                property,
                "0",
                taxCode,
                new BigDecimal("100"),
                equalizedValue,
                BigDecimal.ONE,
                agencies);
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
        private final Map<String, BigDecimal> values = new HashMap<>();

        @Override
        public Operation post(String taxCode, String agency, BigDecimal value, boolean annex) {
            String key = taxCode + "|" + agency;
            boolean exists = values.containsKey(key);
            values.merge(key, value, BigDecimal::add);
            return exists ? Operation.REWRITE : Operation.INSERT;
        }
    }

    private static final class FailingSecondStore implements TaxRateInputKernel.FrozenAgencyStore {
        private int attempts;
        private BigDecimal successfulValue = BigDecimal.ZERO;

        @Override
        public Operation post(String taxCode, String agency, BigDecimal value, boolean annex) {
            attempts++;
            if (attempts == 2) {
                throw new IllegalStateException("synthetic persistence failure");
            }
            successfulValue = successfulValue.add(value);
            return Operation.INSERT;
        }
    }
}
