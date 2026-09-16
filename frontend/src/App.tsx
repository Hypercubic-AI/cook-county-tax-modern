import { FormEvent, useEffect, useRef, useState } from "react";
import { API_BASE_URL, ApiError, endpoints, startAndPoll } from "./api";
import type {
  AssessedValuePreparationRequest,
  AssessedValuePreparationRun,
  BatchRunRequest,
  BatchRunResponse,
  EifdTifIncrementRequest,
  EifdTifIncrementRun,
  MessageSeverity,
  PropertyTaxExemptionsRequest,
  PropertyTaxExemptionsRun,
  TaxRateInputPreparationRequest,
  TaxRateInputPreparationRun,
} from "./models";

type FormValues = Record<string, string>;

type Field = {
  name: string;
  label: string;
  type?: "text" | "date" | "time" | "select";
  hint?: string;
  pattern?: string;
  maxLength?: number;
  options?: Array<{ value: string; label: string }>;
};

type BatchDefinition<T extends BatchRunResponse> = {
  id: string;
  eyebrow: string;
  title: string;
  description: string;
  endpoint: string;
  fields: Field[];
  initialValues: FormValues;
  makeRequest: (values: FormValues) => BatchRunRequest;
};

const commonFields: Field[] = [
  { name: "businessDate", label: "Business date", type: "date" },
  { name: "businessTime", label: "Business time", type: "time", hint: "Pinned for the complete batch run." },
  { name: "idempotencyKey", label: "Idempotency key", maxLength: 128, hint: "Reuse only for the same controls." },
];

const definitions: Array<BatchDefinition<BatchRunResponse>> = [
  {
    id: "assessed-value",
    eyebrow: "Batch 01",
    title: "Assessed Value Preparation",
    description: "Classify assessment details, prepare parcel valuation buckets, and convert Type-5 details.",
    endpoint: endpoints.assessedValuePreparation,
    fields: [
      ...commonFields,
      { name: "processYear", label: "Process year", pattern: "[0-9]{2}", maxLength: 2, hint: "Two digits, for example 26." },
    ],
    initialValues: {
      businessDate: "2025-09-15",
      businessTime: "12:00:00",
      idempotencyKey: "ui-assessed-value-2025-09-15-noon",
      processYear: "26",
    },
    makeRequest: (values) => ({
      businessDate: values.businessDate,
      businessTime: values.businessTime,
      idempotencyKey: values.idempotencyKey,
      processYear: values.processYear,
    } satisfies AssessedValuePreparationRequest),
  },
  {
    id: "exemptions",
    eyebrow: "Batch 02",
    title: "Property Tax Exemptions",
    description: "Run the evidenced homeowner eligibility and exemption roll-forward path over shared server data.",
    endpoint: endpoints.propertyTaxExemptions,
    fields: [
      ...commonFields,
      {
        name: "homeownerProcessingVariant",
        label: "Homeowner processing variant",
        type: "select",
        hint: "The variants are alternatives, not successive stages.",
        options: [
          { value: "ENUMERATED", label: "Enumerated classes (HOME852)" },
          { value: "BROAD", label: "Broad classes (HOME853)" },
        ],
      },
    ],
    initialValues: {
      businessDate: "2025-09-15",
      businessTime: "12:00:00",
      idempotencyKey: "ui-property-tax-exemptions-2025-09-15-enumerated",
      homeownerProcessingVariant: "ENUMERATED",
    },
    makeRequest: (values) => ({
      businessDate: values.businessDate,
      businessTime: values.businessTime,
      idempotencyKey: values.idempotencyKey,
      homeownerProcessingVariant: values.homeownerProcessingVariant as "ENUMERATED" | "BROAD",
    } satisfies PropertyTaxExemptionsRequest),
  },
  {
    id: "eifd",
    eyebrow: "Batch 03",
    title: "EIFD / TIF Increment",
    description: "Carry frozen valuation from assessment comparison through agency equalized-valuation posting.",
    endpoint: endpoints.eifdTifIncrement,
    fields: [
      ...commonFields,
      { name: "reassessmentControl", label: "Reassessment control", pattern: "[0-9]{12}", maxLength: 12, hint: "Twelve numeric control characters." },
      { name: "processingYear", label: "Processing year", pattern: "[0-9]{2}", maxLength: 2 },
      { name: "reportingYear", label: "Reporting year", pattern: "[0-9]{4}", maxLength: 4 },
      { name: "annualEqualizationFactor", label: "Annual equalization factor", pattern: "[0-9]{5}", maxLength: 5, hint: "Five digits with four implied decimals." },
    ],
    initialValues: {
      businessDate: "2025-09-15",
      businessTime: "12:00:00",
      idempotencyKey: "ui-eifd-2025-09-15-noon",
      reassessmentControl: "260181202526",
      processingYear: "26",
      reportingYear: "2026",
      annualEqualizationFactor: "10000",
    },
    makeRequest: (values) => ({
      businessDate: values.businessDate,
      businessTime: values.businessTime,
      idempotencyKey: values.idempotencyKey,
      reassessmentControl: values.reassessmentControl,
      processingYear: values.processingYear,
      reportingYear: values.reportingYear,
      annualEqualizationFactor: values.annualEqualizationFactor,
    } satisfies EifdTifIncrementRequest),
  },
  {
    id: "tax-rate",
    eyebrow: "Batch 04",
    title: "Tax Rate Input Preparation",
    description: "Prepare division-aware parcel and agency EAV inputs for external tax-rate calculation.",
    endpoint: endpoints.taxRateInputPreparation,
    fields: commonFields,
    initialValues: {
      businessDate: "2025-09-15",
      businessTime: "12:00:00",
      idempotencyKey: "ui-tax-rate-input-2025-09-15-noon",
    },
    makeRequest: (values) => ({
      businessDate: values.businessDate,
      businessTime: values.businessTime,
      idempotencyKey: values.idempotencyKey,
    } satisfies TaxRateInputPreparationRequest),
  },
];

