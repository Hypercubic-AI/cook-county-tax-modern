#!/usr/bin/env bash
set -Eeuo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

post_run() {
  local label="$1"
  local route="$2"
  local body="$3"
  local response run_id status

  printf '\n==> %s\n' "$label"
  response="$(curl --fail-with-body --silent --show-error \
    --request POST \
    --header 'Content-Type: application/json' \
    --data "$body" \
    "${BASE_URL}${route}")"
  run_id="$(jq --exit-status --raw-output '.id' <<<"$response")"

  for _ in {1..120}; do
    response="$(curl --fail-with-body --silent --show-error \
      "${BASE_URL}${route}/${run_id}")"
    status="$(jq --exit-status --raw-output '.status' <<<"$response")"
    case "$status" in
      COMPLETED)
        jq '{id, status, returnCode, outputs, messages}' <<<"$response"
        return 0
        ;;
      FAILED)
        jq . <<<"$response"
        return 1
        ;;
    esac
    sleep 0.25
  done

  printf 'Timed out waiting for %s run %s\n' "$label" "$run_id" >&2
  return 1
}

post_run \
  'Assessed Value Preparation' \
  '/api/assessed-value-preparation-runs' \
  '{"businessDate":"2025-09-15","businessTime":"12:00:00","idempotencyKey":"modernized-assessed-value-20250915-noon","processYear":"26"}'

post_run \
  'Property Tax Exemptions (enumerated homeowner rules)' \
  '/api/property-tax-exemptions-runs' \
  '{"businessDate":"2025-09-15","businessTime":"12:00:00","homeownerProcessingVariant":"ENUMERATED","idempotencyKey":"modernized-property-tax-exemptions-enumerated-20250915-noon"}'

post_run \
  'Tax Rate Input Preparation' \
  '/api/tax-rate-input-preparation-runs' \
  '{"businessDate":"2025-09-15","businessTime":"12:00:00","idempotencyKey":"modernized-tax-rate-input-20250915-noon"}'

post_run \
  'EIFD/TIF Increment' \
  '/api/eifd-tif-increment-runs' \
  '{"annualEqualizationFactor":"10000","businessDate":"2025-09-15","businessTime":"12:00:00","idempotencyKey":"modernized-eifd-20250915-noon","processingYear":"26","reassessmentControl":"260181202526","reportingYear":"2026"}'
