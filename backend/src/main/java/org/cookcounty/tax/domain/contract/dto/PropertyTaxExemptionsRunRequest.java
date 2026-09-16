package org.cookcounty.tax.domain.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/// Controls one property-tax-exemption batch run against server-owned input.
///
/// Missing components remain nullable until Bean Validation reports all structural errors.
///
/// @param businessDate date pinned for every stage in the run
/// @param businessTime 24-hour time in `HH:mm:ss` layout pinned for every stage
/// @param homeownerProcessingVariant either `ENUMERATED` or `BROAD`; the variants are alternatives
/// @param idempotencyKey client key that distinguishes one requested run for replay detection
public record PropertyTaxExemptionsRunRequest(
        @Nullable @NotNull(message = "businessDate is required.") LocalDate businessDate,
        @Nullable
                @NotNull(message = "businessTime is required.")
                @Pattern(
                        regexp = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                        message = "businessTime must use HH:mm:ss in 24-hour time.")
                String businessTime,
        @Nullable
                @NotNull(message = "homeownerProcessingVariant is required.")
                @Pattern(
                        regexp = "ENUMERATED|BROAD",
                        message = "homeownerProcessingVariant must be ENUMERATED or BROAD.")
                String homeownerProcessingVariant,
        @Nullable
                @NotBlank(message = "idempotencyKey must not be blank.")
                @Size(max = 128, message = "idempotencyKey must contain at most 128 characters.")
                String idempotencyKey) {}
