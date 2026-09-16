package org.cookcounty.tax.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.OutputRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RuleDisposition;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
        when(exemptions.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(staleExemptions));
        when(exemptions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PropertyTaxExemptionsProcessor processor = new PropertyTaxExemptionsProcessor(
                homeowners,
                exemptions,
                parcels,
                details,
                new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));

        var enumerated = processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
        var broad = processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.BROAD);

        assertReviewedZeroOutput(enumerated, "ASREA852 HOMEOUT");
        assertReviewedZeroOutput(broad, "ASREA853 HOMEOUT");
        assertThat(enumerated.outputs()).usingRecursiveComparison().isEqualTo(broad.outputs().stream()
                .map(PropertyTaxExemptionsProcessorTest::normalizeBroadVariantOutput)
                .toList());
        verify(exemptions, times(20)).deleteById(any());
        assertThat(homeownerRows.stream()
                .filter(row -> Integer.valueOf(2).equals(row.getResponseStatus())))
                .hasSize(7);
    }

    @Test
    void syntheticBroadPositiveRuleExampleRecomputesProrationWithoutClaimingReviewedParity() {
        HomeownerMasterRepository homeowners = mock(HomeownerMasterRepository.class);
        HomeownerExemptionRepository exemptions = mock(HomeownerExemptionRepository.class);
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);

        HomeownerMaster homeowner = new HomeownerMaster();
        homeowner.setId(1L);
        homeowner.setPropertyNumber(55500000000000L);
        homeowner.setVolumeNumber(5);
        homeowner.setProration(new BigDecimal("0.250000"));
        homeowner.setResponseStatus(1);
        AssessmentParcel parcel = new AssessmentParcel();
        parcel.setId(1L);
        parcel.setParcelNumber(homeowner.getPropertyNumber());
        parcel.setVolumeNumber(5);
        parcel.setOverallClass(150);
        parcel.setCurrentTotalValue(90_000L);
        AssessmentDetail detail = new AssessmentDetail();
        detail.setParcelNumber(parcel.getParcelNumber());
        detail.setParcelVolumeNumber(parcel.getVolumeNumber());
        detail.setOccurrenceNumber(1);
        detail.setDetailType("2");
        detail.setAssessmentClass(150);
        detail.setValuation(90_000L);
        detail.setPercentAssessed(new BigDecimal("75"));
        detail.setOccupancyFactor(new BigDecimal("80"));

        when(homeowners.findAllInPropertyOrder()).thenReturn(List.of(homeowner));
        when(homeowners.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcels.findAllInInputOrder()).thenReturn(List.of(parcel));
        when(details.findAllInInputOrder()).thenReturn(List.of(detail));
        when(exemptions.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(exemptions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PropertyTaxExemptionsProcessor processor = new PropertyTaxExemptionsProcessor(
                homeowners,
                exemptions,
                parcels,
                details,
                new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));

        var result = processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.BROAD);

        ArgumentCaptor<HomeownerExemption> published =
                ArgumentCaptor.forClass(HomeownerExemption.class);
        verify(exemptions).save(published.capture());
        assertThat(published.getValue().getProration()).isEqualByComparingTo("0.750000");
        assertThat(result.outputs())
                .filteredOn(output -> "ASREA859 HOMEOUT".equals(output.name()))
                .extracting(OutputRecord::publicationStatus)
                .containsExactly("PUBLISHED");
        assertThat(result.reconciliations().get(2).message())
                .contains("Synthetic rule examples")
                .contains("not a reviewed parity claim");
    }

    private static OutputRecord normalizeBroadVariantOutput(OutputRecord output) {
        if (!output.name().startsWith("ASREA853")) {
            return output;
        }
        // The accepted matched-homeowner mutation is 852-005 but 853-004.
        List<String> ruleIds = output.ruleIds().stream()
                .map(id -> "ASREA853 HOMEOUT".equals(output.name())
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
            PropertyTaxExemptionsProcessor.ProcessResult result,
            String variantOutputName) {
        assertThat(result.returnCode()).isZero();
        assertThat(result.recordsRejected()).isEqualTo(3);
        assertThat(result.rejections())
                .filteredOn(rejection -> "BYPASSED".equals(rejection.outcome()))
                .hasSize(10);
        assertThat(result.outputs())
                .filteredOn(output -> variantOutputName.equals(output.name()))
                .singleElement()
                .satisfies(output -> {
                    assertThat(output.recordCount()).isZero();
                    assertThat(output.publicationStatus()).isEqualTo("EMPTY");
                });
        assertThat(result.outputs())
                .filteredOn(output -> "ASREA859 HOMEOUT".equals(output.name()))
                .singleElement()
                .satisfies(output -> {
                    assertThat(output.recordCount()).isZero();
                    assertThat(output.publicationStatus()).isEqualTo("EMPTY");
                });
        assertThat(result.ruleOutcomes())
                .filteredOn(outcome -> "ashma850-001".equals(outcome.ruleId()))
                .extracting(RuleDisposition::outcome)
                .containsExactly("NOT_APPLICABLE");
    }

    private static List<HomeownerMaster> homeownerRows() {
        long[] properties = {
                10011000010000L, 12022000020000L, 13066000060000L, 14077000070000L,
                20033000030000L, 22055000050000L, 37044000040000L, 71011100110000L,
                76022200120000L, 77033300130000L
        };
        List<HomeownerMaster> rows = new ArrayList<>();
        for (int index = 0; index < properties.length; index++) {
            HomeownerMaster row = new HomeownerMaster();
            row.setId((long) index + 1);
            row.setPropertyNumber(properties[index]);
            row.setVolumeNumber(index + 1);
            row.setResponseStatus(0);
            row.setOwnerName("OWNER " + index);
            rows.add(row);
        }
        return rows;
    }

    private static List<AssessmentParcel> parcelRows() {
        long[] properties = {
                10011000010000L, 12022000020000L, 13066000060000L, 14077000070000L,
                20033000030000L, 22055000050000L, 37044000040000L, 71011100110000L,
                76022200120000L, 77033300130000L
        };
        int[] taxCodes = {
                10001, 12001, 13001, 14001, 20001, 22001, 37001, 71001, 76001, 77001
        };
        List<AssessmentParcel> rows = new ArrayList<>();
        for (int index = 0; index < properties.length; index++) {
            AssessmentParcel row = new AssessmentParcel();
            row.setId((long) index + 1);
            row.setParcelNumber(properties[index]);
            row.setVolumeNumber(index + 1);
            row.setTaxCode(taxCodes[index]);
            row.setOverallClass(202 + index);
            row.setCurrentTotalValue(100_000L);
            rows.add(row);
        }
        return rows;
    }

    private static List<HomeownerExemption> staleExemptions() {
        List<HomeownerExemption> rows = new ArrayList<>();
        for (long id = 1; id <= 10; id++) {
            HomeownerExemption row = new HomeownerExemption();
            row.setId(id);
            rows.add(row);
        }
        return rows;
    }
}
