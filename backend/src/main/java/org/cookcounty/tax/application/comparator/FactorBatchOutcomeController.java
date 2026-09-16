package org.cookcounty.tax.application.comparator;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile(FactorBatchOutcomeRecorder.PROFILE)
@RestController
@RequestMapping("/internal/factor-comparator")
public class FactorBatchOutcomeController {

    private final FactorBatchOutcomeRecorder recorder;

    public FactorBatchOutcomeController(FactorBatchOutcomeRecorder recorder) {
        this.recorder = recorder;
    }

    @GetMapping("/{scenarioId}-runs/{runId}/outcome")
    public ResponseEntity<FactorBatchOutcomeRecorder.RecordedOutcome> getOutcome(
            @PathVariable String scenarioId,
            @PathVariable long runId) {
        return recorder.get(scenarioId, runId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
