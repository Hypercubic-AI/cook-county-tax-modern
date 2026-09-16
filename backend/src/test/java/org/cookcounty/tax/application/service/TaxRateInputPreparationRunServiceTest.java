package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Input;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Operation;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunResponse;
import org.junit.jupiter.api.Test;

class TaxRateInputPreparationRunServiceTest {

    @Test
    void identicalStartReplaysCurrentSnapshotWithoutDispatchingAgain() {
        TaxRateInputProcessor processor = mock(TaxRateInputProcessor.class);
        when(processor.process()).thenReturn(emptyResult());
        CapturingExecutor executor = new CapturingExecutor();
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        TaxRateInputPreparationRunService service = new TaxRateInputPreparationRunService(
                processor, executor, recorder, new TaxRateInputFactorOutcomeProjector());
        TaxRateInputPreparationRunRequest request = request("run-key", "12:00:00");

        BatchRunStartResult<TaxRateInputPreparationRunResponse> first =
                service.startTaxRateInputPreparationRun(request);
        BatchRunStartResult<TaxRateInputPreparationRunResponse> replay =
                service.startTaxRateInputPreparationRun(request("run-key", "12:00:00"));

        assertFalse(first.replayed());
        assertTrue(replay.replayed());
        assertEquals(first.id(), replay.id());
        assertSame(first.response(), replay.response());
        assertEquals("QUEUED", replay.response().getStatus());
        assertEquals(1, executor.submissions);

        executor.task.run();
        TaxRateInputPreparationRunResponse completed =
                service.getTaxRateInputPreparationRun(first.id()).orElseThrow();
        assertEquals("COMPLETED", completed.getStatus());
        assertEquals(0, completed.getReturnCode());
        assertEquals(5, completed.getOutputs().size());
        verify(recorder).completed(
                eq("clerk-agency-attachment"), eq(first.id()), eq("COMPLETED"), any());

        BatchRunStartResult<TaxRateInputPreparationRunResponse> completedReplay =
                service.startTaxRateInputPreparationRun(request("run-key", "12:00:00"));
        assertSame(completed, completedReplay.response());
        assertEquals(1, executor.submissions);
    }

    @Test
    void sameIdempotencyKeyWithDifferentControlsReturnsConflict() {
        TaxRateInputPreparationRunService service = new TaxRateInputPreparationRunService(
                mock(TaxRateInputProcessor.class),
                new CapturingExecutor(),
                mock(FactorBatchOutcomeRecorder.class),
                new TaxRateInputFactorOutcomeProjector());
        service.startTaxRateInputPreparationRun(request("run-key", "12:00:00"));

        assertThrows(
                BatchRunIdempotencyConflictException.class,
                () -> service.startTaxRateInputPreparationRun(
                        request("run-key", "12:00:01")));
    }

    @Test
    void workerFailurePublishesTerminalFailedSnapshot() {
        TaxRateInputProcessor processor = mock(TaxRateInputProcessor.class);
        when(processor.process()).thenThrow(new IllegalStateException("synthetic worker failure"));
        CapturingExecutor executor = new CapturingExecutor();
        TaxRateInputPreparationRunService service = new TaxRateInputPreparationRunService(
                processor,
                executor,
                mock(FactorBatchOutcomeRecorder.class),
                new TaxRateInputFactorOutcomeProjector());

        long id = service.startTaxRateInputPreparationRun(request("failed-key", "12:00:00")).id();
        executor.task.run();

        TaxRateInputPreparationRunResponse failed =
                service.getTaxRateInputPreparationRun(id).orElseThrow();
        assertEquals("FAILED", failed.getStatus());
        assertEquals(16, failed.getReturnCode());
        assertEquals("WORKER_FAILURE", failed.getMessages().get(0).getCode());
    }

    @Test
    void malformedDirectRequestIsRejectedAndUnknownLongIdIsAbsent() {
        TaxRateInputPreparationRunService service = new TaxRateInputPreparationRunService(
                mock(TaxRateInputProcessor.class),
                new CapturingExecutor(),
                mock(FactorBatchOutcomeRecorder.class),
                new TaxRateInputFactorOutcomeProjector());
        TaxRateInputPreparationRunRequest malformed = request(" ", "25:00:00");

        assertThrows(
                BatchRunInvalidRequestException.class,
                () -> service.startTaxRateInputPreparationRun(malformed));
        assertTrue(service.getTaxRateInputPreparationRun(Long.MAX_VALUE).isEmpty());
    }

    private static Result emptyResult() {
        return new TaxRateInputKernel().process(
                new Input(List.of(), List.of(), Map.of(), List.of()),
                (taxCode, agency, value, annex) -> Operation.INSERT);
    }

    private static TaxRateInputPreparationRunRequest request(String key, String time) {
        TaxRateInputPreparationRunRequest request = new TaxRateInputPreparationRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime(time);
        request.setIdempotencyKey(key);
        return request;
    }

    private static final class CapturingExecutor implements Executor {
        private Runnable task;
        private int submissions;

        @Override
        public void execute(Runnable command) {
            task = command;
            submissions++;
        }
    }
}
