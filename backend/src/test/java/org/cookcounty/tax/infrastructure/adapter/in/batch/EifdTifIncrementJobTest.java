package org.cookcounty.tax.infrastructure.adapter.in.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.cookcounty.tax.application.service.EifdTifIncrementProcessor;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

class EifdTifIncrementJobTest {

    @Test
    void stepDelegatesItsPinnedParametersToTheSharedProcessor() {
        EifdTifIncrementProcessor processor = mock(EifdTifIncrementProcessor.class);
        var parameters = new JobParametersBuilder()
                .addString("businessDate", "2026-01-01")
                .addString("businessTime", "08:00:00")
                .addString("idempotencyKey", "batch-job")
                .addString("reassessmentControl", "260181202526")
                .addString("processingYear", "26")
                .addString("reportingYear", "2026")
                .addString("annualEqualizationFactor", "10000")
                .toJobParameters();

        RepeatStatus status = new EifdTifIncrementJob().execute(parameters, processor);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<EifdTifIncrementRunRequest> request =
                ArgumentCaptor.forClass(EifdTifIncrementRunRequest.class);
        verify(processor).process(request.capture());
        assertEquals("260181202526", request.getValue().getReassessmentControl());
        assertEquals("26", request.getValue().getProcessingYear());
        assertEquals("10000", request.getValue().getAnnualEqualizationFactor());
    }
}
