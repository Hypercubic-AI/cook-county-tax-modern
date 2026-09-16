package org.cookcounty.tax.domain.contract.dto;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class PropertyTaxExemptionsRunResponseTest {

    @Test
    void terminalSnapshotDoesNotChangeWhenSourceCollectionsChange() {
        var sourceMessages =
                new ArrayList<>(
                        List.of(new PropertyTaxExemptionsMessage("completed", null, "INFO")));
        var sourceOutputs =
                new ArrayList<>(
                        List.of(
                                new PropertyTaxExemptionsOutput(
                                        "DATASET",
                                        "HOMEOWNER",
                                        "PUBLISHED",
                                        1L,
                                        List.of("rule-1"))));

        var snapshot =
                new PropertyTaxExemptionsRunResponse(
                        LocalDate.of(2025, 9, 15),
                        "12:00:00",
                        "ENUMERATED",
                        41L,
                        sourceMessages,
                        sourceOutputs,
                        List.of(),
                        1L,
                        0L,
                        1L,
                        1L,
                        List.of(),
                        0,
                        List.of(),
                        "COMPLETED");

        sourceMessages.clear();
        sourceOutputs.clear();

        assertThat(snapshot.messages())
                .containsExactly(new PropertyTaxExemptionsMessage("completed", null, "INFO"));
        assertThat(snapshot.outputs())
                .containsExactly(
                        new PropertyTaxExemptionsOutput(
                                "DATASET", "HOMEOWNER", "PUBLISHED", 1L, List.of("rule-1")));
    }
}
