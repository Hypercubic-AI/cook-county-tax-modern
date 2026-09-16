package org.cookcounty.tax.infrastructure.adapter.in.batch;

import java.time.LocalDate;
import java.util.Map;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PropertyTaxExemptionProcessingJob {

    @Bean
    public Job property_tax_exemption_processingJob(
            JobRepository jobRepository,
            Step property_tax_exemption_processingStep) {
        return new JobBuilder("property_tax_exemption_processing", jobRepository)
                .start(property_tax_exemption_processingStep)
                .build();
    }

    @Bean
    public Step property_tax_exemption_processingStep(
            JobRepository jobRepository,
            PlatformTransactionManager tx,
            PropertyTaxExemptionsProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
            LocalDate businessDate = LocalDate.parse(required(parameters, "businessDate"));
            String businessTime = required(parameters, "businessTime");
            HomeownerVariant variant = HomeownerVariant.valueOf(
                    required(parameters, "homeownerProcessingVariant"));
            processor.process(businessDate, businessTime, variant);
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("property_tax_exemption_processingStep", jobRepository)
                .tasklet(tasklet, tx)
                .build();
    }

    private static String required(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required job parameter: " + name);
        }
        return value.toString();
    }
}
