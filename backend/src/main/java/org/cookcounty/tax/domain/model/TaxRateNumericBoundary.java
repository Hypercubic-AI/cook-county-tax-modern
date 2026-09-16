package org.cookcounty.tax.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/// Enforces source widths for tax-rate input identifiers and exact decimal quantities.
final class TaxRateNumericBoundary {
    private TaxRateNumericBoundary() {}

    /// Requires an unsigned source identifier with its complete zero-padded width.
    static String identifier(String value, int digits, String name) {
        Objects.requireNonNull(value, name);
        if (!value.matches("[0-9]{" + digits + "}")) {
            throw new IllegalArgumentException(
                    name + " must contain exactly " + digits + " digits");
        }
        return value;
    }

    /// Requires an exact decimal that fits the source precision and scale without rounding.
    ///
    /// @throws ArithmeticException if the value requires rounding or exceeds source precision
    static BigDecimal fixedPoint(BigDecimal value, int precision, int scale, String name) {
        Objects.requireNonNull(value, name);
        BigDecimal exact = value.setScale(scale, RoundingMode.UNNECESSARY);
        if (exact.precision() > precision) {
            throw new ArithmeticException(name + " exceeds source precision " + precision);
        }
        return exact;
    }
}
