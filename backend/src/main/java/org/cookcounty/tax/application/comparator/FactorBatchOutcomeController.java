package org.cookcounty.tax.application.comparator;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/// Exposes completed comparison evidence only when the comparator profile is active.
///
/// This internal endpoint does not launch jobs or provide the production run-state store.
@Profile(FactorBatchOutcomeRecorder.PROFILE)
@RestController
@RequestMapping("/internal/factor-comparator")
public class FactorBatchOutcomeController {

    /// Joins approved-request provenance with completed comparison results.
    private final FactorBatchOutcomeRecorder recorder;

    /// Connects the profile-scoped endpoint to its evidence recorder.
    ///
    /// @param recorder recorder shared with the capability outcome projectors
    public FactorBatchOutcomeController(FactorBatchOutcomeRecorder recorder) {
        this.recorder = recorder;
    }

    /// Reads a comparison result after both request approval and completion arrive.
    ///
    /// @param scenarioId approved comparator scenario identifier
    /// @param runId database-assigned application run identifier
    /// @return 200 with complete evidence, or 404 when either evidence half is absent
    @GetMapping("/{scenarioId}-runs/{runId}/outcome")
    public ResponseEntity<FactorBatchOutcomeRecorder.RecordedOutcome> getOutcome(
            @PathVariable String scenarioId, @PathVariable long runId) {
        Optional<FactorBatchOutcomeRecorder.RecordedOutcome> outcome =
                recorder.get(scenarioId, runId);
        if (outcome.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(outcome.orElseThrow());
    }
}
