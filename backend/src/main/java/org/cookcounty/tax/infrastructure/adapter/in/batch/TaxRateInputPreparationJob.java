
package org.cookcounty.tax.infrastructure.adapter.in.batch;

import org.cookcounty.tax.application.service.TaxRateInputProcessor;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Configuration
public class TaxRateInputPreparationJob {

    @Bean
    public Job tax_rate_input_preparationJob(
            JobRepository jobRepository, Step tax_rate_input_preparationStep) {
        return new JobBuilder("tax_rate_input_preparation", jobRepository)
                .start(tax_rate_input_preparationStep)
                .build();
    }

    @Bean
    public Step tax_rate_input_preparationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TaxRateInputProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            var result = processor.process();
            if (result.returnCode() != 0) {
                throw new IllegalStateException(
                        "Tax-rate input preparation ended with return code "
                                + result.returnCode());
            }
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("tax_rate_input_preparationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .transactionAttribute(new DefaultTransactionAttribute(
                        TransactionDefinition.PROPAGATION_NOT_SUPPORTED))
                .build();
    }
}
