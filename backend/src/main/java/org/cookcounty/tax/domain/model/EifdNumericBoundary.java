package org.cookcounty.tax.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/// Enforces source widths for EIFD identifiers and exact decimal quantities.
final class EifdNumericBoundary {
    private EifdNumericBoundary() {}

    /// Requires an unsigned source identifier with its complete zero-padded width.
    static String identifier(String value, int digits, String name) {
        Objects.requireNonNull(value, name);
        if (!value.matches("[0-9]{" + digits + "}")) {
            throw new IllegalArgumentException(
                    name + " must contain exactly " + digits + " digits");
        }
        return value;
    }

    /// Requires an integral value that fits a signed source field with the given digit count.
    ///
    /// @throws ArithmeticException if the magnitude exceeds the source representation
    static Long signedIntegral(Long value, long maximumMagnitude, String name) {
        Objects.requireNonNull(value, name);
        if (value < -maximumMagnitude || value > maximumMagnitude) {
            throw new ArithmeticException(name + " exceeds its source precision");
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

    /// Requires a nonnegative exact decimal that fits an unsigned source field.
    ///
    /// @throws IllegalArgumentException if the value is negative
    /// @throws ArithmeticException if the value requires rounding or exceeds source precision
    static BigDecimal unsignedFixedPoint(BigDecimal value, int precision, int scale, String name) {
        BigDecimal exact = fixedPoint(value, precision, scale, name);
        if (exact.signum() < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return exact;
    }
}
