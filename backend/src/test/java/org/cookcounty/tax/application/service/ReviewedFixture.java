package org.cookcounty.tax.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/// Reads reviewed fixtures from the test classpath, without an enclosing source checkout.
final class ReviewedFixture {
    private static final String RESOURCE_ROOT = "/reviewed/";

    private ReviewedFixture() {}

    /// Returns the original bytes. A missing packaged fixture fails without a filesystem fallback.
    static byte[] bytes(String sourcePath) throws IOException {
        String resourcePath = RESOURCE_ROOT + sourcePath;
        try (InputStream stream = ReviewedFixture.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException(
                        "Reviewed fixture is not on the test classpath: " + resourcePath);
            }
            return stream.readAllBytes();
        }
    }

    /// Decodes a reviewed text fixture as UTF-8 without normalizing its whitespace.
    static String string(String sourcePath) throws IOException {
        return new String(bytes(sourcePath), StandardCharsets.UTF_8);
    }
}
