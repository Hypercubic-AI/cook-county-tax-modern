
package org.cookcounty.tax.infrastructure.adapter.in.rest;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.port.in.TaxRateInputPreparationRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax-rate-input-preparation-runs")
public class TaxRateInputPreparationRunController {

    private static final String RESOURCE_PATH = "/api/tax-rate-input-preparation-runs/";
    private static final String FACTOR_SCENARIO_ID = "clerk-agency-attachment";
    private final TaxRateInputPreparationRunUseCase useCase;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    public TaxRateInputPreparationRunController(
            TaxRateInputPreparationRunUseCase useCase,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeRecorder = outcomeRecorder;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaxRateInputPreparationRun(@PathVariable Long id) {
        return useCase.getTaxRateInputPreparationRun(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(error(
                                "RUN_NOT_FOUND",
                                "No run exists for the supplied identifier")));
    }

    @PostMapping
    public ResponseEntity<TaxRateInputPreparationRunResponse> startTaxRateInputPreparationRun(
            @Valid @RequestBody TaxRateInputPreparationRunRequest request,
            @RequestHeader HttpHeaders requestHeaders) {
        var result = useCase.startTaxRateInputPreparationRun(request);
        outcomeRecorder.accepted(
                FACTOR_SCENARIO_ID, result.id(), request, flattened(requestHeaders));
        URI location = URI.create(RESOURCE_PATH + result.id());
        return ResponseEntity.created(location).body(result.response());
    }

    private static Map<String, String> flattened(HttpHeaders headers) {
        Map<String, String> flattened = new LinkedHashMap<>();
        headers.forEach((name, values) -> flattened.put(name, String.join(",", values)));
        return Map.copyOf(flattened);
    }

    private static TaxRateInputPreparationErrorResponse error(String code, String message) {
        TaxRateInputPreparationErrorResponse response = new TaxRateInputPreparationErrorResponse();
        response.setError(code);
        response.setMessage(message);
        return response;
    }
}