function prettyLabel(value: string): string {
  return value
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/_/g, " ")
    .replace(/^./, (letter) => letter.toUpperCase());
}

function statusClass(status: string): string {
  return `status status--${status.toLowerCase().replaceAll("_", "-")}`;
}

function severityClass(severity: MessageSeverity): string {
  return `message message--${severity.toLowerCase()}`;
}

function isAbortError(error: unknown): boolean {
  return error instanceof DOMException && error.name === "AbortError";
}

function failureText(error: unknown): string {
  if (error instanceof ApiError) {
    const context = [error.code, error.ruleId].filter(Boolean).join(" · ");
    return context ? `${error.message} (${context})` : error.message;
  }
  return error instanceof Error ? error.message : "The request failed for an unknown reason.";
}

function RunSummary({ run }: { run: BatchRunResponse }) {
  const metrics = [
    ["Records read", run.recordsRead],
    ["Records written", run.recordsWritten],
    ["Records updated", run.recordsUpdated],
    ["Records rejected", run.recordsRejected],
  ] as const;

  const outputs = run.outputs ?? [];
  const messages = run.messages ?? [];

  return (
    <div className="result" aria-live="polite">
      <div className="result__headline">
        <span className={statusClass(run.status)}>{run.status}</span>
        <span>Run {run.id}</span>
        <span>Return code {run.returnCode ?? "pending"}</span>
        {"partialOutput" in run && run.partialOutput ? <span className="warning-text">Partial output</span> : null}
      </div>

      <dl className="metrics">
        {metrics.map(([label, value]) => (
          <div key={label}>
            <dt>{label}</dt>
            <dd>{value ?? "—"}</dd>
          </div>
        ))}
      </dl>

      {outputs.length > 0 ? (
        <section className="result__section" aria-labelledby={`outputs-${run.id}`}>
          <h4 id={`outputs-${run.id}`}>Outputs</h4>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Output</th><th>Kind / state</th><th>Records</th></tr></thead>
              <tbody>
                {outputs.map((output, index) => {
                  const name = "name" in output ? output.name : output.artifactId;
                  const detail = "publicationStatus" in output
                    ? `${output.kind} · ${output.publicationStatus}`
                    : "available" in output
                      ? `${output.kind} · ${output.available ? "available" : "unavailable"}`
                      : "partial" in output
                        ? `${output.kind} · ${output.partial ? "partial" : "complete"}`
                        : `generation ${output.generation ?? "current"}`;
                  return <tr key={`${name}-${index}`}><td>{name}</td><td>{detail}</td><td>{output.recordCount}</td></tr>;
                })}
              </tbody>
            </table>
          </div>
        </section>
      ) : <p className="empty-state">No output metadata has been published.</p>}

      {"stages" in run && run.stages?.length ? <AssessedStages run={run} /> : null}
      {"reconciliations" in run && run.reconciliations?.length ? <ExemptionReconciliations run={run} /> : null}
      {"reconciliation" in run && run.reconciliation ? <TaxRateReconciliation run={run} /> : null}
      {"ruleOutcomes" in run && run.ruleOutcomes?.length ? <RuleOutcomeSummary run={run} /> : null}

      {messages.length > 0 ? (
        <section className="result__section">
          <h4>Messages</h4>
          <ul className="message-list">
            {messages.map((message, index) => {
              const text = "text" in message ? message.text : message.message;
              const code = "code" in message ? message.code : undefined;
              return (
                <li className={severityClass(message.severity)} key={`${message.ruleId ?? code ?? "message"}-${index}`}>
                  <strong>{message.severity}</strong>
                  <span>{text}</span>
                  {code || message.ruleId ? <small>{[code, message.ruleId].filter(Boolean).join(" · ")}</small> : null}
                </li>
              );
            })}
          </ul>
        </section>
      ) : null}
    </div>
  );
}

