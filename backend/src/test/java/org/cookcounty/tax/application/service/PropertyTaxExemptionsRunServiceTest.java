package org.cookcounty.tax.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.BatchEvidence;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.ProcessResult;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunRequest;
import org.junit.jupiter.api.Test;

class PropertyTaxExemptionsRunServiceTest {

    @Test
    void identicalReplayReturnsOriginalRunAndDispatchesOnlyOnce() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        ProcessResult processResult = success();
        when(processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED))
                .thenReturn(processResult);
        PropertyTaxExemptionsFactorOutcomeProjector projector =
                mock(PropertyTaxExemptionsFactorOutcomeProjector.class);
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        Outcome outcome = new Outcome(
                0, List.of(), Map.of(), List.of(), Map.of(), List.of(), null, false, false);
        when(projector.scenarioId(HomeownerVariant.ENUMERATED))
                .thenReturn("homeowner-enumerated-variant");
        when(projector.project(HomeownerVariant.ENUMERATED, processResult)).thenReturn(outcome);
        PropertyTaxExemptionsRunService service =
                new PropertyTaxExemptionsRunService(
                        processor, projector, recorder, Runnable::run);
        PropertyTaxExemptionsRunRequest request = request("same-key", "ENUMERATED");

        var first = service.startPropertyTaxExemptionsRun(request);
        var replay = service.startPropertyTaxExemptionsRun(request);

        assertThat(first.getId()).isGreaterThan(Integer.MAX_VALUE);
        assertThat(first.getStatus()).isEqualTo("QUEUED");
        assertThat(replay.getId()).isEqualTo(first.getId());
        assertThat(replay.getStatus()).isEqualTo("COMPLETED");
        verify(processor, times(1)).process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
        verify(recorder).completed(
                "homeowner-enumerated-variant", first.getId(), "COMPLETED", outcome);
    }

    @Test
    void reusedKeyWithDifferentControlsIsAConflict() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        when(processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED))
                .thenReturn(success());
        PropertyTaxExemptionsRunService service = service(processor);
        service.startPropertyTaxExemptionsRun(request("same-key", "ENUMERATED"));

        assertThatThrownBy(() -> service.startPropertyTaxExemptionsRun(
                request("same-key", "BROAD")))
                .isInstanceOf(BatchRunIdempotencyConflictException.class);
    }

    @Test
    void unsupportedSourceOnlyVariantIsRejectedBeforeDispatch() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        PropertyTaxExemptionsRunService service = service(processor);

        assertThatThrownBy(() -> service.startPropertyTaxExemptionsRun(
                request("ashma-key", "ASHMA850")))
                .isInstanceOf(BatchRunInvalidRequestException.class)
                .hasMessage("homeownerProcessingVariant must be ENUMERATED or BROAD");
        verify(processor, times(0)).process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
    }

    @Test
    void workerFailurePublishesFailedSnapshotAndWithholdsHomeout() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        when(processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED))
                .thenThrow(new IllegalStateException("repository unavailable"));
        PropertyTaxExemptionsRunService service = service(processor);

        var queued = service.startPropertyTaxExemptionsRun(
                request("failure-key", "ENUMERATED"));
        var failed = service.getPropertyTaxExemptionsRun(queued.getId()).orElseThrow();

        assertThat(failed.getStatus()).isEqualTo("FAILED");
        assertThat(failed.getReturnCode()).isEqualTo(16);
        assertThat(failed.getOutputs()).singleElement().satisfies(output -> {
            assertThat(output.getName()).isEqualTo("ASREA859 HOMEOUT");
            assertThat(output.getPublicationStatus()).isEqualTo("WITHHELD");
        });
    }

    private static PropertyTaxExemptionsRunService service(
            PropertyTaxExemptionsProcessor processor) {
        return new PropertyTaxExemptionsRunService(
                processor,
                mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                mock(org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.class),
                Runnable::run);
    }

    private static PropertyTaxExemptionsRunRequest request(String key, String variant) {
        PropertyTaxExemptionsRunRequest request = new PropertyTaxExemptionsRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime("12:00:00");
        request.setIdempotencyKey(key);
        request.setHomeownerProcessingVariant(variant);
        return request;
    }

    private static ProcessResult success() {
        return new ProcessResult(
                0, 60, 20, 7, 3,
                List.of(), List.of(), List.of(), List.of(), List.of(),
                new BatchEvidence(
                        List.of(), List.of(), List.of(), List.of(), 0, 0, 0, List.of()));
    }
}
