package org.cookcounty.tax.infrastructure.config;

import org.cookcounty.tax.application.batch.BatchRunLifecycle;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/// Configures bounded batch execution and durable lease maintenance.
@Configuration(proxyBeanMethods = false)
public class BatchConfig {
    /// Creates the transaction boundary for one tax-increment processing step.
    ///
    /// Each callback commits or rolls back as a unit. A later step failure does not roll back a
    /// callback that already committed.
    ///
    /// @param transactionManager manager for the step's repository writes
    /// @return transaction operations shared by increment processors
    @Bean(name = "eifdTifIncrementTransactions")
    public TransactionOperations eifdTifIncrementTransactions(
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    /// Creates the virtual-thread worker executor shared by all batch capabilities.
    @Bean(name = "batchWorkerExecutor", destroyMethod = "close")
    public ExecutorService batchWorkerExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /// Applies the configured admission limit to this application's workers.
    @Bean(name = "batchRunExecutor")
    public Executor boundedBatchRunExecutor(
            @Value("${app.batch.max-concurrent-runs}") int maximumConcurrentRuns,
            @Qualifier("batchWorkerExecutor") ExecutorService batchWorkerExecutor) {
        return new BoundedBatchExecutor(batchWorkerExecutor, maximumConcurrentRuns);
    }

    /// Supplies dedicated platform threads so worker stalls do not stop lease renewal.
    @Bean(name = "batchLeaseScheduler", destroyMethod = "close")
    public ScheduledExecutorService batchLeaseScheduler() {
        return Executors.newScheduledThreadPool(2);
    }

    /// Creates one durable owner identity for this application process.
    @Bean
    public BatchRunLifecycle batchRunLifecycle(
            BatchRunStore store,
            @Value("${app.batch.run-lease}") Duration leaseDuration,
            @Qualifier("batchLeaseScheduler") ScheduledExecutorService scheduler) {
        return new BatchRunLifecycle(
                store, UUID.randomUUID().toString(), leaseDuration, Clock.systemUTC(), scheduler);
    }
}
