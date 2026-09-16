export type RunStatus = "QUEUED" | "RUNNING" | "COMPLETED" | "FAILED";
export type MessageSeverity = "INFO" | "WARNING" | "ERROR";

export interface ErrorResponse {
  error: string;
  message: string;
  ruleId?: string;
}

export interface BatchRunBase {
  id: number;
  status: RunStatus;
  businessDate: string;
  businessTime: string;
  returnCode?: number;
  recordsRead?: number;
  recordsWritten?: number;
  recordsUpdated?: number;
  recordsRejected?: number;
}

export interface AssessedValuePreparationRequest {
  businessDate: string;
  businessTime: string;
  idempotencyKey: string;
  processYear: string;
}

export interface AssessedValueOutput {
  artifactId: string;
  kind:
    | "OVERALL_CLASS_REPORT"
    | "VALUATION_BREAKDOWN_REPORT"
    | "ASSESSMENT_MASTER"
    | "TYPE5_ERROR_REPORT";
  mediaType: string;
  recordCount: number;
  partial: boolean;
}

export interface AssessedValueMessage {
  severity: MessageSeverity;
  code: string;
  ruleId?: string;
  message: string;
  recordKey?: string;
}

export interface AssessedValueStageResult {
  stage: "OVERALL_CLASS" | "VALUATION_BUCKETING" | "TYPE5_CONVERSION";
  status: "NOT_STARTED" | "RUNNING" | "COMPLETED" | "FAILED";
  returnCode?: number;
  recordsRead: number;
  recordsWritten: number;
  recordsUpdated: number;
  recordsRejected: number;
  outputPublished: boolean;
  partialOutput: boolean;
}

export interface AssessedValuePreparationRun extends BatchRunBase {
  processYear: string;
  partialOutput?: boolean;
  outputs: AssessedValueOutput[];
  messages: AssessedValueMessage[];
  stages?: AssessedValueStageResult[];
}

export interface PropertyTaxExemptionsRequest {
  businessDate: string;
  businessTime: string;
  idempotencyKey: string;
  homeownerProcessingVariant: "ENUMERATED" | "BROAD";
}

export interface PropertyTaxExemptionsOutput {
  name: string;
  kind: "DATASET" | "REPORT" | "STAGING";
  recordCount: number;
  publicationStatus: "PUBLISHED" | "EMPTY" | "WITHHELD";
  ruleIds?: string[];
}

export interface PropertyTaxExemptionsMessage {
  severity: MessageSeverity;
  message: string;
  ruleId?: string;
}

export interface PropertyTaxExemptionsRejection {
  source: string;
  recordKey?: string;
  outcome: "REJECTED" | "BYPASSED" | "PARTIALLY_APPLIED" | "WARNING";
  message: string;
  ruleId: string;
}

export interface PropertyTaxExemptionsReconciliation {
  name: string;
  status: "RECONCILED" | "RECONCILED_WITH_REJECTIONS" | "PARTIALLY_APPLIED" | "FAILED";
  recordsRead: number;
  recordsMatched: number;
  recordsWritten: number;
  recordsRejected: number;
  ruleIds: string[];
  message?: string;
}

export interface PropertyTaxExemptionsRuleOutcome {
  ruleId: string;
  outcome: "APPLIED" | "NOT_APPLICABLE" | "REJECTED" | "WARNING" | "FAILED";
  recordsAffected: number;
  message?: string;
}

export interface PropertyTaxExemptionsRun extends BatchRunBase {
  homeownerProcessingVariant: "ENUMERATED" | "BROAD";
  outputs?: PropertyTaxExemptionsOutput[];
  messages?: PropertyTaxExemptionsMessage[];
  rejections?: PropertyTaxExemptionsRejection[];
  reconciliations?: PropertyTaxExemptionsReconciliation[];
  ruleOutcomes?: PropertyTaxExemptionsRuleOutcome[];
}

export interface EifdTifIncrementRequest {
  businessDate: string;
  businessTime: string;
  idempotencyKey: string;
  reassessmentControl: string;
  processingYear: string;
  reportingYear: string;
  annualEqualizationFactor: string;
}

export interface EifdTifIncrementOutput {
  name: string;
  recordCount: number;
  generation?: number;
}

export interface EifdTifIncrementMessage {
  severity: MessageSeverity;
  text: string;
  ruleId?: string;
}

export interface EifdTifIncrementRun extends BatchRunBase {
  outputs: EifdTifIncrementOutput[];
  messages: EifdTifIncrementMessage[];
}

export interface TaxRateInputPreparationRequest {
  businessDate: string;
  businessTime: string;
  idempotencyKey: string;
}

export interface TaxRateInputPreparationOutput {
  name: string;
  kind: "DIVIDED_VALUE" | "AGENCY_ASSESSMENT" | "ANNEX_DISCONNECT" | "FROZEN_AGENCY" | "REPORT";
  recordCount: number;
  available: boolean;
}

export interface TaxRateInputPreparationMessage {
  severity: MessageSeverity;
  code: string;
  text: string;
  ruleId?: string;
}

export type ReconciliationCounts = Record<string, number | boolean>;

export interface TaxRateInputPreparationRun extends BatchRunBase {
  outputs: TaxRateInputPreparationOutput[];
  messages: TaxRateInputPreparationMessage[];
  reconciliation?: {
    divisionStamping?: ReconciliationCounts;
    agencyAttachment?: ReconciliationCounts;
    agencyComparison?: ReconciliationCounts;
    frozenAgencyPosting?: ReconciliationCounts;
  };
}

export type BatchRunResponse =
  | AssessedValuePreparationRun
  | PropertyTaxExemptionsRun
  | EifdTifIncrementRun
  | TaxRateInputPreparationRun;

export type BatchRunRequest =
  | AssessedValuePreparationRequest
  | PropertyTaxExemptionsRequest
  | EifdTifIncrementRequest
  | TaxRateInputPreparationRequest;
