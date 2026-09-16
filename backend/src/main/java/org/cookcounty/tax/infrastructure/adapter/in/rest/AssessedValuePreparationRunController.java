
package org.cookcounty.tax.infrastructure.adapter.in.rest;

import java.net.URI;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.cookcounty.tax.domain.port.in.AssessedValuePreparationRunUseCase;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/assessed-value-preparation-runs")
public class AssessedValuePreparationRunController {

    private final AssessedValuePreparationRunUseCase useCase;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    public AssessedValuePreparationRunController(
            AssessedValuePreparationRunUseCase useCase,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeRecorder = outcomeRecorder;
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getAssessedValuePreparationRun(
            @PathVariable @Positive Long id) {
        return useCase.getAssessedValuePreparationRun(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(notFound()));
    }


    @PostMapping
    public ResponseEntity<AssessedValuePreparationRunResponse> startAssessedValuePreparationRun(
            @Valid @RequestBody AssessedValuePreparationRunRequest request,
            @RequestHeader Map<String, String> headers) {
        AssessedValuePreparationRunResponse result =
                useCase.startAssessedValuePreparationRun(request);
        outcomeRecorder.accepted(
                "valuation-preparation", result.getId(), request, headers);
        URI location = URI.create(
                "/api/assessed-value-preparation-runs/" + result.getId());
        return ResponseEntity.created(location).body(result);
    }

    private static AssessedValuePreparationErrorResponse notFound() {
        AssessedValuePreparationErrorResponse error = new AssessedValuePreparationErrorResponse();
        error.setError("RUN_NOT_FOUND");
        error.setMessage("No assessed-value preparation run exists for the supplied id.");
        return error;
    }

}
