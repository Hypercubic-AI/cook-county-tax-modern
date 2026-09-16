package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cookcounty.tax.application.batch.InMemoryBatchRunStore;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.OutputRecord;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AssessedValuePreparationRunServiceTest {

    @Mock private AssessedValuePreparationProcessor processor;

    @Mock private AssessedValuePreparationFactorOutcomeProjector outcomeProjector;

    @Mock private FactorBatchOutcomeRecorder outcomeRecorder;

    private AssessedValuePreparationRunService service;

    @BeforeEach
    void setUp() {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        service =
                new AssessedValuePreparationRunService(
                        processor,
                        outcomeProjector,
                        outcomeRecorder,
                        store,
                        Runnable::run,
                        InMemoryBatchRunStore.lifecycle(store));
    }

    @Test
    void startPublishesQueuedThenCompletedWholeSnapshotsWithLongId() {
        ProcessResult result = success();
        Outcome projected =
                new Outcome(
                        0, List.of(), Map.of(), List.of(), Map.of(), List.of(), null, false, false);
        when(processor.process(LocalDate.of(2025, 9, 15), "12:00:00", "26")).thenReturn(result);
        when(outcomeProjector.project(result)).thenReturn(projected);

        BatchRunStart<AssessedValuePreparationRunResponse> start =
                success(service.startAssessedValuePreparationRun(request("run-1", "26")));
        AssessedValuePreparationRunResponse accepted = start.response();
        AssessedValuePreparationRunResponse completed =
                success(service.getAssessedValuePreparationRun(start.id()));

        assertThat(accepted.status()).isEqualTo("QUEUED");
        assertThat(accepted.id() > Integer.MAX_VALUE).isTrue();
        assertThat(completed.status()).isEqualTo("COMPLETED");
        assertThat(completed.recordsRead()).isEqualTo(30);
        assertThat(completed.outputs()).isEqualTo(List.of());
        verify(outcomeRecorder)
                .completed("valuation-preparation", accepted.id(), "COMPLETED", projected);
    }

    @Test
    void sameIdempotencyKeyAndControlsReplayCurrentRunWithoutRedispatch() {
        when(processor.process(any(), any(), any())).thenReturn(success());

        BatchRunStart<AssessedValuePreparationRunResponse> first =
                success(service.startAssessedValuePreparationRun(request("same-key", "26")));
        BatchRunStart<AssessedValuePreparationRunResponse> replay =
                success(service.startAssessedValuePreparationRun(request("same-key", "26")));

        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("COMPLETED");
        verify(processor).process(LocalDate.of(2025, 9, 15), "12:00:00", "26");
    }

    @Test
    void sameIdempotencyKeyWithDifferentControlsConflicts() {
        when(processor.process(any(), any(), any())).thenReturn(success());
        success(service.startAssessedValuePreparationRun(request("same-key", "26")));

        assertThat(failure(service.startAssessedValuePreparationRun(request("same-key", "27"))))
                .isInstanceOf(BatchRunFailure.IdempotencyConflict.class);
    }

    @ParameterizedTest(name = "invalid process year {0} is rejected")
    @ValueSource(strings = {"2", "260", "2A"})
    void invalidProcessYearLengthAndCharactersAreRejectedBeforeDispatch(String invalid) {
        assertThat(
                        failure(
                                service.startAssessedValuePreparationRun(
                                        request("run-invalid-" + invalid, invalid))))
                .isEqualTo(
                        new BatchRunFailure.InvalidRequest(
                                "processYear must contain exactly two decimal digits."));
    }

    @Test
    void admissionRejectionPersistsAReplayVisibleFailedSnapshot() {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        AssessedValuePreparationRunService rejectingService =
                new AssessedValuePreparationRunService(
                        processor,
                        outcomeProjector,
                        outcomeRecorder,
                        store,
                        command -> {
                            throw new java.util.concurrent.RejectedExecutionException("full");
                        },
                        InMemoryBatchRunStore.lifecycle(store));

        assertThat(
                        failure(
                                rejectingService.startAssessedValuePreparationRun(
                                        request("rejected-run", "26"))))
                .isInstanceOf(BatchRunFailure.AdmissionRejected.class);

        BatchRunStart<AssessedValuePreparationRunResponse> replay =
                success(
                        rejectingService.startAssessedValuePreparationRun(
                                request("rejected-run", "26")));
        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("FAILED");
        assertThat(replay.response().messages().getFirst().code())
                .isEqualTo("BATCH_CAPACITY_EXHAUSTED");
    }

    @Test
    void workerExceptionPublishesFailedTerminalSnapshot() {
        doThrow(new IllegalStateException("database unavailable"))
                .when(processor)
                .process(any(), any(), any());

        BatchRunStart<AssessedValuePreparationRunResponse> start =
                success(service.startAssessedValuePreparationRun(request("run-fails", "26")));
        AssessedValuePreparationRunResponse failed =
                success(service.getAssessedValuePreparationRun(start.id()));

        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.returnCode()).isEqualTo(16);
        assertThat(failed.messages().getFirst().code()).isEqualTo("WORKER_FAILED");
    }

    @Test
    void
            semanticOrderingFailureRemainsFailedWhenLegacyReturnCodeIsZeroAndMarksEveryOutputPartial() {
        StageOutcome failedStage =
                new StageOutcome("VALUATION_BUCKETING", "FAILED", 0, 2, 1, 1, 1, true, true, 3);
        when(processor.process(any(), any(), any()))
                .thenReturn(
                        new ProcessResult(
                                true,
                                0,
                                2,
                                1,
                                1,
                                1,
                                true,
                                List.of(
                                        new OutputRecord(
                                                "VALUATION_BREAKDOWN_REPORT", "text/plain", 3)),
                                List.of(),
                                List.of(failedStage)));

        BatchRunStart<AssessedValuePreparationRunResponse> start =
                success(
                        service.startAssessedValuePreparationRun(
                                request("semantic-failure", "26")));
        AssessedValuePreparationRunResponse failed =
                success(service.getAssessedValuePreparationRun(start.id()));

        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.returnCode()).isEqualTo(0);
        assertThat(failed.partialOutput()).isTrue();
        assertThat(failed.outputs().getFirst().partial()).isTrue();
        assertThat(failed.stages().getFirst().partialOutput()).isTrue();
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

    private static ProcessResult success() {
        return new ProcessResult(false, 0, 30, 30, 10, 0, false, List.of(), List.of(), List.of());
    }

    private static AssessedValuePreparationRunRequest request(String key, String processYear) {
        return new AssessedValuePreparationRunRequest(
                LocalDate.of(2025, 9, 15), "12:00:00", key, processYear);
    }
}
