-- Rename storage fields without changing their values or nullability.
ALTER TABLE assessment_details RENAME COLUMN cdu TO supplemental_detail_code;

ALTER TABLE homeowner_masters RENAME COLUMN nphe_amount TO base_year_exemption_amount;
ALTER TABLE homeowner_masters RENAME COLUMN nphe_base_year TO exemption_base_year;
ALTER TABLE homeowner_masters RENAME COLUMN nphe_status TO base_year_establishment_code;

ALTER TABLE maintained_homestead_exemptions RENAME COLUMN nphe_amount TO base_year_exemption_amount;
ALTER TABLE maintained_homestead_exemptions RENAME COLUMN nphe_base_year TO exemption_base_year;
ALTER TABLE maintained_homestead_exemptions RENAME COLUMN nphe_status TO base_year_establishment_code;
