package org.cookcounty.tax.domain.model;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/// Verifies source-width identity and fixed-point boundaries that primitive numerics cannot retain.
class AssessmentNumericContractTest {
    @Test
    void preservesLeadingZeroAndSignedBusinessKeys() {
        TaxRateEqualizedValue taxValue =
                new TaxRateEqualizedValue(
                        1,
                        "001",
                        "000000000000042",
                        "00007",
                        new BigDecimal("125"),
                        new BigDecimal("126"),
                        "0",
                        null);
        AssessmentParcelSourceRecord signedParcel =
                new AssessmentParcelSourceRecord("-000000000000042", 1, "AA==");

        assertThat(taxValue.volumeNumber()).isEqualTo("001");
        assertThat(taxValue.parcelNumber()).isEqualTo("000000000000042");
        assertThat(taxValue.taxCode()).isEqualTo("00007");
        assertThat(signedParcel.parcelNumber()).isEqualTo("-000000000000042");
    }

    @Test
    void rejectsWholeUnitPrecisionOverflowBeforePersistence() {
        ArithmeticException failure =
                assertThrows(
                        ArithmeticException.class,
                        () ->
                                new TaxRateEqualizedValue(
                                        1,
                                        "001",
                                        "000000000000042",
                                        "00007",
                                        new BigDecimal("100000000000"),
                                        BigDecimal.ZERO,
                                        "0",
                                        null));

        assertThat(failure).hasMessageThat().contains("assessedValue");
    }

    @Test
    void rejectsFrozenParcelCountPrecisionOverflowBeforePersistence() {
        ArithmeticException failure =
                assertThrows(
                        ArithmeticException.class,
                        () ->
                                EifdNumericBoundary.signedIntegral(
                                        10_000_000_000_000L,
                                        9_999_999_999_999L,
                                        "currentParcelCount"));

        assertThat(failure).hasMessageThat().contains("currentParcelCount");
    }

    @Test
    void rejectsFractionalWholeUnitValuesInsteadOfRounding() {
        assertThrows(
                ArithmeticException.class,
                () ->
                        new TaxRateEqualizedValue(
                                1,
                                "001",
                                "000000000000042",
                                "00007",
                                new BigDecimal("1.5"),
                                BigDecimal.ZERO,
                                "0",
                                null));
    }
}
