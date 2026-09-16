package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.batch.InMemoryBatchRunStore;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunResponse;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

class EifdTifIncrementRunServiceTest {

    @Test
    void identicalStartReplaysCurrentSnapshotWithoutDispatchingAgain() {
        EifdTifIncrementProcessor processor = mock(EifdTifIncrementProcessor.class);
        when(processor.process(org.mockito.ArgumentMatchers.any())).thenReturn(success());
        CapturingExecutor executor = new CapturingExecutor();
        EifdTifIncrementRunService service =
                newService(processor, executor, mock(FactorBatchOutcomeRecorder.class));

        BatchRunStart<EifdTifIncrementRunResponse> first =
                success(service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000")));
        BatchRunStart<EifdTifIncrementRunResponse> replay =
                success(service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000")));

        assertThat(first.replayed()).isFalse();
        assertThat(replay.replayed()).isTrue();
        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.response()).isSameInstanceAs(first.response());
        assertThat(replay.response().status()).isEqualTo("QUEUED");
        assertThat(executor.submissions).isEqualTo(1);

        executor.task().run();
        EifdTifIncrementRunResponse completed = success(service.getEifdTifIncrementRun(first.id()));
        assertThat(completed.status()).isEqualTo("COMPLETED");
        assertThat(completed.returnCode()).isEqualTo(0);
        assertThat(completed.outputs().size()).isEqualTo(3);

        BatchRunStart<EifdTifIncrementRunResponse> completedReplay =
                success(service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000")));
        assertThat(completedReplay.response()).isSameInstanceAs(completed);
        assertThat(executor.submissions).isEqualTo(1);
    }

    @Test
    void sameIdempotencyKeyWithDifferentControlsReturnsConflict() {
        EifdTifIncrementRunService service =
                newService(
                        mock(EifdTifIncrementProcessor.class),
                        new CapturingExecutor(),
                        mock(FactorBatchOutcomeRecorder.class));
        success(service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000")));

        assertThat(
                        failure(
                                service.startEifdTifIncrementRun(
                                        request("run-key", "12:00:00", "10001"))))
                .isInstanceOf(BatchRunFailure.IdempotencyConflict.class);
    }

    @Test
    void workerFailurePublishesTerminalFailedSnapshot() {
        EifdTifIncrementProcessor processor = mock(EifdTifIncrementProcessor.class);
        when(processor.process(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("rule-example worker failure"));
        CapturingExecutor executor = new CapturingExecutor();
        EifdTifIncrementRunService service =
                newService(processor, executor, mock(FactorBatchOutcomeRecorder.class));

        long id =
                success(
                                service.startEifdTifIncrementRun(
                                        request("failed-key", "12:00:00", "10000")))
                        .id();
        executor.task().run();

        EifdTifIncrementRunResponse failed = success(service.getEifdTifIncrementRun(id));
        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.returnCode()).isEqualTo(16);
        assertThat(failed.messages().get(0).severity()).isEqualTo("ERROR");
    }

    @Test
    void malformedDirectControlsAreRejectedAndUnknownLongIdIsAbsent() {
        EifdTifIncrementRunService service =
                newService(
                        mock(EifdTifIncrementProcessor.class),
                        new CapturingExecutor(),
                        mock(FactorBatchOutcomeRecorder.class));

        assertThat(failure(service.startEifdTifIncrementRun(request(" ", "25:00:00", "10000"))))
                .isEqualTo(
                        new BatchRunFailure.InvalidRequest(
                                "businessTime must use 24-hour HH:mm:ss format"));
        assertThat(
                        failure(
                                service.startEifdTifIncrementRun(
                                        request("bad-factor", "12:00:00", "00000"))))
                .isInstanceOf(BatchRunFailure.InvalidRequest.class);
        assertThat(failure(service.getEifdTifIncrementRun(Long.MAX_VALUE)))
                .isEqualTo(new BatchRunFailure.RunNotFound(Long.MAX_VALUE));
    }

    @Test
    void admissionRejectionLeavesReplayVisibleFailedSnapshot() {
        Executor rejecting =
                command -> {
                    throw new java.util.concurrent.RejectedExecutionException("full");
                };
        EifdTifIncrementRunService service =
                newService(
                        mock(EifdTifIncrementProcessor.class),
                        rejecting,
                        mock(FactorBatchOutcomeRecorder.class));
        EifdTifIncrementRunRequest request = request("rejected-key", "12:00:00", "10000");

        assertThat(failure(service.startEifdTifIncrementRun(request)))
                .isInstanceOf(BatchRunFailure.AdmissionRejected.class);
        BatchRunStart<EifdTifIncrementRunResponse> replay =
                success(service.startEifdTifIncrementRun(request));

        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("FAILED");
        assertThat(replay.response().messages().get(0).severity()).isEqualTo("ERROR");
    }

    private static EifdTifIncrementRunService newService(
            EifdTifIncrementProcessor processor,
            Executor executor,
            FactorBatchOutcomeRecorder recorder) {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        return new EifdTifIncrementRunService(
                processor,
                new org.cookcounty.tax.application.batch.EifdTifIncrementKernel(),
                new EifdTifIncrementOutcomeProjector(),
                executor,
                recorder,
                store,
                InMemoryBatchRunStore.lifecycle(store));
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

    private EifdTifIncrementProcessResult success() {
        return new EifdTifIncrementProcessResult(
                true,
                0,
                10,
                206,
                19,
                0,
                List.of(
                        new EifdTifIncrementProcessResult.Output("agencySummaryReport", 14, null),
                        new EifdTifIncrementProcessResult.Output(
                                "townWithinAgencyReport", 75, null),
                        new EifdTifIncrementProcessResult.Output(
                                "agencyYearAppendRecords", 117, null)),
                List.of(
                        new EifdTifIncrementProcessResult.Message(
                                "INFO", "EIFD/TIF increment batch completed successfully.", null)));
    }

    private EifdTifIncrementRunRequest request(String key, String time, String factor) {
        return new EifdTifIncrementRunRequest(
                factor, LocalDate.of(2025, 9, 15), time, key, "26", "260181202526", "2026");
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
