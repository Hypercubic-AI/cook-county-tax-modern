package org.cookcounty.tax.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/// Enforces source widths for property-tax exemption values before persistence.
///
/// Identifier methods preserve leading zeroes through fixed source widths. Decimal methods reject
/// values that require rounding or exceed the source precision.
final class PropertyTaxExemptionsNumericBoundary {
    private PropertyTaxExemptionsNumericBoundary() {}

    /// Returns an unsigned numeric identifier with its source width.
    static String unsignedIdentifier(String value, int width, String name) {
        if (value.isEmpty()
                || !value.chars().allMatch(character -> character >= '0' && character <= '9')) {
            throw new IllegalArgumentException(name + " must contain only decimal digits");
        }
        if (value.length() > width) {
            throw new IllegalArgumentException(name + " exceeds source width " + width);
        }
        return "0".repeat(width - value.length()) + value;
    }

    /// Returns an optional unsigned identifier with its source width.
    static @Nullable String optionalUnsignedIdentifier(
            @Nullable String value, int width, String name) {
        return value == null ? null : unsignedIdentifier(value, width, name);
    }

    /// Returns an exact fixed-point value with the source scale and precision.
    static BigDecimal exact(BigDecimal value, int precision, int scale, String name) {
        BigDecimal exact = value.setScale(scale, RoundingMode.UNNECESSARY);
        if (exact.precision() > precision) {
            throw new ArithmeticException(name + " exceeds source precision " + precision);
        }
        return exact;
    }

    /// Returns an optional exact fixed-point value without changing absence.
    static @Nullable BigDecimal optionalExact(
            @Nullable BigDecimal value, int precision, int scale, String name) {
        return value == null ? null : exact(value, precision, scale, name);
    }
}
