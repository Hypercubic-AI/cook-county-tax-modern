package org.cookcounty.tax.infrastructure.adapter.out.persistence;

import jakarta.persistence.EntityManager;

import org.cookcounty.tax.Application;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.DriverManager;
import java.util.UUID;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class PostgreSqlIntegrationTestSupport {
    private static final String SCHEMA = "quality_" + UUID.randomUUID().toString().replace("-", "");
    private static @Nullable PostgreSQLContainer<?> database;

    @Autowired protected EntityManager entityManager;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        String externalUrl = System.getenv("TEST_DATABASE_URL");
        if (externalUrl == null) {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17");
            container.start();
            database = container;
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.username", container::getUsername);
            registry.add("spring.datasource.password", container::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> externalUrl);
            registry.add(
                    "spring.datasource.username",
                    () -> requiredEnvironment("TEST_DATABASE_USERNAME"));
            registry.add(
                    "spring.datasource.password",
                    () -> requiredEnvironment("TEST_DATABASE_PASSWORD"));
        }
        registry.add("spring.flyway.default-schema", () -> SCHEMA);
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
    }

    protected final void clearPersistenceContext() {
        entityManager.clear();
    }

    @AfterAll
    static void releaseDatabase() throws Exception {
        if (database != null) {
            database.stop();
            database = null;
            return;
        }
        try (var connection =
                        DriverManager.getConnection(
                                requiredEnvironment("TEST_DATABASE_URL"),
                                requiredEnvironment("TEST_DATABASE_USERNAME"),
                                requiredEnvironment("TEST_DATABASE_PASSWORD"));
                var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA \"" + SCHEMA + "\" CASCADE");
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null) {
            throw new IllegalStateException(name + " must be set with TEST_DATABASE_URL");
        }
        return value;
    }
}
