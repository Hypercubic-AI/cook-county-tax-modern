package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.cookcounty.tax.application.service.TaxRateInputKernel.Operation;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;
import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.cookcounty.tax.domain.port.out.TaxRateInputReferenceDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TaxRateInputProcessorTest {

    @Test
    void reviewedClkattchCaseReproducesTenUnmatchedTaxCodesAndZeroAgencyOutput() {
        TaxRateInputReferenceDataRepository referenceData =
                mock(TaxRateInputReferenceDataRepository.class);
        FrozenAgencyAdjustmentRepository frozen = mock(FrozenAgencyAdjustmentRepository.class);
        when(referenceData.findEqualizedValuesInSourceOrder()).thenReturn(equalizedValues());
        when(referenceData.findDivisionsInSourceOrder()).thenReturn(divisions());
        TaxRateInputProcessor processor = new TaxRateInputProcessor(referenceData, frozen);

        Result result = processor.process();

        assertEquals(0, result.returnCode());
        assertEquals(10, result.divisionStamping().equalizedValueRecordsRead());
        assertEquals(10, result.divisionStamping().divisionRecordsRead());
        assertEquals(10, result.divisionStamping().outputRecordsWritten());
        assertEquals(10, result.agencyAttachment().assessmentRecordsRead());
        assertEquals(10, result.agencyAttachment().assessmentRecordsUnmatched());
        assertEquals(0, result.agencyAttachment().assessmentRecordsWritten());
        assertEquals(0, result.agencyAssessments().size());
        assertEquals(10, result.messages().stream()
                .filter(message -> message.code().equals("UNMATCHED_TAX_CODE"))
                .count());
    }

    @Test
    void clrtm755UsesCompositeTaxCodeAgencyIdentityWhenRewriting() {
        TaxRateInputReferenceDataRepository referenceData =
                mock(TaxRateInputReferenceDataRepository.class);
        FrozenAgencyAdjustmentRepository frozen = mock(FrozenAgencyAdjustmentRepository.class);
        FrozenAgencyAdjustment existing = new FrozenAgencyAdjustment();
        existing.setTaxCode("10001");
        existing.setAgencyNumber("000000001");
        existing.setAnnexedEqualizedValue(7L);
        when(frozen.findByTaxCodeAndAgencyNumber("10001", "000000001"))
                .thenReturn(Optional.of(existing));
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        TaxRateInputProcessor processor = new TaxRateInputProcessor(referenceData, frozen);

        Operation operation = processor.post("10001", "000000001", 5, true);

        assertEquals(Operation.REWRITE, operation);
        assertEquals(12, existing.getAnnexedEqualizedValue());
        verify(frozen).findByTaxCodeAndAgencyNumber("10001", "000000001");
    }

    @Test
    void clrtm755InitializesOnlyListedFieldsForInsertedCompositeKey() {
        TaxRateInputReferenceDataRepository referenceData =
                mock(TaxRateInputReferenceDataRepository.class);
        FrozenAgencyAdjustmentRepository frozen = mock(FrozenAgencyAdjustmentRepository.class);
        when(frozen.findByTaxCodeAndAgencyNumber("10001", "000000009"))
                .thenReturn(Optional.empty());
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        TaxRateInputProcessor processor = new TaxRateInputProcessor(referenceData, frozen);

        Operation operation = processor.post("10001", "000000009", 25, false);

        ArgumentCaptor<FrozenAgencyAdjustment> saved =
                ArgumentCaptor.forClass(FrozenAgencyAdjustment.class);
        verify(frozen).save(saved.capture());
        FrozenAgencyAdjustment inserted = saved.getValue();
        assertEquals(Operation.INSERT, operation);
        assertEquals("10001", inserted.getTaxCode());
        assertEquals("000000009", inserted.getAgencyNumber());
        assertEquals(25, inserted.getDisconnectedEqualizedValue());
        assertEquals(0, inserted.getAnnexedEqualizedValue());
        assertEquals(0, inserted.getFrozenEqualizedValue());
        assertEquals(0, inserted.getExpiredIncentiveEqualizedValue());
        assertNull(inserted.getExpiredIncentiveTaxAmount());
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
                .map(value -> new TaxRateDivision(
                        value.sourceOrder(),
                        value.volumeNumber(),
                        value.parcelNumber(),
                        1_000_000L + value.sourceOrder()))
                .toList();
    }

    private static TaxRateEqualizedValue equalized(
            int sourceOrder, int volume, long property, int taxCode) {
        return new TaxRateEqualizedValue(
                sourceOrder, volume, property, taxCode, 100L, 100L, "0");
    }
}
