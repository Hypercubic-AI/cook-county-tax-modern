package org.cookcounty.tax.domain.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/// Controls one assessed-value preparation run before structural validation.
///
/// Missing JSON values remain nullable so Bean Validation can return all structural violations in
/// one response. The service rechecks these controls before it reserves persisted run identity.
///
/// @param businessDate date printed in the source-compatible valuation reports
/// @param businessTime 24-hour run time in `HH:mm:ss` form
/// @param idempotencyKey caller key that identifies an exact run request
/// @param processYear two decimal digits used by Type-5 year conversion
public record AssessedValuePreparationRunRequest(
        @Nullable @NotNull(message = "businessDate is required.") LocalDate businessDate,
        @Nullable
                @NotNull(message = "businessTime is required.")
                @Pattern(
                        regexp = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                        message = "businessTime must use HH:mm:ss in 24-hour time.")
                String businessTime,
        @Nullable
                @NotBlank(message = "idempotencyKey must not be blank.")
                @Size(max = 128, message = "idempotencyKey must contain at most 128 characters.")
                String idempotencyKey,
        @Nullable
                @NotNull(message = "processYear is required.")
                @Pattern(
                        regexp = "^[0-9]{2}$",
                        message = "processYear must contain exactly two decimal digits.")
                String processYear) {}
