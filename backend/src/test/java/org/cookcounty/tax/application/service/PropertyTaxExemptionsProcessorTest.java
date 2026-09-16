package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.OutputRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RuleDisposition;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.model.PropertyTaxRenewal;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class PropertyTaxExemptionsProcessorTest {

    @Test
    void bothReviewedVariantsReproduceTenNonResidentialRootsAndEmptyHomeout() {
        HomeownerMasterRepository homeowners = mock(HomeownerMasterRepository.class);
        HomeownerExemptionRepository exemptions = mock(HomeownerExemptionRepository.class);
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);

        List<HomeownerMaster> homeownerRows = homeownerRows();
        List<AssessmentParcel> parcelRows = parcelRows();
        List<HomeownerExemption> staleExemptions = staleExemptions();
        when(homeowners.findAllInPropertyOrder()).thenReturn(homeownerRows);
        when(homeowners.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcels.findAllInInputOrder()).thenReturn(parcelRows);
        when(details.findAllInInputOrder()).thenReturn(List.of());
        when(exemptions.findAllInPersistenceOrder()).thenReturn(staleExemptions);
        when(exemptions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PropertyTaxExemptionsProcessor processor =
                new PropertyTaxExemptionsProcessor(
                        homeowners,
                        exemptions,
                        parcels,
                        details,
                        PropertyTaxExemptionsProcessorTest::reviewedRenewals,
                        new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));

        var enumerated =
                processor.process(
                        LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
        var broad =
                processor.process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.BROAD);

        assertReviewedZeroOutput(enumerated, "ASREA852 HOMEOUT");
        assertReviewedZeroOutput(broad, "ASREA853 HOMEOUT");
        assertThat(enumerated.outputs())
                .isEqualTo(
                        broad.outputs().stream()
                                .map(
                                        PropertyTaxExemptionsProcessorTest
                                                ::normalizeBroadVariantOutput)
                                .toList());
        verify(exemptions, times(20)).deleteById(any());
        ArgumentCaptor<HomeownerMaster> renewed = ArgumentCaptor.forClass(HomeownerMaster.class);
        verify(homeowners, times(14)).save(renewed.capture());
        assertThat(renewed.getAllValues().stream().filter(row -> row.responseStatus() == 2).count())
                .isEqualTo(14L);
    }

    @Test
    void publicationAndRuleCountsFollowTheCurrentInputsInsteadOfScenarioTotals() {
        HomeownerMasterRepository homeowners = mock(HomeownerMasterRepository.class);
        HomeownerExemptionRepository exemptions = mock(HomeownerExemptionRepository.class);
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        HomeownerMaster homeowner = homeownerRows().getFirst();
        when(homeowners.findAllInPropertyOrder()).thenReturn(List.of(homeowner));
        when(homeowners.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcels.findAllInInputOrder()).thenReturn(List.of());
        when(details.findAllInInputOrder()).thenReturn(List.of());
        when(exemptions.findAllInPersistenceOrder()).thenReturn(List.of());
        PropertyTaxExemptionsProcessor processor =
                new PropertyTaxExemptionsProcessor(
                        homeowners,
                        exemptions,
                        parcels,
                        details,
                        () -> List.of(new PropertyTaxRenewal(homeowner.propertyNumber(), "B0011")),
                        new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));

        var result =
                processor.process(
                        LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);

        assertThat(
                        result.outputs().stream()
                                .collect(
                                        java.util.stream.Collectors.toMap(
                                                OutputRecord::name, OutputRecord::recordCount)))
                .containsExactly(
                        "ASREA841 HOMEOUT", 1L,
                        "ASREA841 PRINTOUT", 7L,
                        "ASREA841 ERRPRINT", 6L,
                        "ASREA852 HOMEOUT", 0L,
                        "ASREA852 PRINTOUT", 3L,
                        "ASREA859 HOMEOUT", 0L,
                        "ASREA859 PRINTOUT", 3L);
        assertThat(
                        result.ruleOutcomes().stream()
                                .filter(outcome -> outcome.ruleId().startsWith("asrea841-"))
                                .collect(
                                        java.util.stream.Collectors.toMap(
                                                RuleDisposition::ruleId,
                                                RuleDisposition::recordsAffected)))
                .containsExactly(
                        "asrea841-001", 1L,
                        "asrea841-002", 1L,
                        "asrea841-003", 0L,
                        "asrea841-004", 0L);
        assertThat(result.rejections()).isEmpty();
    }

    @Test
    void syntheticBroadPositiveRuleExampleRecomputesProrationWithoutClaimingReviewedParity() {
        HomeownerMasterRepository homeowners = mock(HomeownerMasterRepository.class);
        HomeownerExemptionRepository exemptions = mock(HomeownerExemptionRepository.class);
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);

        HomeownerMaster homeowner =
                homeowner(
                        1L,
                        "55500000000000",
                        "005",
                        "10001",
                        150,
                        25,
                        new BigDecimal("0.250000"),
                        1,
                        new BigDecimal("90000"),
                        1);
        AssessmentParcel parcel =
                parcel(
                        1L,
                        homeowner.propertyNumber(),
                        "005",
                        "10001",
                        150,
                        new BigDecimal("90000"));
        AssessmentDetail detail =
                detail(parcel.parcelNumber(), parcel.volumeNumber(), 150, new BigDecimal("90000"));

        when(homeowners.findAllInPropertyOrder()).thenReturn(List.of(homeowner));
        when(homeowners.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcels.findAllInInputOrder()).thenReturn(List.of(parcel));
        when(details.findAllInInputOrder()).thenReturn(List.of(detail));
        when(exemptions.findAllInPersistenceOrder()).thenReturn(List.of());
        when(exemptions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PropertyTaxExemptionsProcessor processor =
                new PropertyTaxExemptionsProcessor(
                        homeowners,
                        exemptions,
                        parcels,
                        details,
                        List::of,
                        new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));

        var result =
                processor.process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.BROAD);

        ArgumentCaptor<HomeownerExemption> published =
                ArgumentCaptor.forClass(HomeownerExemption.class);
        verify(exemptions).save(published.capture());
        assertThat(published.getValue().proration()).isEqualTo(new BigDecimal("0.750000"));
        assertThat(
                        result.outputs().stream()
                                .filter(output -> "ASREA859 HOMEOUT".equals(output.name()))
                                .map(OutputRecord::publicationStatus)
                                .toList())
                .containsExactly("PUBLISHED");
    }

    @Test
    void renewalMergeDerivesMatchesAndRejectionsFromDistinctKeys() {
        HomeownerMasterRepository homeowners = mock(HomeownerMasterRepository.class);
        HomeownerExemptionRepository exemptions = mock(HomeownerExemptionRepository.class);
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        HomeownerMaster homeowner =
                homeowner(
                        1L,
                        "42",
                        "7",
                        "31",
                        202,
                        25,
                        new BigDecimal("1.000000"),
                        1,
                        new BigDecimal("123456"),
                        0);

        when(homeowners.findAllInPropertyOrder()).thenReturn(List.of(homeowner));
        when(homeowners.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcels.findAllInInputOrder()).thenReturn(List.of());
        when(details.findAllInInputOrder()).thenReturn(List.of());
        when(exemptions.findAllInPersistenceOrder()).thenReturn(List.of());
        var processor =
                new PropertyTaxExemptionsProcessor(
                        homeowners,
                        exemptions,
                        parcels,
                        details,
                        () ->
                                List.of(
                                        new PropertyTaxRenewal("42", "MATCH"),
                                        new PropertyTaxRenewal("43", "MISS3"),
                                        new PropertyTaxRenewal("42", "LOW42")),
                        new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));

        var result =
                processor.process(
                        LocalDate.of(2043, 6, 24), "17:45:23", HomeownerVariant.ENUMERATED);

        assertThat(result.batchEvidence().renewalPrintRecords()).hasSize(1);
        assertThat(result.batchEvidence().renewalErrorRecords()).hasSize(2);
        assertThat(
                        result.rejections().stream()
                                .map(PropertyTaxExemptionsProcessor.Rejection::message)
                                .toList())
                .containsExactly(
                        "Renewal has no matching homeowner and cannot create one.",
                        "Renewal key is below the current merge position.")
                .inOrder();
        assertThat(result.batchEvidence().renewalPrintRecords().getFirst().propertyNumber())
                .isEqualTo("000000000000042");
        assertThat(result.batchEvidence().batchDisplays())
                .containsExactly("PROGRAM ASREA841 DATE AND TIME OF RUN =  43/06/20   01745");
    }

    @Test
    void homeownerRejectsFractionalWholeDollarValueBeforePersistence() {
        assertThrows(
                ArithmeticException.class,
                () ->
                        homeowner(
                                1L,
                                "42",
                                "7",
                                "31",
                                202,
                                25,
                                new BigDecimal("1.000000"),
                                1,
                                new BigDecimal("123456.5"),
                                0));
    }

    private static OutputRecord normalizeBroadVariantOutput(OutputRecord output) {
        if (!output.name().startsWith("ASREA853")) {
            return output;
        }
        // The accepted matched-homeowner mutation is 852-005 but 853-004.
        List<String> ruleIds =
                output.ruleIds().stream()
                        .map(
                                id ->
                                        "ASREA853 HOMEOUT".equals(output.name())
                                                        && "asrea853-004".equals(id)
                                                ? "asrea852-005"
                                                : id.replace("asrea853", "asrea852"))
                        .toList();
        return new OutputRecord(
                output.name().replace("ASREA853", "ASREA852"),
                output.kind(),
                output.recordCount(),
                output.publicationStatus(),
                ruleIds);
    }

    private static void assertReviewedZeroOutput(
            PropertyTaxExemptionsProcessor.ProcessResult result, String variantOutputName) {
        assertThat(result.returnCode()).isEqualTo(0);
        assertThat(result.recordsRejected()).isEqualTo(3);
        assertThat(
                        result.rejections().stream()
                                .filter(rejection -> "BYPASSED".equals(rejection.outcome()))
                                .count())
                .isEqualTo(10L);
        List<OutputRecord> variantOutputs =
                result.outputs().stream()
                        .filter(output -> variantOutputName.equals(output.name()))
                        .toList();
        assertThat(variantOutputs).hasSize(1);
        assertThat(variantOutputs.getFirst().recordCount()).isEqualTo(0);
        assertThat(variantOutputs.getFirst().publicationStatus()).isEqualTo("EMPTY");
        List<OutputRecord> homeOutputs =
                result.outputs().stream()
                        .filter(output -> "ASREA859 HOMEOUT".equals(output.name()))
                        .toList();
        assertThat(homeOutputs).hasSize(1);
        assertThat(homeOutputs.getFirst().recordCount()).isEqualTo(0);
        assertThat(homeOutputs.getFirst().publicationStatus()).isEqualTo("EMPTY");
        assertThat(
                        result.ruleOutcomes().stream()
                                .filter(outcome -> "ashma850-001".equals(outcome.ruleId()))
                                .map(RuleDisposition::outcome)
                                .toList())
                .containsExactly("NOT_APPLICABLE");
    }

    private static List<HomeownerMaster> homeownerRows() {
        String[] properties = {
            "010011000010000", "012022000020000", "013066000060000", "014077000070000",
            "020033000030000", "022055000050000", "037044000040000", "071011100110000",
            "076022200120000", "077033300130000"
        };
        List<HomeownerMaster> rows = new ArrayList<>();
        for (int index = 0; index < properties.length; index++) {
            rows.add(
                    homeowner(
                            (long) index + 1,
                            properties[index],
                            String.format("%03d", index + 1),
                            String.format("%05d", 10_000 + index),
                            202 + index,
                            index,
                            BigDecimal.ONE,
                            1,
                            new BigDecimal("100000"),
                            0));
        }
        return rows;
    }

    private static List<AssessmentParcel> parcelRows() {
        String[] properties = {
            "010011000010000", "012022000020000", "013066000060000", "014077000070000",
            "020033000030000", "022055000050000", "037044000040000", "071011100110000",
            "076022200120000", "077033300130000"
        };
        String[] taxCodes = {
            "10001", "12001", "13001", "14001", "20001", "22001", "37001", "71001", "76001", "77001"
        };
        List<AssessmentParcel> rows = new ArrayList<>();
        for (int index = 0; index < properties.length; index++) {
            rows.add(
                    parcel(
                            (long) index + 1,
                            properties[index],
                            String.format("%03d", index + 1),
                            taxCodes[index],
                            202 + index,
                            new BigDecimal("100000")));
        }
        return rows;
    }

    private static List<PropertyTaxRenewal> reviewedRenewals() {
        return List.of(
                new PropertyTaxRenewal("10011000010000", "B0001"),
                new PropertyTaxRenewal("12022000020000", "B0002"),
                new PropertyTaxRenewal("13066000060000", "B0003"),
                new PropertyTaxRenewal("14077000070000", "B0004"),
                new PropertyTaxRenewal("20033000030000", "B0005"),
                new PropertyTaxRenewal("37044000040000", "B0007"),
                new PropertyTaxRenewal("76022200120000", "B0009"),
                new PropertyTaxRenewal("12022000020000", "LOW01"),
                new PropertyTaxRenewal("12500000000000", "MISS1"),
                new PropertyTaxRenewal("60000000000000", "MISS2"));
    }

    private static List<HomeownerExemption> staleExemptions() {
        List<HomeownerExemption> rows = new ArrayList<>();
        for (long id = 1; id <= 10; id++) {
            rows.add(
                    new HomeownerExemption(
                            id,
                            0L,
                            25,
                            BigDecimal.ZERO,
                            0,
                            null,
                            null,
                            0,
                            null,
                            0,
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            null,
                            String.format("%015d", id),
                            null,
                            BigDecimal.ZERO,
                            null,
                            String.format("%015d", id),
                            BigDecimal.ONE,
                            0,
                            0,
                            0,
                            null,
                            null,
                            "00000",
                            null,
                            0,
                            "001",
                            null));
        }
        return rows;
    }

    private static HomeownerMaster homeowner(
            Long id,
            String propertyNumber,
            String volumeNumber,
            String taxCode,
            int assessmentClass,
            int applicationYear,
            BigDecimal proration,
            int cooperativeQuantity,
            BigDecimal assessedValue,
            int responseStatus) {
        return new HomeownerMaster(
                id,
                0L,
                applicationYear,
                assessedValue,
                assessmentClass,
                null,
                null,
                cooperativeQuantity,
                BigDecimal.ONE,
                assessedValue,
                null,
                null,
                null,
                null,
                "AB",
                BigDecimal.ZERO,
                null,
                propertyNumber,
                proration,
                responseStatus,
                0,
                null,
                taxCode,
                0,
                null,
                0,
                volumeNumber,
                null);
    }

    private static AssessmentParcel parcel(
            Long id,
            String parcelNumber,
            String volumeNumber,
            String taxCode,
            int overallClass,
            BigDecimal currentTotalValue) {
        return new AssessmentParcel(
                id,
                0L,
                BigDecimal.ZERO,
                "",
                "",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                currentTotalValue,
                0,
                BigDecimal.ZERO,
                overallClass,
                parcelNumber,
                "",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                taxCode,
                "0",
                volumeNumber,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static AssessmentDetail detail(
            String parcelNumber, String volumeNumber, int assessmentClass, BigDecimal valuation) {
        return new AssessmentDetail(
                null,
                null,
                null,
                null,
                assessmentClass,
                "",
                null,
                null,
                null,
                null,
                null,
                "",
                "2",
                null,
                null,
                null,
                null,
                null,
                0,
                new BigDecimal("80"),
                1,
                parcelNumber,
                volumeNumber,
                new BigDecimal("75"),
                null,
                null,
                null,
                null,
                valuation);
    }
}
