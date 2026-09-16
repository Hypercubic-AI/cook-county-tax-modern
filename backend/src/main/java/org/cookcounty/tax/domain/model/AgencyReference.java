package org.cookcounty.tax.domain.model;

import java.util.Objects;

/// Immutable reporting description for one agency.
///
/// @param agencyNumber nine-character identifier, including leading zeroes
/// @param description name printed when an agency group is published
public record AgencyReference(String agencyNumber, String description) {
    /// Requires the fixed-width agency identity and its available report description.
    public AgencyReference {
        requireWidth(agencyNumber, 9, "agencyNumber");
        Objects.requireNonNull(description, "description");
        if (description.length() > 44) {
            throw new IllegalArgumentException("description must not exceed 44 characters");
        }
    }

    /// Enforces a decimal fixed-width identifier without converting it to a quantity.
    private static void requireWidth(String value, int width, String name) {
        Objects.requireNonNull(value, name);
        if (value.length() != width || !value.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(name + " must contain exactly " + width + " digits");
        }
    }
}
