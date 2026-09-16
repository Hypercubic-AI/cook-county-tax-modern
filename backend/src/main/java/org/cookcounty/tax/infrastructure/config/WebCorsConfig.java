package org.cookcounty.tax.infrastructure.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Allows the separately served operator UI to invoke the generated batch API. */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebCorsConfig(@Value("${app.web.allowed-origins:"
            + "http://localhost:5173,http://127.0.0.1:5173,"
            + "http://localhost:4173,http://127.0.0.1:4173}") String[] allowedOrigins) {
        this.allowedOrigins = Arrays.copyOf(allowedOrigins, allowedOrigins.length);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type")
                .maxAge(3600);
    }
}
