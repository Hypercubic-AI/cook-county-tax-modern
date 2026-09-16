package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunResponse;
import org.junit.jupiter.api.Test;

class EifdTifIncrementRunServiceTest {

    @Test
    void identicalStartReplaysCurrentSnapshotWithoutDispatchingAgain() {
        EifdTifIncrementProcessor processor = mock(EifdTifIncrementProcessor.class);
        when(processor.process(org.mockito.ArgumentMatchers.any())).thenReturn(success());
        CapturingExecutor executor = new CapturingExecutor();
        EifdTifIncrementRunService service = new EifdTifIncrementRunService(
                processor, executor, mock(FactorBatchOutcomeRecorder.class));

        BatchRunStartResult<EifdTifIncrementRunResponse> first =
                service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000"));
        BatchRunStartResult<EifdTifIncrementRunResponse> replay =
                service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000"));

        assertFalse(first.replayed());
        assertTrue(replay.replayed());
        assertEquals(first.id(), replay.id());
        assertSame(first.response(), replay.response());
        assertEquals("QUEUED", replay.response().getStatus());
        assertEquals(1, executor.submissions);

        executor.task.run();
        EifdTifIncrementRunResponse completed =
                service.getEifdTifIncrementRun(first.id()).orElseThrow();
        assertEquals("COMPLETED", completed.getStatus());
        assertEquals(0, completed.getReturnCode());
        assertEquals(3, completed.getOutputs().size());

        BatchRunStartResult<EifdTifIncrementRunResponse> completedReplay =
                service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000"));
        assertSame(completed, completedReplay.response());
        assertEquals(1, executor.submissions);
    }

    @Test
    void sameIdempotencyKeyWithDifferentControlsReturnsConflict() {
        EifdTifIncrementRunService service = new EifdTifIncrementRunService(
                mock(EifdTifIncrementProcessor.class), new CapturingExecutor(),
                mock(FactorBatchOutcomeRecorder.class));
        service.startEifdTifIncrementRun(request("run-key", "12:00:00", "10000"));

        assertThrows(BatchRunIdempotencyConflictException.class,
                () -> service.startEifdTifIncrementRun(
                        request("run-key", "12:00:00", "10001")));
    }

    @Test
    void workerFailurePublishesTerminalFailedSnapshot() {
        EifdTifIncrementProcessor processor = mock(EifdTifIncrementProcessor.class);
        when(processor.process(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("rule-example worker failure"));
        CapturingExecutor executor = new CapturingExecutor();
        EifdTifIncrementRunService service = new EifdTifIncrementRunService(
                processor, executor, mock(FactorBatchOutcomeRecorder.class));

        long id = service.startEifdTifIncrementRun(
                request("failed-key", "12:00:00", "10000")).id();
        executor.task.run();

        EifdTifIncrementRunResponse failed =
                service.getEifdTifIncrementRun(id).orElseThrow();
        assertEquals("FAILED", failed.getStatus());
        assertEquals(16, failed.getReturnCode());
        assertEquals("EIFD/TIF increment batch failed.", failed.getMessages().get(0).getText());
    }

    @Test
    void malformedDirectControlsAreRejectedAndUnknownLongIdIsAbsent() {
        EifdTifIncrementRunService service = new EifdTifIncrementRunService(
                mock(EifdTifIncrementProcessor.class), new CapturingExecutor(),
                mock(FactorBatchOutcomeRecorder.class));

        assertThrows(BatchRunInvalidRequestException.class,
                () -> service.startEifdTifIncrementRun(
                        request(" ", "25:00:00", "10000")));
        assertThrows(BatchRunInvalidRequestException.class,
                () -> service.startEifdTifIncrementRun(
                        request("bad-factor", "12:00:00", "00000")));
        assertTrue(service.getEifdTifIncrementRun(Long.MAX_VALUE).isEmpty());
    }

    private EifdTifIncrementProcessResult success() {
        return new EifdTifIncrementProcessResult(true, 0, 10, 206, 19, 0,
                List.of(
                        new EifdTifIncrementProcessResult.Output("agencySummaryReport", 14, null),
                        new EifdTifIncrementProcessResult.Output("townWithinAgencyReport", 75, null),
                        new EifdTifIncrementProcessResult.Output("agencyYearAppendRecords", 117, null)),
                List.of(new EifdTifIncrementProcessResult.Message(
                        "INFO", "EIFD/TIF increment batch completed successfully.", null)));
    }

    private EifdTifIncrementRunRequest request(String key, String time, String factor) {
        EifdTifIncrementRunRequest request = new EifdTifIncrementRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime(time);
        request.setIdempotencyKey(key);
        request.setReassessmentControl("260181202526");
        request.setProcessingYear("26");
        request.setReportingYear("2026");
        request.setAnnualEqualizationFactor(factor);
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
