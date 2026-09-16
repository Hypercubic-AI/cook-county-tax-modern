package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/// Mutable composite identity for one tax-code agency position.
///
/// Both components are nullable only while JPA constructs the embeddable. A hydrated identity uses
/// a five-character tax code and a one-based position from the source's 40 positions.
@Embeddable
public class TaxCodeAgencySlotId implements Serializable {
    @Column(name = "tax_code", length = 5, nullable = false)
    private @Nullable String taxCode;

    @Column(name = "slot_position", nullable = false)
    private @Nullable Integer slotPosition;

    /// Creates an empty identity for JPA hydration.
    public TaxCodeAgencySlotId() {}

    /// Returns the hydrated tax-code identity.
    public @Nullable String getTaxCode() {
        return taxCode;
    }

    /// Supplies the tax-code identity during hydration or mapping.
    public void setTaxCode(@Nullable String taxCode) {
        this.taxCode = taxCode;
    }

    /// Returns the hydrated one-based source position.
    public @Nullable Integer getSlotPosition() {
        return slotPosition;
    }

    /// Supplies the one-based source position during hydration or mapping.
    public void setSlotPosition(@Nullable Integer slotPosition) {
        this.slotPosition = slotPosition;
    }

    /// Compares the complete composite identity.
    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TaxCodeAgencySlotId that)) {
            return false;
        }
        return Objects.equals(taxCode, that.taxCode)
                && Objects.equals(slotPosition, that.slotPosition);
    }

    /// Returns the hash of both identity components.
    @Override
    public int hashCode() {
        return Objects.hash(taxCode, slotPosition);
    }
}
