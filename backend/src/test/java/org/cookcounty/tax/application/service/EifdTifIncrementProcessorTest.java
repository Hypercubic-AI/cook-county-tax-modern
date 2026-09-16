package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class EifdTifIncrementProcessorTest {

    @Test
    void reviewedNineStepTracePublishesReportsAndMutatesTenFrozenAndNineAgencies() {
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        FrozenValuationRepository frozen = mock(FrozenValuationRepository.class);
        AgencyEqualizedValuationRepository agencies = mock(AgencyEqualizedValuationRepository.class);
        AssessmentDetailValuator valuator = mock(AssessmentDetailValuator.class);

        when(parcels.findAllInInputOrder()).thenReturn(parcels());
        when(details.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(frozen.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(frozenValues()));
        when(agencies.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(agencyValues()));
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agencies.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EifdTifIncrementProcessor processor = new EifdTifIncrementProcessor(
                parcels, details, frozen, agencies, valuator);
        EifdTifIncrementProcessResult result = processor.process(request());

        assertTrue(result.completed());
        assertEquals(0, result.returnCode());
        assertEquals(19, result.recordsUpdated());
        assertEquals(List.of(14, 75, 117),
                result.outputs().stream()
                        .skip(1)
                        .map(EifdTifIncrementProcessResult.Output::recordCount)
                        .toList());
        assertEquals(10, result.outputs().get(0).recordCount());
        verify(frozen, times(10)).save(any());
        verify(agencies, times(9)).save(any());
        assertEquals(10, result.asrea740ReportFacts().size());
        ArgumentCaptor<FrozenValuation> frozenCaptor =
                ArgumentCaptor.forClass(FrozenValuation.class);
        verify(frozen, times(10)).save(frozenCaptor.capture());
        FrozenValuation firstFrozenUpdate = frozenCaptor.getAllValues().get(0);
        assertEquals(30L, firstFrozenUpdate.getPriorLandValue());
        assertEquals(70L, firstFrozenUpdate.getPriorImprovementValue());
        assertEquals(100L, firstFrozenUpdate.getPriorTotalValue());
        assertEquals(40L, firstFrozenUpdate.getCurrentLandValue());
        assertEquals(80L, firstFrozenUpdate.getCurrentImprovementValue());
        assertEquals(120L, firstFrozenUpdate.getCurrentTotalValue());
        assertEquals(100L, result.asrea740ReportFacts().get(0).proposedImprovementValue());
        firstFrozenUpdate.setProposedImprovementValue(999L);
        assertEquals(100L, result.asrea740ReportFacts().get(0).proposedImprovementValue());
        assertEquals(12L, firstFrozenUpdate.getNoChangeActionPriorParcelCount());
        assertEquals(0L, firstFrozenUpdate.getChangeActionPriorParcelCount());
        FrozenValuation secondFrozenUpdate = frozenCaptor.getAllValues().get(1);
        assertEquals(13L, secondFrozenUpdate.getChangeActionPriorParcelCount());
        assertEquals(0L, secondFrozenUpdate.getNoChangeActionPriorParcelCount());

        ArgumentCaptor<AgencyEqualizedValuation> captor =
                ArgumentCaptor.forClass(AgencyEqualizedValuation.class);
        verify(agencies, times(9)).save(captor.capture());
        AgencyEqualizedValuation first = captor.getAllValues().get(0);
        assertEquals(100L, first.getNewPropertyEqualizedValue());
        assertEquals(9001L, first.getCookCountyRealEstateValue());
        assertTrue(result.messages().stream().anyMatch(message -> message.text().equals(
                "TOTAL NUMBER OF CLERK'S NEW PROPERTY RECORDS READ 9")));
        assertTrue(result.messages().stream().anyMatch(message -> message.text().equals(
                "TOTAL NUMBER OF CLERK'S NEW PROPERTY RECORDS UNMATCHED 9")));
        assertTrue(result.messages().stream().anyMatch(message -> message.text().equals(
                "TOTAL NUMBER OF VSAM AGENCY EQ-VALUATION RECORDS UPDATED 9")));
    }

    private List<AssessmentParcel> parcels() {
        List<AssessmentParcel> values = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            AssessmentParcel parcel = new AssessmentParcel();
            parcel.setParcelNumber((long) index + 1);
            parcel.setTaxCode(10_001 + index);
            if (index == 0) {
                parcel.setPriorLandValue(900L);
                parcel.setPriorImprovementValue(900L);
                parcel.setPriorTotalValue(1_800L);
                parcel.setCurrentLandValue(1_000L);
                parcel.setCurrentImprovementValue(1_000L);
                parcel.setCurrentTotalValue(2_000L);
                parcel.setEifdPriorLandValue(30L);
                parcel.setEifdPriorImprovementValue(70L);
                parcel.setEifdPriorTotalValue(100L);
                parcel.setEifdCurrentLandValue(40L);
                parcel.setEifdCurrentImprovementValue(80L);
                parcel.setEifdCurrentTotalValue(120L);
            }
            values.add(parcel);
        }
        return values;
    }

    private List<FrozenValuation> frozenValues() {
        long[] divisions = {1, 2, 3, 4, 5, 6, 7, 11, 12, 13};
        List<FrozenValuation> values = new ArrayList<>();
        for (long division : divisions) {
            FrozenValuation value = new FrozenValuation();
            value.setDivisionNumber(String.format("%09d", division));
            value.setProposedImprovementValue(100L);
            if (division == 1) {
                value.setNoChangeActionPriorParcelCount(12L);
            } else if (division == 2) {
                value.setChangeActionPriorParcelCount(13L);
            }
            values.add(value);
        }
        return values;
    }

    private List<AgencyEqualizedValuation> agencyValues() {
        long[] agencyNumbers = {1, 2, 3, 4, 5, 6, 7, 11, 12};
        List<AgencyEqualizedValuation> values = new ArrayList<>();
        for (long agencyNumber : agencyNumbers) {
            AgencyEqualizedValuation value = new AgencyEqualizedValuation();
            value.setAgencyNumber(String.format("%09d", agencyNumber));
            value.setCookCountyRealEstateValue(9_000L + agencyNumber);
            values.add(value);
        }
        return values;
    }

    private EifdTifIncrementRunRequest request() {
        EifdTifIncrementRunRequest request = new EifdTifIncrementRunRequest();
        request.setBusinessDate(LocalDate.of(2026, 1, 1));
        request.setBusinessTime("08:00:00");
        request.setIdempotencyKey("reviewed-eifd-trace");
        request.setReassessmentControl("260181202526");
        request.setProcessingYear("26");
        request.setReportingYear("2026");
        request.setAnnualEqualizationFactor("10000");
        return request;
    }
}
