
package org.cookcounty.tax.infrastructure.adapter.in.batch;
import java.time.LocalDate;
import java.util.Map;

import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor;

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
public class AssessedValuePreparationJob {

    @Bean
    public Job assessed_value_preparationJob(JobRepository jobRepository, Step assessed_value_preparationStep) {
        return new JobBuilder("assessed_value_preparation", jobRepository)
            .start(assessed_value_preparationStep)
            .build();
    }

    @Bean
    public Step assessed_value_preparationStep(
            JobRepository jobRepository,
            PlatformTransactionManager tx,
            AssessedValuePreparationProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
            LocalDate businessDate = LocalDate.parse(required(parameters, "businessDate"));
            String businessTime = required(parameters, "businessTime");
            String processYear = required(parameters, "processYear");
            processor.process(businessDate, businessTime, processYear);
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("assessed_value_preparationStep", jobRepository)
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
