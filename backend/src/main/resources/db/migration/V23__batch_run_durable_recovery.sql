-- Stop and drain all pre-V23 application instances before applying this migration.
-- Those instances cannot renew leases, so their nonterminal reservations become
-- recovery candidates. This migration is not safe during a mixed-version rollout.

ALTER TABLE batch_runs
    ADD COLUMN state VARCHAR(16),
    ADD COLUMN owner_id VARCHAR(128),
    ADD COLUMN lease_expires_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN recovery_response_json JSONB;

UPDATE batch_runs
SET state = CASE
        WHEN response_json ->> 'status' IN ('COMPLETED', 'FAILED')
            THEN response_json ->> 'status'
        ELSE 'QUEUED'
    END,
    owner_id = CASE
        WHEN response_json ->> 'status' IN ('COMPLETED', 'FAILED') THEN NULL
        ELSE 'pre-lease-deployment'
    END,
    lease_expires_at = CASE
        WHEN response_json ->> 'status' IN ('COMPLETED', 'FAILED') THEN NULL
        ELSE CURRENT_TIMESTAMP
    END,
    recovery_response_json = CASE
        WHEN response_json ->> 'status' IN ('COMPLETED', 'FAILED') THEN response_json
        ELSE jsonb_set(response_json, '{status}', '"FAILED"'::jsonb, true)
    END;

ALTER TABLE batch_runs
    ALTER COLUMN state SET NOT NULL,
    ALTER COLUMN recovery_response_json SET NOT NULL,
    ADD CONSTRAINT ck_batch_runs_state
        CHECK (state IN ('QUEUED', 'RUNNING', 'COMPLETED', 'FAILED')),
    ADD CONSTRAINT ck_batch_runs_lifecycle
        CHECK (
            (state IN ('QUEUED', 'RUNNING') AND owner_id IS NOT NULL AND lease_expires_at IS NOT NULL)
            OR
            (state IN ('COMPLETED', 'FAILED') AND owner_id IS NULL AND lease_expires_at IS NULL)
        );

CREATE INDEX ix_batch_runs_recovery
    ON batch_runs (lease_expires_at)
    WHERE state IN ('QUEUED', 'RUNNING');
