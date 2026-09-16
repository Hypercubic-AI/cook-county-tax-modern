package org.cookcounty.tax.application.service;

/// One recoverable assessed-value observation before API mapping.
///
/// @param severity `WARNING` or `ERROR` as determined by the ordered stage
/// @param code stable machine-readable observation code
/// @param ruleId accepted business-rule identifier
/// @param message safe operator explanation
/// @param recordKey township, volume, parcel, and tax-type key for the affected record
public record AssessedValueRuleMessage(
        String severity, String code, String ruleId, String message, String recordKey) {}
