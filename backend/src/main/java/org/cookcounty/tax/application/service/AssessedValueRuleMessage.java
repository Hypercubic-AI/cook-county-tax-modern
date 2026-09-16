package org.cookcounty.tax.application.service;

/** A rule-addressed batch observation before it is mapped to the API DTO. */
public record AssessedValueRuleMessage(
        String severity,
        String code,
        String ruleId,
        String message,
        String recordKey) {}
