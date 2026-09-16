package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cookcounty.tax.application.service.TaxRateInputKernel.Operation;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;
import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.cookcounty.tax.domain.port.out.TaxRateInputReferenceDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class TaxRateInputProcessorTest {

    @Test
    void reviewedAgencyAttachmentCaseProducesTenUnmatchedCodesAndNoAgencyOutput() {
        TaxRateInputReferenceDataRepository referenceData =
                mock(TaxRateInputReferenceDataRepository.class);
        FrozenAgencyAdjustmentRepository frozen = mock(FrozenAgencyAdjustmentRepository.class);
        when(referenceData.findEqualizedValuesInSourceOrder()).thenReturn(equalizedValues());
        when(referenceData.findDivisionsInSourceOrder()).thenReturn(divisions());
        TaxRateInputProcessor processor =
                new TaxRateInputProcessor(referenceData, frozen, new TaxRateInputKernel());

        Result result = processor.process();

        assertThat(result.returnCode()).isEqualTo(0);
        assertThat(result.divisionStamping().equalizedValueRecordsRead()).isEqualTo(10);
        assertThat(result.divisionStamping().divisionRecordsRead()).isEqualTo(10);
        assertThat(result.divisionStamping().outputRecordsWritten()).isEqualTo(10);
        assertThat(result.agencyAttachment().assessmentRecordsRead()).isEqualTo(10);
        assertThat(result.agencyAttachment().assessmentRecordsUnmatched()).isEqualTo(10);
        assertThat(result.agencyAttachment().assessmentRecordsWritten()).isEqualTo(0);
        assertThat(result.agencyAssessments().size()).isEqualTo(0);
        assertThat(
                        result.messages().stream()
                                .filter(message -> message.code().equals("UNMATCHED_TAX_CODE"))
                                .count())
                .isEqualTo(10);
    }

    @Test
    void frozenAgencyPostingUsesCompositeTaxCodeAgencyIdentityWhenRewriting() {
        TaxRateInputReferenceDataRepository referenceData =
                mock(TaxRateInputReferenceDataRepository.class);
        FrozenAgencyAdjustmentRepository frozen = mock(FrozenAgencyAdjustmentRepository.class);
        FrozenAgencyAdjustment existing = existingAdjustment();
        when(frozen.findByTaxCodeAndAgencyNumber("10001", "000000001"))
                .thenReturn(Optional.of(existing));
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        TaxRateInputProcessor processor =
                new TaxRateInputProcessor(referenceData, frozen, new TaxRateInputKernel());

        Operation operation = processor.post("10001", "000000001", new BigDecimal("5"), true);

        ArgumentCaptor<FrozenAgencyAdjustment> saved =
                ArgumentCaptor.forClass(FrozenAgencyAdjustment.class);
        verify(frozen).save(saved.capture());
        assertThat(operation).isEqualTo(Operation.REWRITE);
        assertThat(existing.annexedEqualizedValue()).isEqualTo(BigDecimal.valueOf(7));
        assertThat(saved.getValue().annexedEqualizedValue()).isEqualTo(BigDecimal.valueOf(12));
        assertThat(saved.getValue().version()).isEqualTo(4L);
        verify(frozen).findByTaxCodeAndAgencyNumber("10001", "000000001");
    }

    @Test
    void frozenAgencyPostingInitializesOnlyListedFieldsForInsertedCompositeKey() {
        TaxRateInputReferenceDataRepository referenceData =
                mock(TaxRateInputReferenceDataRepository.class);
        FrozenAgencyAdjustmentRepository frozen = mock(FrozenAgencyAdjustmentRepository.class);
        when(frozen.findByTaxCodeAndAgencyNumber("10001", "000000009"))
                .thenReturn(Optional.empty());
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        TaxRateInputProcessor processor =
                new TaxRateInputProcessor(referenceData, frozen, new TaxRateInputKernel());

        Operation operation = processor.post("10001", "000000009", new BigDecimal("25"), false);

        ArgumentCaptor<FrozenAgencyAdjustment> saved =
                ArgumentCaptor.forClass(FrozenAgencyAdjustment.class);
        verify(frozen).save(saved.capture());
        FrozenAgencyAdjustment inserted = saved.getValue();
        assertThat(operation).isEqualTo(Operation.INSERT);
        assertThat(inserted.taxCode()).isEqualTo("10001");
        assertThat(inserted.agencyNumber()).isEqualTo("000000009");
        assertThat(inserted.disconnectedEqualizedValue()).isEqualTo(new BigDecimal("25"));
        assertThat(inserted.annexedEqualizedValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(inserted.frozenEqualizedValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(inserted.expiredIncentiveEqualizedValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(inserted.expiredIncentiveTaxAmount()).isNull();
    }

    private static List<TaxRateEqualizedValue> equalizedValues() {
        return List.of(
                equalized(1, 1, 10_011_000_010_000L, 10001),
                equalized(2, 2, 12_022_000_020_000L, 12001),
                equalized(3, 3, 13_066_000_060_000L, 13001),
                equalized(4, 4, 14_077_000_070_000L, 14001),
                equalized(5, 5, 20_033_000_030_000L, 20001),
                equalized(6, 6, 22_055_000_050_000L, 22001),
                equalized(7, 7, 37_044_000_040_000L, 37001),
                equalized(8, 10, 71_011_100_110_000L, 71001),
                equalized(9, 11, 76_022_200_120_000L, 76001),
                equalized(10, 12, 77_033_300_130_000L, 77001));
    }

    private static List<TaxRateDivision> divisions() {
        return equalizedValues().stream()
                .map(
                        value ->
                                new TaxRateDivision(
                                        value.sourceOrder(),
                                        value.volumeNumber(),
                                        value.parcelNumber(),
                                        String.format("%014d", 1_000_000L + value.sourceOrder()),
                                        null))
                .toList();
    }

    private static TaxRateEqualizedValue equalized(
            int sourceOrder, int volume, long property, int taxCode) {
        return new TaxRateEqualizedValue(
                sourceOrder,
                String.format("%03d", volume),
                String.format("%015d", property),
                String.format("%05d", taxCode),
                new BigDecimal("100"),
                new BigDecimal("100"),
                "0",
                null);
    }

    private static FrozenAgencyAdjustment existingAdjustment() {
        return new FrozenAgencyAdjustment(
                9L,
                4L,
                "000000001",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(7),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                null,
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                java.math.BigDecimal.ZERO.setScale(2),
                "10001",
                java.math.BigDecimal.ZERO.setScale(3),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
    }
}
