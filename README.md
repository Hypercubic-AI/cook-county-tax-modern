# Cook County Tax Modernization

The modern application runs with Postgres, the Spring Boot backend, and the Vite frontend. Run the commands below from this `modernized` directory unless a step changes directories.

## 1. Start Postgres

```bash
docker compose up --detach db
```

The Compose service publishes Postgres on `localhost:5432` with database `cook-county-tax-rate-making` and the local-development `postgres`/`postgres` credentials.

## 2. Start the backend and load seed data

```bash
cd backend
SPRING_PROFILES_ACTIVE=factor-comparator \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cook-county-tax-rate-making \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`. On a clean database, Flyway creates the schema and loads deterministic fixtures in migrations V2, V4, V6, and V8 through V15. The fixtures contain exactly ten rows in each populated collection below; assessment details is intentionally empty after V14:

- assessment parcels and preserved assessment source records, with no separate assessment detail occurrences;
- homeowner masters, homeowner exemptions, maintained homestead exemptions, Senior Freeze masters, and Senior Freeze applicants;
- frozen agency adjustments, frozen valuations, and agency equalized valuations;
- tax-rate equalized values and tax-rate divisions.

The records preserve the reviewed parcel, owner, class, tax-type, status, division, and agency variations used by the accepted bullets.

To discard mutated local data and recreate a clean seed database, first stop the backend, then run:

```bash
./scripts/reset-seed.sh --confirm
```

This removes the local Compose Postgres volume and starts an empty database. Restart the backend afterward so Flyway recreates and seeds it. The script never connects to Postgres directly and refuses to delete the volume without `--confirm`.

## 3. Start the frontend

In another terminal:

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

`VITE_API_BASE_URL` is optional; the frontend defaults to `http://localhost:8080`.

## 4. Run the accepted batches

The requests use stable controls and idempotency keys. `curl --fail-with-body` makes every command fail on an HTTP error.

### Assessed Value Preparation

```bash
curl --fail-with-body --silent --show-error \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{"businessDate":"2025-09-15","businessTime":"12:00:00","idempotencyKey":"modernized-assessed-value-20250915-noon","processYear":"26"}' \
  http://localhost:8080/api/assessed-value-preparation-runs
```

### Property Tax Exemptions

```bash
curl --fail-with-body --silent --show-error \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{"businessDate":"2025-09-15","businessTime":"12:00:00","homeownerProcessingVariant":"ENUMERATED","idempotencyKey":"modernized-property-tax-exemptions-enumerated-20250915-noon"}' \
  http://localhost:8080/api/property-tax-exemptions-runs
```

### Tax Rate Input Preparation

```bash
curl --fail-with-body --silent --show-error \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{"businessDate":"2025-09-15","businessTime":"12:00:00","idempotencyKey":"modernized-tax-rate-input-20250915-noon"}' \
  http://localhost:8080/api/tax-rate-input-preparation-runs
```

### EIFD/TIF Increment

```bash
curl --fail-with-body --silent --show-error \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{"annualEqualizationFactor":"10000","businessDate":"2025-09-15","businessTime":"12:00:00","idempotencyKey":"modernized-eifd-20250915-noon","processingYear":"26","reassessmentControl":"260181202526","reportingYear":"2026"}' \
  http://localhost:8080/api/eifd-tif-increment-runs
```

To submit all four requests in this order, with `BASE_URL` defaulting to `http://localhost:8080`, run:

```bash
./scripts/run-batches.sh
```

Each POST returns a run resource. Use the returned `Location` path with `GET http://localhost:8080<location>` to observe final status, return code, reconciliation, outputs, and messages.
