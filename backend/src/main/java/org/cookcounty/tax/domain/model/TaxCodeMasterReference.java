package org.cookcounty.tax.domain.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/// Immutable tax rate and ordered agency assignments for one tax code.
///
/// The source owns 40 positions. The persisted list can omit unassigned positions. Processing
/// restores each omitted position as zero, examines positions 1 through 40, and emits only positive
/// agency numbers.
///
/// @param taxCode five-character identifier, including leading zeroes
/// @param taxRate exact percentage rate used to calculate frozen tax
/// @param agencySlots assigned source positions in ascending position order
public record TaxCodeMasterReference(
        String taxCode, BigDecimal taxRate, List<AgencySlot> agencySlots) {
    /// Copies and validates the sparse, ordered representation of all assigned positions.
    public TaxCodeMasterReference {
        requireWidth(taxCode, 5, "taxCode");
        Objects.requireNonNull(taxRate, "taxRate");
        agencySlots = List.copyOf(agencySlots);
        if (agencySlots.size() > 40) {
            throw new IllegalArgumentException("agencySlots must not exceed 40 entries");
        }
        Set<Integer> positions = new HashSet<>();
        int previous = 0;
        for (AgencySlot slot : agencySlots) {
            if (!positions.add(slot.position()) || slot.position() <= previous) {
                throw new IllegalArgumentException(
                        "agencySlots must use unique ascending source positions");
            }
            previous = slot.position();
        }
    }

    /// One assigned position in the tax code's fixed 40-position source order.
    ///
    /// @param position one-based source position
    /// @param agencyNumber nine-character signed agency value, including leading zeroes
    public record AgencySlot(int position, String agencyNumber) {
        /// Requires a valid source position and fixed-width agency identity.
        public AgencySlot {
            if (position < 1 || position > 40) {
                throw new IllegalArgumentException("position must be between 1 and 40");
            }
            Objects.requireNonNull(agencyNumber, "agencyNumber");
            boolean unsigned = agencyNumber.chars().allMatch(Character::isDigit);
            boolean negative =
                    agencyNumber.startsWith("-")
                            && agencyNumber.substring(1).chars().allMatch(Character::isDigit);
            if (agencyNumber.length() != 9 || (!unsigned && !negative)) {
                throw new IllegalArgumentException(
                        "agencyNumber must contain a nine-character signed decimal value");
            }
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
