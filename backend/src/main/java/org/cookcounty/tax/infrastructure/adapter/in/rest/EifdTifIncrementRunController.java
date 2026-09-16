package org.cookcounty.tax.infrastructure.adapter.in.rest;

import java.net.URI;
import java.util.Map;
import jakarta.validation.Valid;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.port.in.EifdTifIncrementRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eifd-tif-increment-runs")
public final class EifdTifIncrementRunController {

    private final EifdTifIncrementRunUseCase useCase;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    public EifdTifIncrementRunController(
            EifdTifIncrementRunUseCase useCase,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeRecorder = outcomeRecorder;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEifdTifIncrementRun(@PathVariable Long id) {
        return useCase.getEifdTifIncrementRun(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> {
                    EifdTifIncrementErrorResponse error = new EifdTifIncrementErrorResponse();
                    error.setError("RUN_NOT_FOUND");
                    error.setMessage(
                            "No EIFD/TIF increment run was found for id " + id + ".");
                    return ResponseEntity.status(404).body(error);
                });
    }

    @PostMapping
    public ResponseEntity<EifdTifIncrementRunResponse> startEifdTifIncrementRun(
            @Valid @RequestBody EifdTifIncrementRunRequest request,
            @RequestHeader Map<String, String> headers) {
        var result = useCase.startEifdTifIncrementRun(request);
        outcomeRecorder.accepted("eifd-tif-increment", result.id(), request, headers);
        URI location = URI.create("/api/eifd-tif-increment-runs/" + result.id());
        return ResponseEntity.created(location).body(result.response());
    }
}
