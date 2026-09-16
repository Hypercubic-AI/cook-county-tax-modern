package org.cookcounty.tax.infrastructure.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Configuration(proxyBeanMethods = false)
public class BatchConfig {

    @Bean(name = "batchRunExecutor", destroyMethod = "shutdown")
    @Primary
    public ExecutorService batchRunExecutor() {
        return Executors.newFixedThreadPool(
                4, new CustomizableThreadFactory("batch-run-"));
    }
}
