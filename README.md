# Cook County Tax Modernization

The application runs with PostgreSQL, the Spring Boot backend, and the Vite frontend. Run these commands from the repository root.

The backend requires Java 25 and Maven. See [backend quality and source compatibility](docs/backend-quality.md) for verification, Spring composition, nullability, and deployment guidance.

## 1. Start PostgreSQL

```bash
docker compose up --detach db
```

The Compose service publishes PostgreSQL on `localhost:5432`. It creates the `cook-county-tax-rate-making` database with local `postgres` credentials.

## 2. Start the backend

```bash
cd backend
DATABASE_URL=jdbc:postgresql://localhost:5432/cook-county-tax-rate-making \
DATABASE_USERNAME=postgres \
DATABASE_PASSWORD=postgres \
CORS_ALLOWED_ORIGINS=http://localhost:5173 \
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`. `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `CORS_ALLOWED_ORIGINS` are required. `SERVER_PORT`, `BATCH_MAX_CONCURRENT_RUNS`, and `BATCH_RUN_LEASE` have defaults.

Set `SPRING_PROFILES_ACTIVE=factor-comparator` only when the enclosing Isomorphic application needs the internal comparison endpoint and evidence recorder.

Flyway creates the schema on a new database. A new deployment contains maintained township reference data, but it does not contain scenario records. Author scenario records through the enclosing Isomorphic application's fixtures and repository ports. This standalone repository does not contain the application's `rifle/refire` substitutions.

The HTTP examples below remain valid without scenario records. Their output depends on the production or scenario input that you load through the application.

## 3. Start the frontend

In another terminal:

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

`VITE_API_BASE_URL` is optional. The frontend defaults to `http://localhost:8080`.

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
