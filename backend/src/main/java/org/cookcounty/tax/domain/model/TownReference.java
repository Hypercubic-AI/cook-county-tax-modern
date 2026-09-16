package org.cookcounty.tax.domain.model;

import java.util.Objects;

/// Immutable reporting name for one Cook County town code.
///
/// @param townNumber two-character code, including a leading zero when present
/// @param name name printed beside the original town code
public record TownReference(String townNumber, String name) {
    /// Requires the fixed-width town identity and its available report name.
    public TownReference {
        Objects.requireNonNull(townNumber, "townNumber");
        Objects.requireNonNull(name, "name");
        if (townNumber.length() != 2 || !townNumber.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("townNumber must contain exactly two digits");
        }
        if (name.length() > 13) {
            throw new IllegalArgumentException("name must not exceed 13 characters");
        }
    }
}
