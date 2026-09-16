package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.OutputRecord;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessedValuePreparationRunServiceTest {

    @Mock
    private AssessedValuePreparationProcessor processor;

    @Mock
    private AssessedValuePreparationFactorOutcomeProjector outcomeProjector;

    @Mock
    private FactorBatchOutcomeRecorder outcomeRecorder;

    private AssessedValuePreparationRunService service;

    @BeforeEach
    void setUp() {
        service = new AssessedValuePreparationRunService(
                processor, outcomeProjector, outcomeRecorder, Runnable::run);
    }

    @Test
    void startPublishesQueuedThenCompletedWholeSnapshotsWithLongId() {
        ProcessResult result = success();
        Outcome projected = new Outcome(
                0, List.of(), Map.of(), List.of(), Map.of(), List.of(),
                null, false, false);
        when(processor.process(LocalDate.of(2025, 9, 15), "12:00:00", "26"))
                .thenReturn(result);
        when(outcomeProjector.project(result)).thenReturn(projected);

        AssessedValuePreparationRunResponse accepted = service.startAssessedValuePreparationRun(
                request("run-1", "26"));
        AssessedValuePreparationRunResponse completed = service
                .getAssessedValuePreparationRun(accepted.getId()).orElseThrow();

        assertEquals("QUEUED", accepted.getStatus());
        assertTrue(accepted.getId() > Integer.MAX_VALUE);
        assertEquals("COMPLETED", completed.getStatus());
        assertEquals(30, completed.getRecordsRead());
        assertEquals(List.of(), completed.getOutputs());
        verify(outcomeRecorder).completed(
                "valuation-preparation", accepted.getId(), "COMPLETED", projected);
    }

    @Test
    void sameIdempotencyKeyAndControlsReplayCurrentRunWithoutRedispatch() {
        when(processor.process(any(), any(), any())).thenReturn(success());

        AssessedValuePreparationRunResponse first = service.startAssessedValuePreparationRun(
                request("same-key", "26"));
        AssessedValuePreparationRunResponse replay = service.startAssessedValuePreparationRun(
                request("same-key", "26"));

        assertEquals(first.getId(), replay.getId());
        assertEquals("COMPLETED", replay.getStatus());
        verify(processor).process(LocalDate.of(2025, 9, 15), "12:00:00", "26");
    }

    @Test
    void sameIdempotencyKeyWithDifferentControlsConflicts() {
        when(processor.process(any(), any(), any())).thenReturn(success());
        service.startAssessedValuePreparationRun(request("same-key", "26"));

        assertThrows(
                BatchRunIdempotencyConflictException.class,
                () -> service.startAssessedValuePreparationRun(request("same-key", "27")));
    }

    @Test
    void invalidProcessYearLengthAndCharactersAreRejectedBeforeDispatch() {
        for (String invalid : List.of("2", "260", "2A")) {
            assertThrows(
                    BatchRunInvalidRequestException.class,
                    () -> service.startAssessedValuePreparationRun(
                            request("run-invalid-" + invalid, invalid)));
        }
    }

    @Test
    void workerExceptionPublishesFailedTerminalSnapshot() {
        doThrow(new IllegalStateException("database unavailable"))
                .when(processor).process(any(), any(), any());

        AssessedValuePreparationRunResponse accepted = service.startAssessedValuePreparationRun(
                request("run-fails", "26"));
        AssessedValuePreparationRunResponse failed = service
                .getAssessedValuePreparationRun(accepted.getId()).orElseThrow();

        assertEquals("FAILED", failed.getStatus());
        assertEquals(16, failed.getReturnCode());
        assertEquals("WORKER_FAILED", failed.getMessages().get(0).getCode());
    }

    @Test
    void semanticOrderingFailureRemainsFailedWhenLegacyReturnCodeIsZeroAndMarksEveryOutputPartial() {
        StageOutcome failedStage = new StageOutcome(
                "VALUATION_BUCKETING", "FAILED", 0, 2, 1, 1, 1, true, true, 3);
        when(processor.process(any(), any(), any())).thenReturn(new ProcessResult(
                true, 0, 2, 1, 1, 1, true,
                List.of(new OutputRecord("VALUATION_BREAKDOWN_REPORT", "text/plain", 3)),
                List.of(), List.of(failedStage)));

        AssessedValuePreparationRunResponse accepted = service.startAssessedValuePreparationRun(
                request("semantic-failure", "26"));
        AssessedValuePreparationRunResponse failed = service
                .getAssessedValuePreparationRun(accepted.getId()).orElseThrow();

        assertEquals("FAILED", failed.getStatus());
        assertEquals(0, failed.getReturnCode());
        assertTrue(failed.getPartialOutput());
        assertTrue(failed.getOutputs().get(0).getPartial());
        assertTrue(failed.getStages().get(0).getPartialOutput());
    }

    private static ProcessResult success() {
        return new ProcessResult(
                false, 0, 30, 30, 10, 0, false,
                List.of(), List.of(), List.of());
    }

    private static AssessedValuePreparationRunRequest request(String key, String processYear) {
        AssessedValuePreparationRunRequest request = new AssessedValuePreparationRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime("12:00:00");
        request.setIdempotencyKey(key);
        request.setProcessYear(processYear);
        return request;
    }
}
