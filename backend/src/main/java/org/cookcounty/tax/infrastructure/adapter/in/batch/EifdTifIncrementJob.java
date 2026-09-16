package org.cookcounty.tax.infrastructure.adapter.in.batch;

import java.time.LocalDate;

import org.cookcounty.tax.application.service.EifdTifIncrementProcessor;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EifdTifIncrementJob {

    @Bean
    public Job eifd_tif_incrementJob(
            JobRepository jobRepository,
            Step eifd_tif_incrementStep) {
        return new JobBuilder("eifd_tif_increment", jobRepository)
                .start(eifd_tif_incrementStep)
                .build();
    }

    @Bean
    public Step eifd_tif_incrementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            EifdTifIncrementProcessor processor) {
        return new StepBuilder("eifd_tif_incrementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> execute(
                        contribution.getStepExecution()
                                .getJobExecution().getJobParameters(),
                        processor), transactionManager)
                .build();
    }

    RepeatStatus execute(JobParameters parameters, EifdTifIncrementProcessor processor) {
        EifdTifIncrementRunRequest request = new EifdTifIncrementRunRequest();
        request.setBusinessDate(LocalDate.parse(parameters.getString("businessDate")));
        request.setBusinessTime(parameters.getString("businessTime"));
        request.setIdempotencyKey(parameters.getString("idempotencyKey"));
        request.setReassessmentControl(parameters.getString("reassessmentControl"));
        request.setProcessingYear(parameters.getString("processingYear"));
        request.setReportingYear(parameters.getString("reportingYear"));
        request.setAnnualEqualizationFactor(parameters.getString("annualEqualizationFactor"));
        processor.process(request);
        return RepeatStatus.FINISHED;
    }
}
