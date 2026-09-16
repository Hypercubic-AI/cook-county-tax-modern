package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AgencyReference;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference.AgencySlot;
import org.cookcounty.tax.domain.model.TownReference;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.EifdTifReferenceDataRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class EifdTifIncrementProcessorTest {

    @Test
    void shuffledDivisionKeysPreserveSourceEligibilityAndAgencyUpdates() {
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        FrozenValuationRepository frozen = mock(FrozenValuationRepository.class);
        AgencyEqualizedValuationRepository agencies =
                mock(AgencyEqualizedValuationRepository.class);
        AssessmentDetailValuator valuator = new AssessmentDetailValuator();
        EifdTifReferenceDataRepository references = references();

        when(parcels.findAllInInputOrder()).thenReturn(parcels());
        when(details.findAllInPersistenceOrder()).thenReturn(List.of());
        when(frozen.findAllInPersistenceOrder()).thenReturn(shuffledFrozenValues());
        when(agencies.findAllInPersistenceOrder()).thenReturn(agencyValues());
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agencies.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EifdTifIncrementProcessor processor =
                new EifdTifIncrementProcessor(
                        parcels,
                        details,
                        frozen,
                        agencies,
                        references,
                        valuator,
                        new org.cookcounty.tax.application.batch.EifdTifIncrementKernel(),
                        new EifdTifIncrementOutcomeProjector(),
                        org.springframework.transaction.support.TransactionOperations
                                .withoutTransaction());
        EifdTifIncrementProcessResult result = processor.process(request());

        assertThat(result.completed()).isTrue();
        assertThat(result.returnCode()).isEqualTo(0);
        assertThat(result.recordsUpdated()).isEqualTo(19);
        assertThat(
                        result.outputs().stream()
                                .skip(1)
                                .map(EifdTifIncrementProcessResult.Output::recordCount)
                                .toList())
                .isEqualTo(List.of(14, 75, 117));
        assertThat(result.outputs().get(0).recordCount()).isEqualTo(10);
        verify(frozen, times(10)).save(any());
        verify(agencies, times(9)).save(any());
        assertThat(result.frozenValuationReportFacts().size()).isEqualTo(10);
        ArgumentCaptor<FrozenValuation> frozenCaptor =
                ArgumentCaptor.forClass(FrozenValuation.class);
        verify(frozen, times(10)).save(frozenCaptor.capture());
        FrozenValuation firstFrozenUpdate = frozenCaptor.getAllValues().get(0);
        assertThat(firstFrozenUpdate.priorLandValue()).isEqualTo(BigDecimal.valueOf(30));
        assertThat(firstFrozenUpdate.priorImprovementValue()).isEqualTo(BigDecimal.valueOf(70));
        assertThat(firstFrozenUpdate.priorTotalValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(firstFrozenUpdate.currentLandValue()).isEqualTo(BigDecimal.valueOf(40));
        assertThat(firstFrozenUpdate.currentImprovementValue()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(firstFrozenUpdate.currentTotalValue()).isEqualTo(BigDecimal.valueOf(120));
        assertThat(result.frozenValuationReportFacts().get(0).proposedImprovementValue())
                .isEqualTo(BigDecimal.valueOf(100));
        assertThat(firstFrozenUpdate.noChangeActionPriorParcelCount()).isEqualTo(12L);
        assertThat(firstFrozenUpdate.changeActionPriorParcelCount()).isEqualTo(0L);
        FrozenValuation secondFrozenUpdate = frozenCaptor.getAllValues().get(1);
        assertThat(secondFrozenUpdate.changeActionPriorParcelCount()).isEqualTo(13L);
        assertThat(secondFrozenUpdate.noChangeActionPriorParcelCount()).isEqualTo(0L);

        ArgumentCaptor<AgencyEqualizedValuation> captor =
                ArgumentCaptor.forClass(AgencyEqualizedValuation.class);
        verify(agencies, times(9)).save(captor.capture());
        AgencyEqualizedValuation first = captor.getAllValues().get(0);
        assertThat(first.newPropertyEqualizedValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(first.cookCountyRealEstateValue()).isEqualTo(BigDecimal.valueOf(9001));
        assertThat(captor.getAllValues().stream().map(AgencyEqualizedValuation::agencyNumber))
                .containsExactly(
                        "000000001",
                        "000000002",
                        "000000003",
                        "000000004",
                        "000000005",
                        "000000006",
                        "000000007",
                        "000000011",
                        "000000012");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("divisionJoinCases")
    void divisionJoinDoesNotPairByPositionAndGroupsRepeatedMasterKeys(
            String caseName, String firstDivision, long expectedPriorLand) {
        AssessmentParcel first = parcel(1L, firstDivision, 10L);
        AssessmentParcel second = parcel(2L, "00000000000002", 20L);
        FrozenValuation stored = frozen("00000000000002", 1L, 0L, 0L);

        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        FrozenValuationRepository frozen = mock(FrozenValuationRepository.class);
        AgencyEqualizedValuationRepository agencies =
                mock(AgencyEqualizedValuationRepository.class);
        when(parcels.findAllInInputOrder()).thenReturn(List.of(first, second));
        when(details.findAllInPersistenceOrder()).thenReturn(List.of());
        when(frozen.findAllInPersistenceOrder()).thenReturn(List.of(stored));
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EifdTifIncrementProcessor processor =
                new EifdTifIncrementProcessor(
                        parcels,
                        details,
                        frozen,
                        agencies,
                        mock(EifdTifReferenceDataRepository.class),
                        mock(AssessmentDetailValuator.class),
                        new org.cookcounty.tax.application.batch.EifdTifIncrementKernel(),
                        new EifdTifIncrementOutcomeProjector(),
                        org.springframework.transaction.support.TransactionOperations
                                .withoutTransaction());
        processor.process(request());

        ArgumentCaptor<FrozenValuation> saved = ArgumentCaptor.forClass(FrozenValuation.class);
        verify(frozen).save(saved.capture());
        assertThat(saved.getValue().divisionNumber()).isEqualTo("00000000000002");
        assertThat(saved.getValue().priorLandValue())
                .isEqualTo(BigDecimal.valueOf(expectedPriorLand));
    }

    private static Stream<Arguments> divisionJoinCases() {
        return Stream.of(
                Arguments.of(
                        "missing frozen key is not positionally paired", "00000000000001", 20L),
                Arguments.of(
                        "repeated master division is accumulated once", "00000000000002", 30L));
    }

    private AssessmentParcel parcel(long number, String division, long priorLand) {
        return new AssessmentParcel(
                null,
                null,
                BigDecimal.valueOf(0),
                "",
                "",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0,
                BigDecimal.valueOf(0),
                0,
                String.format("%015d", number),
                "",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0,
                String.format("%05d", 10_001),
                "",
                String.format("%03d", 0),
                null,
                division,
                BigDecimal.valueOf(priorLand),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(priorLand),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
    }

    private List<AssessmentParcel> parcels() {
        int[] taxCodes = {
            10_001, 12_001, 13_001, 14_001, 20_001,
            22_001, 37_001, 71_001, 76_001, 77_001
        };
        long[] divisions = {1, 2, 3, 4, 5, 6, 7, 11, 12, 13};
        List<AssessmentParcel> values = new ArrayList<>();
        for (int index = 0; index < divisions.length; index++) {
            values.add(
                    new AssessmentParcel(
                            null,
                            null,
                            BigDecimal.valueOf(0),
                            "",
                            "",
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(index == 0 ? 1_000L : 0L),
                            BigDecimal.valueOf(index == 0 ? 1_000L : 0L),
                            BigDecimal.valueOf(index == 0 ? 2_000L : 0L),
                            0,
                            BigDecimal.valueOf(0),
                            0,
                            String.format("%015d", (long) index + 1),
                            "",
                            BigDecimal.valueOf(index == 0 ? 900L : 0L),
                            BigDecimal.valueOf(index == 0 ? 900L : 0L),
                            BigDecimal.valueOf(index == 0 ? 1_800L : 0L),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            0,
                            String.format("%05d", taxCodes[index]),
                            "",
                            String.format("%03d", 0),
                            null,
                            String.format("%014d", divisions[index]),
                            BigDecimal.valueOf(index == 0 ? 30L : 0L),
                            BigDecimal.valueOf(index == 0 ? 70L : 0L),
                            BigDecimal.valueOf(index == 0 ? 100L : 0L),
                            BigDecimal.valueOf(index == 0 ? 40L : 0L),
                            BigDecimal.valueOf(index == 0 ? 80L : 0L),
                            BigDecimal.valueOf(index == 0 ? 120L : 0L)));
        }
        return values;
    }

    private List<FrozenValuation> frozenValues() {
        long[] divisions = {1, 2, 3, 4, 5, 6, 7, 11, 12, 13};
        List<FrozenValuation> values = new ArrayList<>();
        for (long division : divisions) {
            values.add(
                    frozen(
                            Long.toString(division),
                            100L,
                            division == 1 ? 12L : 0L,
                            division == 2 ? 13L : 0L));
        }
        return values;
    }

    private FrozenValuation frozen(
            String division,
            long proposedImprovement,
            long noChangePriorCount,
            long changePriorCount) {
        return new FrozenValuation(
                null,
                null,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0L,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                changePriorCount,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0L,
                BigDecimal.valueOf(0),
                String.format("%014d", Long.parseLong(division)),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0L,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                noChangePriorCount,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0L,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(proposedImprovement),
                BigDecimal.valueOf(0));
    }

    private List<FrozenValuation> shuffledFrozenValues() {
        List<FrozenValuation> values = frozenValues();
        Collections.reverse(values);
        return values;
    }

    private List<AgencyEqualizedValuation> agencyValues() {
        long[] agencyNumbers = {1, 2, 3, 4, 5, 6, 7, 11, 12, 20};
        List<AgencyEqualizedValuation> values = new ArrayList<>();
        for (long agencyNumber : agencyNumbers) {
            values.add(
                    new AgencyEqualizedValuation(
                            null,
                            null,
                            String.format("%09d", agencyNumber),
                            BigDecimal.valueOf(0),
                            BigDecimal.ZERO,
                            "000000000",
                            "000000000",
                            "000000000",
                            "000000000",
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(9_000L + agencyNumber),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.ZERO,
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            "000000000",
                            "000000000",
                            "000000000",
                            "000000000",
                            "000000000",
                            0,
                            BigDecimal.ZERO,
                            0,
                            BigDecimal.ZERO,
                            0,
                            BigDecimal.ZERO,
                            false,
                            0,
                            BigDecimal.valueOf(0)));
        }
        return values;
    }

    private EifdTifReferenceDataRepository references() {
        EifdTifReferenceDataRepository references = mock(EifdTifReferenceDataRepository.class);
        long[] agencies = {1, 2, 3, 4, 5, 6, 7, 11, 12, 20};
        int[] towns = {10, 12, 13, 14, 20, 22, 37, 71, 76, 77};
        String[] names = {
            "BARRINGTON", "BLOOM", "BREMEN", "CALUMET", "LEYDEN",
            "MAINE", "THORNTON", "JEFFERSON", "SOUTH", "WEST"
        };
        for (int index = 0; index < agencies.length; index++) {
            String agencyNumber = String.format("%09d", agencies[index]);
            String taxCode = String.format("%02d001", towns[index]);
            when(references.findAgency(agencyNumber))
                    .thenReturn(
                            Optional.of(
                                    new AgencyReference(agencyNumber, "Agency " + agencyNumber)));
            when(references.findTown(String.format("%02d", towns[index])))
                    .thenReturn(
                            Optional.of(
                                    new TownReference(
                                            String.format("%02d", towns[index]), names[index])));
            when(references.findTaxCodeMaster(taxCode))
                    .thenReturn(
                            Optional.of(
                                    new TaxCodeMasterReference(
                                            taxCode,
                                            BigDecimal.ZERO,
                                            List.of(new AgencySlot(1, agencyNumber)))));
        }
        return references;
    }

    private EifdTifIncrementRunRequest request() {
        return new EifdTifIncrementRunRequest(
                "10000",
                LocalDate.of(2026, 1, 1),
                "08:00:00",
                "reviewed-eifd-trace",
                "26",
                "260181202526",
                "2026");
    }
}
