package org.cookcounty.tax.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/// Enforces source widths for assessment identifiers and whole-unit valuations.
final class AssessmentNumericBoundary {
    private AssessmentNumericBoundary() {}

    /// Requires a signed source identifier with an exact zero-padded magnitude width.
    ///
    /// The optional minus sign does not consume a source digit. A positive value has no plus sign.
    static String signedIdentifier(String value, int digits, String name) {
        Objects.requireNonNull(value, name);
        if (!value.matches("-?[0-9]{" + digits + "}")) {
            throw new IllegalArgumentException(
                    name + " must contain " + digits + " digits and an optional minus sign");
        }
        return value;
    }

    /// Requires an exact whole-unit decimal that fits the signed source precision.
    ///
    /// @throws ArithmeticException if the value has a fraction or more source digits
    static BigDecimal wholeUnits(BigDecimal value, int precision, String name) {
        Objects.requireNonNull(value, name);
        BigDecimal exact = value.setScale(0, RoundingMode.UNNECESSARY);
        if (exact.precision() > precision) {
            throw new ArithmeticException(name + " exceeds source precision " + precision);
        }
        return exact;
    }
}
