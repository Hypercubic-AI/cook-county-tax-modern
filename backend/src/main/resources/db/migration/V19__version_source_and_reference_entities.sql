ALTER TABLE assessment_parcel_source_records ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE property_tax_renewals ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE agency_references ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE town_references ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tax_code_master_references ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tax_code_agency_slots ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tax_rate_divisions ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tax_rate_equalized_values ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
