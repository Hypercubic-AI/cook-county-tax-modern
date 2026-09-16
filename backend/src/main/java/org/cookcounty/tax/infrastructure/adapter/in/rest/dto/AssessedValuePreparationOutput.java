
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

// GENERATED-IMPORTS:end

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class AssessedValuePreparationOutput {

    // GENERATED-FIELDS:start

    private String artifactId;

    private String kind;

    private String mediaType;

    private Boolean partial;

    private Integer recordCount;

    // GENERATED-FIELDS:end

    public AssessedValuePreparationOutput() {}

    // GENERATED-ACCESSORS:start

    public String getArtifactId() {
        return artifactId;
    }
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getMediaType() {
        return mediaType;
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Boolean getPartial() {
        return partial;
    }
    public void setPartial(Boolean partial) {
        this.partial = partial;
    }

    public Integer getRecordCount() {
        return recordCount;
    }
    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    // GENERATED-ACCESSORS:end
}
