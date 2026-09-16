
package org.cookcounty.tax.infrastructure.adapter.in.rest.dto;

// GENERATED-IMPORTS:start

import java.time.LocalDate;

import java.util.List;

// GENERATED-IMPORTS:end
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EifdTifIncrementRunResponse {

    // GENERATED-FIELDS:start

    private LocalDate businessDate;

    private String businessTime;

    private Long id;

    private List<EifdTifIncrementMessage> messages;

    private List<EifdTifIncrementOutput> outputs;

    private Integer recordsRead;

    private Integer recordsRejected;

    private Integer recordsUpdated;

    private Integer recordsWritten;

    private Integer returnCode;

    private String status;

    // GENERATED-FIELDS:end

    public EifdTifIncrementRunResponse() {}

    // GENERATED-ACCESSORS:start

    public LocalDate getBusinessDate() {
        return businessDate;
    }
    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getBusinessTime() {
        return businessTime;
    }
    public void setBusinessTime(String businessTime) {
        this.businessTime = businessTime;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public List<EifdTifIncrementMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<EifdTifIncrementMessage> messages) {
        this.messages = messages;
    }

    public List<EifdTifIncrementOutput> getOutputs() {
        return outputs;
    }
    public void setOutputs(List<EifdTifIncrementOutput> outputs) {
        this.outputs = outputs;
    }

    public Integer getRecordsRead() {
        return recordsRead;
    }
    public void setRecordsRead(Integer recordsRead) {
        this.recordsRead = recordsRead;
    }

    public Integer getRecordsRejected() {
        return recordsRejected;
    }
    public void setRecordsRejected(Integer recordsRejected) {
        this.recordsRejected = recordsRejected;
    }

    public Integer getRecordsUpdated() {
        return recordsUpdated;
    }
    public void setRecordsUpdated(Integer recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public Integer getRecordsWritten() {
        return recordsWritten;
    }
    public void setRecordsWritten(Integer recordsWritten) {
        this.recordsWritten = recordsWritten;
    }

    public Integer getReturnCode() {
        return returnCode;
    }
    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // GENERATED-ACCESSORS:end
}
