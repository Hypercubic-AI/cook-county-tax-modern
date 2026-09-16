package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.application.batch.InMemoryBatchRunStore;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.BatchEvidence;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.ProcessResult;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class PropertyTaxExemptionsRunServiceTest {

    @Test
    void identicalReplayReturnsOriginalRunAndDispatchesOnlyOnce() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        ProcessResult processResult = success();
        when(processor.process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED))
                .thenReturn(processResult);
        PropertyTaxExemptionsFactorOutcomeProjector projector =
                mock(PropertyTaxExemptionsFactorOutcomeProjector.class);
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        Outcome outcome =
                new Outcome(
                        0, List.of(), Map.of(), List.of(), Map.of(), List.of(), null, false, false);
        when(projector.scenarioId(HomeownerVariant.ENUMERATED))
                .thenReturn("homeowner-enumerated-variant");
        when(projector.project(HomeownerVariant.ENUMERATED, processResult)).thenReturn(outcome);
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        PropertyTaxExemptionsRunService service =
                new PropertyTaxExemptionsRunService(
                        processor,
                        projector,
                        recorder,
                        store,
                        Runnable::run,
                        InMemoryBatchRunStore.lifecycle(store));
        PropertyTaxExemptionsRunRequest request = request("same-key", "ENUMERATED");

        BatchRunStart<PropertyTaxExemptionsRunResponse> first =
                success(service.startPropertyTaxExemptionsRun(request));
        BatchRunStart<PropertyTaxExemptionsRunResponse> replay =
                success(service.startPropertyTaxExemptionsRun(request));

        assertThat(first.id()).isGreaterThan((long) Integer.MAX_VALUE);
        assertThat(first.response().status()).isEqualTo("QUEUED");
        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("COMPLETED");
        verify(processor, times(1))
                .process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
        verify(recorder)
                .completed("homeowner-enumerated-variant", first.id(), "COMPLETED", outcome);
    }

    @Test
    void reusedKeyWithDifferentControlsIsAConflict() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        when(processor.process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED))
                .thenReturn(success());
        PropertyTaxExemptionsRunService service = service(processor);
        success(service.startPropertyTaxExemptionsRun(request("same-key", "ENUMERATED")));

        assertThat(failure(service.startPropertyTaxExemptionsRun(request("same-key", "BROAD"))))
                .isInstanceOf(BatchRunFailure.IdempotencyConflict.class);
    }

    @Test
    void unsupportedSourceOnlyVariantIsRejectedBeforeDispatch() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        PropertyTaxExemptionsRunService service = service(processor);

        assertThat(failure(service.startPropertyTaxExemptionsRun(request("ashma-key", "ASHMA850"))))
                .isEqualTo(
                        new BatchRunFailure.InvalidRequest(
                                "homeownerProcessingVariant must be ENUMERATED or BROAD"));
        verify(processor, times(0))
                .process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
    }

    @Test
    void admissionRejectionRemainsReplayVisibleAsFailedSnapshot() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        PropertyTaxExemptionsRunService service =
                new PropertyTaxExemptionsRunService(
                        processor,
                        mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                        mock(FactorBatchOutcomeRecorder.class),
                        store,
                        command -> {
                            throw new java.util.concurrent.RejectedExecutionException("full");
                        },
                        InMemoryBatchRunStore.lifecycle(store));
        PropertyTaxExemptionsRunRequest request = request("capacity-key", "ENUMERATED");

        assertThat(failure(service.startPropertyTaxExemptionsRun(request)))
                .isInstanceOf(BatchRunFailure.AdmissionRejected.class);
        BatchRunStart<PropertyTaxExemptionsRunResponse> replay =
                success(service.startPropertyTaxExemptionsRun(request));

        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("FAILED");
        var messages = requireNonNull(replay.response().messages(), "failed replay messages");
        assertThat(messages.getFirst().severity()).isEqualTo("ERROR");
        verify(processor, times(0))
                .process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED);
    }

    @Test
    void workerFailurePublishesFailedSnapshotAndWithholdsHomeout() {
        PropertyTaxExemptionsProcessor processor = mock(PropertyTaxExemptionsProcessor.class);
        when(processor.process(LocalDate.of(2025, 9, 15), "12:00:00", HomeownerVariant.ENUMERATED))
                .thenThrow(new IllegalStateException("repository unavailable"));
        PropertyTaxExemptionsRunService service = service(processor);

        BatchRunStart<PropertyTaxExemptionsRunResponse> queued =
                success(
                        service.startPropertyTaxExemptionsRun(
                                request("failure-key", "ENUMERATED")));
        PropertyTaxExemptionsRunResponse failed =
                success(service.getPropertyTaxExemptionsRun(queued.id()));

        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.returnCode()).isEqualTo(16);
        var outputs = requireNonNull(failed.outputs(), "failed run outputs");
        assertThat(outputs).hasSize(1);
        var output = outputs.getFirst();
        assertThat(output.name()).isEqualTo("ASREA859 HOMEOUT");
        assertThat(output.publicationStatus()).isEqualTo("WITHHELD");
    }

    private static <T> T success(Result<T, BatchRunFailure> result) {
        return switch (result) {
            case Result.Ok<T, BatchRunFailure>(var value) -> value;
            case Result.Err<T, BatchRunFailure>(var error) ->
                    throw new AssertionError("Expected success but got " + error);
        };
    }

    private static <T> BatchRunFailure failure(Result<T, BatchRunFailure> result) {
        return switch (result) {
            case Result.Ok<T, BatchRunFailure>(var value) ->
                    throw new AssertionError("Expected failure but got " + value);
            case Result.Err<T, BatchRunFailure>(var error) -> error;
        };
    }

    private static PropertyTaxExemptionsRunService service(
            PropertyTaxExemptionsProcessor processor) {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        return new PropertyTaxExemptionsRunService(
                processor,
                mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                mock(org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.class),
                store,
                Runnable::run,
                InMemoryBatchRunStore.lifecycle(store));
    }

    private static PropertyTaxExemptionsRunRequest request(String key, String variant) {
        return new PropertyTaxExemptionsRunRequest(
                LocalDate.of(2025, 9, 15), "12:00:00", variant, key);
    }

    private static ProcessResult success() {
        return new ProcessResult(
                0,
                60,
                20,
                7,
                3,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new BatchEvidence(List.of(), List.of(), List.of(), List.of(), 0, 0, 0, List.of()));
    }
}
