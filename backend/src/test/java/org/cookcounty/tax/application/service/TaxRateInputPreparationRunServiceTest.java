package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.application.batch.InMemoryBatchRunStore;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Input;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Operation;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunResponse;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

class TaxRateInputPreparationRunServiceTest {

    @Test
    void identicalStartReplaysCurrentSnapshotWithoutDispatchingAgain() {
        TaxRateInputProcessor processor = mock(TaxRateInputProcessor.class);
        when(processor.process()).thenReturn(emptyResult());
        CapturingExecutor executor = new CapturingExecutor();
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        TaxRateInputPreparationRunService service =
                newService(processor, executor, recorder, new TaxRateInputFactorOutcomeProjector());
        TaxRateInputPreparationRunRequest request = request("run-key", "12:00:00");

        BatchRunStart<TaxRateInputPreparationRunResponse> first =
                success(service.startTaxRateInputPreparationRun(request));
        BatchRunStart<TaxRateInputPreparationRunResponse> replay =
                success(service.startTaxRateInputPreparationRun(request("run-key", "12:00:00")));

        assertThat(first.replayed()).isFalse();
        assertThat(replay.replayed()).isTrue();
        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.response()).isSameInstanceAs(first.response());
        assertThat(replay.response().status()).isEqualTo("QUEUED");
        assertThat(executor.submissions).isEqualTo(1);

        executor.task().run();
        TaxRateInputPreparationRunResponse completed =
                success(service.getTaxRateInputPreparationRun(first.id()));
        assertThat(completed.status()).isEqualTo("COMPLETED");
        assertThat(completed.returnCode()).isEqualTo(0);
        assertThat(completed.outputs()).hasSize(5);
        verify(recorder)
                .completed(eq("clerk-agency-attachment"), eq(first.id()), eq("COMPLETED"), any());

        BatchRunStart<TaxRateInputPreparationRunResponse> completedReplay =
                success(service.startTaxRateInputPreparationRun(request("run-key", "12:00:00")));
        assertThat(completedReplay.response()).isSameInstanceAs(completed);
        assertThat(executor.submissions).isEqualTo(1);
    }

    @Test
    void sameIdempotencyKeyWithDifferentControlsReturnsConflict() {
        TaxRateInputPreparationRunService service =
                newService(
                        mock(TaxRateInputProcessor.class),
                        new CapturingExecutor(),
                        mock(FactorBatchOutcomeRecorder.class),
                        new TaxRateInputFactorOutcomeProjector());
        success(service.startTaxRateInputPreparationRun(request("run-key", "12:00:00")));

        assertThat(failure(service.startTaxRateInputPreparationRun(request("run-key", "12:00:01"))))
                .isInstanceOf(BatchRunFailure.IdempotencyConflict.class);
    }

    @Test
    void admissionRejectionPersistsFailedSnapshotForExactReplay() {
        TaxRateInputPreparationRunService service =
                newService(
                        mock(TaxRateInputProcessor.class),
                        command -> {
                            throw new RejectedExecutionException("synthetic capacity rejection");
                        },
                        mock(FactorBatchOutcomeRecorder.class),
                        new TaxRateInputFactorOutcomeProjector());
        TaxRateInputPreparationRunRequest request = request("rejected-key", "12:00:00");

        assertThat(failure(service.startTaxRateInputPreparationRun(request)))
                .isInstanceOf(BatchRunFailure.AdmissionRejected.class);
        BatchRunStart<TaxRateInputPreparationRunResponse> replay =
                success(service.startTaxRateInputPreparationRun(request));

        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("FAILED");
        assertThat(replay.response().returnCode()).isEqualTo(16);
        assertThat(replay.response().recordsRead()).isEqualTo(0);
        assertThat(replay.response().messages().get(0).code())
                .isEqualTo("BATCH_CAPACITY_EXHAUSTED");
    }

    @Test
    void workerFailurePublishesTerminalFailedSnapshot() {
        TaxRateInputProcessor processor = mock(TaxRateInputProcessor.class);
        when(processor.process()).thenThrow(new IllegalStateException("synthetic worker failure"));
        CapturingExecutor executor = new CapturingExecutor();
        TaxRateInputPreparationRunService service =
                newService(
                        processor,
                        executor,
                        mock(FactorBatchOutcomeRecorder.class),
                        new TaxRateInputFactorOutcomeProjector());

        long id =
                success(service.startTaxRateInputPreparationRun(request("failed-key", "12:00:00")))
                        .id();
        executor.task().run();

        TaxRateInputPreparationRunResponse failed =
                success(service.getTaxRateInputPreparationRun(id));
        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.returnCode()).isEqualTo(16);
        assertThat(failed.messages().get(0).code()).isEqualTo("WORKER_FAILURE");
    }

    @Test
    void malformedDirectRequestIsRejectedAndUnknownLongIdIsAbsent() {
        TaxRateInputPreparationRunService service =
                newService(
                        mock(TaxRateInputProcessor.class),
                        new CapturingExecutor(),
                        mock(FactorBatchOutcomeRecorder.class),
                        new TaxRateInputFactorOutcomeProjector());
        TaxRateInputPreparationRunRequest malformed = request(" ", "25:00:00");

        assertThat(failure(service.startTaxRateInputPreparationRun(malformed)))
                .isEqualTo(
                        new BatchRunFailure.InvalidRequest(
                                "businessTime must use 24-hour HH:mm:ss format"));
        assertThat(failure(service.getTaxRateInputPreparationRun(Long.MAX_VALUE)))
                .isEqualTo(new BatchRunFailure.RunNotFound(Long.MAX_VALUE));
    }

    private static TaxRateInputPreparationRunService newService(
            TaxRateInputProcessor processor,
            Executor executor,
            FactorBatchOutcomeRecorder recorder,
            TaxRateInputFactorOutcomeProjector projector) {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        return new TaxRateInputPreparationRunService(
                processor,
                executor,
                recorder,
                projector,
                store,
                InMemoryBatchRunStore.lifecycle(store));
    }

    private static <T> T success(
            org.cookcounty.tax.domain.contract.Result<T, BatchRunFailure> result) {
        return switch (result) {
            case org.cookcounty.tax.domain.contract.Result.Ok<T, BatchRunFailure>(var value) ->
                    value;
            case org.cookcounty.tax.domain.contract.Result.Err<T, BatchRunFailure>(var error) ->
                    throw new AssertionError("Expected success but got " + error);
        };
    }

    private static <T> BatchRunFailure failure(
            org.cookcounty.tax.domain.contract.Result<T, BatchRunFailure> result) {
        return switch (result) {
            case org.cookcounty.tax.domain.contract.Result.Ok<T, BatchRunFailure>(var value) ->
                    throw new AssertionError("Expected failure but got " + value);
            case org.cookcounty.tax.domain.contract.Result.Err<T, BatchRunFailure>(var error) ->
                    error;
        };
    }

    private static Result emptyResult() {
        return new TaxRateInputKernel()
                .process(
                        new Input(List.of(), List.of(), Map.of(), List.of()),
                        (taxCode, agency, value, annex) -> Operation.INSERT);
    }

    private static TaxRateInputPreparationRunRequest request(String key, String time) {
        return new TaxRateInputPreparationRunRequest(LocalDate.of(2025, 9, 15), time, key);
    }

    private static final class CapturingExecutor implements Executor {
        private @Nullable Runnable task;
        private int submissions;

        @Override
        public void execute(Runnable command) {
            task = command;
            submissions++;
        }

        Runnable task() {
            return requireNonNull(task, "task was not submitted");
        }
    }
}
