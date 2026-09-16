import type { BatchRunRequest, BatchRunResponse, ErrorResponse } from "./models";

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
export const API_BASE_URL = (configuredBaseUrl || "http://localhost:8080").replace(/\/+$/, "");

export const endpoints = {
  assessedValuePreparation: "/api/assessed-value-preparation-runs",
  propertyTaxExemptions: "/api/property-tax-exemptions-runs",
  eifdTifIncrement: "/api/eifd-tif-increment-runs",
  taxRateInputPreparation: "/api/tax-rate-input-preparation-runs",
} as const;

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly ruleId?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function readJson<T>(response: Response): Promise<T> {
  const body = (await response.json().catch(() => null)) as T | ErrorResponse | null;
  if (!response.ok) {
    const error = body as ErrorResponse | null;
    throw new ApiError(
      error?.message || `${response.status} ${response.statusText}`,
      response.status,
      error?.error,
      error?.ruleId,
    );
  }
  if (body === null) {
    throw new ApiError("The server returned an empty response.", response.status);
  }
  return body as T;
}

async function request<T>(path: string, init: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...init.headers,
    },
  });
  return readJson<T>(response);
}

function wait(milliseconds: number, signal: AbortSignal): Promise<void> {
  signal.throwIfAborted();
  const { promise, resolve, reject } = Promise.withResolvers<void>();
  const abort = () => {
    window.clearTimeout(timeout);
    reject(new DOMException("Polling was cancelled.", "AbortError"));
  };
  const timeout = window.setTimeout(() => {
    signal.removeEventListener("abort", abort);
    resolve();
  }, milliseconds);
  signal.addEventListener("abort", abort, { once: true });
  return promise;
}

export async function startAndPoll<T extends BatchRunResponse>(
  endpoint: string,
  payload: BatchRunRequest,
  signal: AbortSignal,
  onSnapshot: (run: T) => void,
): Promise<T> {
  let run = await request<T>(endpoint, {
    method: "POST",
    body: JSON.stringify(payload),
    signal,
  });
  onSnapshot(run);

  while (run.status === "QUEUED" || run.status === "RUNNING") {
    await wait(1_000, signal);
    run = await request<T>(`${endpoint}/${encodeURIComponent(String(run.id))}`, {
      method: "GET",
      signal,
    });
    onSnapshot(run);
  }

  return run;
}