function AssessedStages({ run }: { run: AssessedValuePreparationRun }) {
  return (
    <section className="result__section">
      <h4>Stage results</h4>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Stage</th><th>Status</th><th>Return code</th><th>Read / written / rejected</th></tr></thead>
          <tbody>{run.stages!.map((stage) => (
            <tr key={stage.stage}>
              <td>{prettyLabel(stage.stage)}</td><td>{stage.status}</td><td>{stage.returnCode ?? "—"}</td>
              <td>{stage.recordsRead} / {stage.recordsWritten} / {stage.recordsRejected}</td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </section>
  );
}

function ExemptionReconciliations({ run }: { run: PropertyTaxExemptionsRun }) {
  return (
    <section className="result__section">
      <h4>Reconciliation</h4>
      <div className="table-wrap"><table>
        <thead><tr><th>Stage</th><th>Status</th><th>Read</th><th>Matched</th><th>Written</th><th>Rejected</th></tr></thead>
        <tbody>{run.reconciliations!.map((item) => (
          <tr key={item.name}><td>{item.name}</td><td>{item.status}</td><td>{item.recordsRead}</td><td>{item.recordsMatched}</td><td>{item.recordsWritten}</td><td>{item.recordsRejected}</td></tr>
        ))}</tbody>
      </table></div>
    </section>
  );
}

function TaxRateReconciliation({ run }: { run: TaxRateInputPreparationRun }) {
  return (
    <section className="result__section">
      <h4>Reconciliation</h4>
      <div className="reconciliation-grid">
        {Object.entries(run.reconciliation!).map(([name, counts]) => counts ? (
          <dl key={name} className="reconciliation-card">
            <div className="reconciliation-card__title"><dt>{prettyLabel(name)}</dt></div>
            {Object.entries(counts).map(([label, value]) => (
              <div key={label}><dt>{prettyLabel(label)}</dt><dd>{String(value)}</dd></div>
            ))}
          </dl>
        ) : null)}
      </div>
    </section>
  );
}

function RuleOutcomeSummary({ run }: { run: PropertyTaxExemptionsRun }) {
  const totals = run.ruleOutcomes!.reduce<Record<string, number>>((summary, item) => {
    summary[item.outcome] = (summary[item.outcome] ?? 0) + 1;
    return summary;
  }, {});
  return (
    <section className="result__section">
      <h4>Rule outcomes</h4>
      <div className="outcome-list">{Object.entries(totals).map(([outcome, count]) => (
        <span key={outcome}>{prettyLabel(outcome)} <strong>{count}</strong></span>
      ))}</div>
    </section>
  );
}

function BatchCard<T extends BatchRunResponse>({ definition }: { definition: BatchDefinition<T> }) {
  const [values, setValues] = useState<FormValues>(definition.initialValues);
  const [run, setRun] = useState<T | null>(null);
  const [failure, setFailure] = useState<string | null>(null);
  const [working, setWorking] = useState(false);
  const activeController = useRef<AbortController | null>(null);

  useEffect(() => () => activeController.current?.abort(), []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    activeController.current?.abort();
    const controller = new AbortController();
    activeController.current = controller;
    setWorking(true);
    setFailure(null);
    setRun(null);

    try {
      await startAndPoll<T>(definition.endpoint, definition.makeRequest(values), controller.signal, setRun);
    } catch (error) {
      if (!isAbortError(error)) setFailure(failureText(error));
    } finally {
      if (activeController.current === controller) {
        activeController.current = null;
        setWorking(false);
      }
    }
  }

  return (
    <article className="batch-card" id={definition.id}>
      <div className="batch-card__intro">
        <p className="eyebrow">{definition.eyebrow}</p>
        <h2>{definition.title}</h2>
        <p>{definition.description}</p>
        <code>{definition.endpoint}</code>
      </div>

      <form onSubmit={submit} className="batch-form">
        <div className="field-grid">
          {definition.fields.map((field) => (
            <label className="field" key={field.name}>
              <span>{field.label}</span>
              {field.type === "select" ? (
                <select
                  value={values[field.name]}
                  onChange={(event) => setValues((current) => ({ ...current, [field.name]: event.target.value }))}
                  required
                >
                  {field.options?.map((option) => <option value={option.value} key={option.value}>{option.label}</option>)}
                </select>
              ) : (
                <input
                  type={field.type ?? "text"}
                  value={values[field.name]}
                  onChange={(event) => setValues((current) => ({ ...current, [field.name]: event.target.value }))}
                  pattern={field.pattern}
                  maxLength={field.maxLength}
                  step={field.type === "time" ? 1 : undefined}
                  required
                />
              )}
              {field.hint ? <small>{field.hint}</small> : null}
            </label>
          ))}
        </div>
        <div className="actions">
          <button className="run-button" type="submit" disabled={working}>
            {working ? "Run in progress…" : `Run ${definition.title}`}
          </button>
          {working ? (
            <button className="cancel-button" type="button" onClick={() => activeController.current?.abort()}>
              Stop polling
            </button>
          ) : null}
        </div>
      </form>

      {failure ? <div className="failure" role="alert"><strong>Request failed</strong><span>{failure}</span></div> : null}
      {run ? <RunSummary run={run} /> : null}
    </article>
  );
}

export default function App() {
  return (
    <div className="app-shell">
      <header className="hero">
        <div className="hero__bar">
          <a className="brand" href="#top" aria-label="Cook County Tax Operations home">
            <span className="brand__mark">CC</span>
            <span>Tax Operations</span>
          </a>
          <span className="environment">API <code>{API_BASE_URL}</code></span>
        </div>
        <div className="hero__content" id="top">
          <p className="eyebrow">Modernized batch console</p>
          <h1>Four accepted tax workflows.<br />One operating surface.</h1>
          <p>Start and observe live asynchronous runs against the generated backend. Each result remains tied to its accepted batch contract.</p>
        </div>
        <nav aria-label="Batch workflows">
          {definitions.map((definition) => <a href={`#${definition.id}`} key={definition.id}>{definition.title}</a>)}
        </nav>
      </header>

      <main>
        <section className="section-heading">
          <p className="eyebrow">Operator workspace</p>
          <h2>Batch runs</h2>
          <p>Controls are sent to shared live server state. A created response is polled until the run completes or fails.</p>
        </section>
        <div className="batch-list">
          {definitions.map((definition) => <BatchCard definition={definition} key={definition.id} />)}
        </div>
      </main>

      <footer>
        <span>Cook County Tax Modernization</span>
        <a href="#top">Back to top</a>
      </footer>
    </div>
  );
}
