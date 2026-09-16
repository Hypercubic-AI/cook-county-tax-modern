CREATE TABLE batch_runs (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    capability VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    fingerprint VARCHAR(512) NOT NULL,
    response_json JSONB NOT NULL,
    CONSTRAINT uq_batch_runs_capability_key UNIQUE (capability, idempotency_key)
);
ALTER SEQUENCE batch_runs_id_seq RESTART WITH 2147483648;

ALTER TABLE assessment_details ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE assessment_parcels ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE agency_equalized_valuations ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE frozen_valuations ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE frozen_agency_adjustments ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE homeowner_exemptions ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE homeowner_masters ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE maintained_homestead_exemptions ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE senior_freeze_applicants ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE senior_freeze_masters ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
