package org.cookcounty.tax.domain.contract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/// Describes one published assessed-value artifact.
///
/// @param artifactId stable run-scoped artifact identifier
/// @param kind business role of the report or assessment-master output
/// @param mediaType media type required to read the artifact
/// @param partial whether an ordering failure limited the published records
/// @param recordCount number of fixed-width or text records in the artifact
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessedValuePreparationOutput(
        String artifactId, String kind, String mediaType, Boolean partial, Integer recordCount) {}
