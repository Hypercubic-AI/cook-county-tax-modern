package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// Metadata for one logical batch output.
///
/// @param generation catalog generation when the output is generation-managed, otherwise absent
/// @param name stable logical output name
/// @param recordCount number of records emitted to the output
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EifdTifIncrementOutput(
        @Nullable Integer generation, String name, Integer recordCount) {
    /// Requires the output identity and whole-record count.
    public EifdTifIncrementOutput {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(recordCount, "recordCount");
    }
}
