package org.cookcounty.tax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/// Starts the tax service, including its HTTP endpoints and durable batch infrastructure.
@SpringBootApplication
public class Application {
    /// Starts Spring Boot with the supplied command-line configuration overrides.
    ///
    /// @param args Spring Boot application arguments
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
