package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/// Selects the business clock and replay identity for one tax-rate input preparation run.
///
/// Structural validation reports all missing or malformed components before the service reserves a
/// run. The server reads source data from its repositories. This request does not identify files or
/// replace the repository inputs.
///
/// @param businessDate date used in every report header for the run
/// @param businessTime 24-hour business time in `HH:mm:ss` form, used consistently by every stage
/// @param idempotencyKey caller-selected key that identifies an exact start-request replay
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxRateInputPreparationRunRequest(
        @NotNull(message = "businessDate is required.") @Nullable LocalDate businessDate,
        @NotNull(message = "businessTime is required.")
                @Pattern(
                        regexp = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                        message = "businessTime must use HH:mm:ss in 24-hour time.")
                @Nullable String businessTime,
        @NotBlank(message = "idempotencyKey must not be blank.")
                @Size(max = 128, message = "idempotencyKey must contain at most 128 characters.")
                @Nullable String idempotencyKey) {}
