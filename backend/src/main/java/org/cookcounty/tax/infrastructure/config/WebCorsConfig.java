package org.cookcounty.tax.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/// Applies the configured operator-UI origins to the batch API.
///
/// Production must supply its allowed origins explicitly. Development origins belong to the
/// development profile, not to a fallback embedded in application code.
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /// Browser preflight results may be cached for one hour.
    private static final long PREFLIGHT_CACHE_SECONDS = 3_600;

    /// Private copy of the environment-specific origin allowlist.
    private final String[] allowedOrigins;

    /// Copies the configured origins so the caller cannot later change the allowlist.
    ///
    /// @param allowedOrigins verified origins for the selected deployment profile
    public WebCorsConfig(@Value("${app.web.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins.clone();
    }

    /// Allows the UI's read and launch requests without enabling unrelated methods or paths.
    ///
    /// @param registry Spring MVC registry that owns the resulting CORS configuration
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type")
                .maxAge(PREFLIGHT_CACHE_SECONDS);
    }
}
