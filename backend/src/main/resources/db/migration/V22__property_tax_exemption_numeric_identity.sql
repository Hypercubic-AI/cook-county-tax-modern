-- Preserve source identifier widths and exact fixed-point values for exemption records.
ALTER TABLE homeowner_masters
    ALTER COLUMN property_number TYPE VARCHAR(15) USING LPAD(property_number::text, 15, '0'),
    ALTER COLUMN volume_number TYPE VARCHAR(3) USING LPAD(volume_number::text, 3, '0'),
    ALTER COLUMN tax_code TYPE VARCHAR(5) USING LPAD(tax_code::text, 5, '0'),
    ALTER COLUMN zip_code TYPE VARCHAR(9) USING CASE WHEN zip_code IS NULL THEN NULL ELSE LPAD(zip_code::text, 9, '0') END,
    ALTER COLUMN assessed_value TYPE NUMERIC(9,0) USING assessed_value::NUMERIC(9,0),
    ALTER COLUMN equalized_value TYPE NUMERIC(9,0) USING equalized_value::NUMERIC(9,0),
    ALTER COLUMN base_year_exemption_amount TYPE NUMERIC(9,0) USING base_year_exemption_amount::NUMERIC(9,0),
    ALTER COLUMN temporary_assessed_value TYPE NUMERIC(9,0) USING temporary_assessed_value::NUMERIC(9,0);

ALTER TABLE homeowner_exemptions
    ALTER COLUMN property_number TYPE VARCHAR(15) USING LPAD(property_number::text, 15, '0'),
    ALTER COLUMN key_parcel_number TYPE VARCHAR(15) USING LPAD(key_parcel_number::text, 15, '0'),
    ALTER COLUMN volume_number TYPE VARCHAR(3) USING LPAD(volume_number::text, 3, '0'),
    ALTER COLUMN tax_code TYPE VARCHAR(5) USING LPAD(tax_code::text, 5, '0'),
    ALTER COLUMN zip_code TYPE VARCHAR(9) USING CASE WHEN zip_code IS NULL THEN NULL ELSE LPAD(zip_code::text, 9, '0') END,
    ALTER COLUMN assessed_value TYPE NUMERIC(9,0) USING assessed_value::NUMERIC(9,0),
    ALTER COLUMN equalized_value TYPE NUMERIC(9,0) USING equalized_value::NUMERIC(9,0);

ALTER TABLE maintained_homestead_exemptions
    ALTER COLUMN property_number TYPE VARCHAR(15) USING LPAD(property_number::text, 15, '0'),
    ALTER COLUMN volume_number TYPE VARCHAR(3) USING LPAD(volume_number::text, 3, '0'),
    ALTER COLUMN tax_code TYPE VARCHAR(5) USING LPAD(tax_code::text, 5, '0'),
    ALTER COLUMN zip_code TYPE VARCHAR(9) USING LPAD(zip_code::text, 9, '0'),
    ALTER COLUMN assessed_value TYPE NUMERIC(9,0) USING assessed_value::NUMERIC(9,0),
    ALTER COLUMN equalized_value TYPE NUMERIC(9,0) USING equalized_value::NUMERIC(9,0),
    ALTER COLUMN base_year_exemption_amount TYPE NUMERIC(9,0) USING base_year_exemption_amount::NUMERIC(9,0);

ALTER TABLE property_tax_renewals
    ALTER COLUMN property_number TYPE VARCHAR(15) USING LPAD(property_number::text, 15, '0'),
    DROP COLUMN matched;

ALTER TABLE senior_freeze_applicants
    ALTER COLUMN applicant_zip_code TYPE VARCHAR(9) USING LPAD(applicant_zip_code::text, 9, '0'),
    ALTER COLUMN phone_number TYPE VARCHAR(10) USING LPAD(phone_number::text, 10, '0'),
    ALTER COLUMN social_security_number TYPE VARCHAR(11) USING LPAD(social_security_number::text, 11, '0'),
    ALTER COLUMN base_year_eligible_equalized_value TYPE NUMERIC(9,0) USING base_year_eligible_equalized_value::NUMERIC(9,0),
    ALTER COLUMN homeowner_base_year_assessed_value TYPE NUMERIC(9,0) USING homeowner_base_year_assessed_value::NUMERIC(9,0),
    ALTER COLUMN homeowner_base_year_equalized_value TYPE NUMERIC(9,0) USING homeowner_base_year_equalized_value::NUMERIC(9,0);

ALTER TABLE senior_freeze_masters
    ALTER COLUMN key_parcel_number TYPE VARCHAR(14) USING LPAD(key_parcel_number::text, 14, '0'),
    ALTER COLUMN mailing_zip_code TYPE VARCHAR(9) USING LPAD(mailing_zip_code::text, 9, '0'),
    ALTER COLUMN base_year_eligible_computed_full_assessed_value TYPE NUMERIC(9,0) USING base_year_eligible_computed_full_assessed_value::NUMERIC(9,0),
    ALTER COLUMN base_year_equalized_value TYPE NUMERIC(9,0) USING base_year_equalized_value::NUMERIC(9,0),
    ALTER COLUMN base_year_full_assessed_value TYPE NUMERIC(9,0) USING base_year_full_assessed_value::NUMERIC(9,0),
    ALTER COLUMN base_year_total_eligible_computed_equalized_value TYPE NUMERIC(9,0) USING base_year_total_eligible_computed_equalized_value::NUMERIC(9,0),
    ALTER COLUMN class288_expiration_assessed_value TYPE NUMERIC(9,0) USING class288_expiration_assessed_value::NUMERIC(9,0),
    ALTER COLUMN class288_expiration_equalized_value TYPE NUMERIC(9,0) USING class288_expiration_equalized_value::NUMERIC(9,0),
    ALTER COLUMN class288_over_limit_assessed_value TYPE NUMERIC(9,0) USING class288_over_limit_assessed_value::NUMERIC(9,0),
    ALTER COLUMN class288_over_limit_equalized_value TYPE NUMERIC(9,0) USING class288_over_limit_equalized_value::NUMERIC(9,0),
    ALTER COLUMN current_year_eligible_computed_assessed_value TYPE NUMERIC(9,0) USING current_year_eligible_computed_assessed_value::NUMERIC(9,0),
    ALTER COLUMN current_year_eligible_computed_equalized_value TYPE NUMERIC(9,0) USING current_year_eligible_computed_equalized_value::NUMERIC(9,0),
    ALTER COLUMN current_year_final_equalized_value_difference TYPE NUMERIC(9,0) USING current_year_final_equalized_value_difference::NUMERIC(9,0),
    ALTER COLUMN current_year_full_assessed_value TYPE NUMERIC(9,0) USING current_year_full_assessed_value::NUMERIC(9,0),
    ALTER COLUMN current_year_full_equalized_value TYPE NUMERIC(9,0) USING current_year_full_equalized_value::NUMERIC(9,0),
    ALTER COLUMN current_year_not_eligible_assessed_value TYPE NUMERIC(9,0) USING current_year_not_eligible_assessed_value::NUMERIC(9,0),
    ALTER COLUMN current_year_not_eligible_equalized_value TYPE NUMERIC(9,0) USING current_year_not_eligible_equalized_value::NUMERIC(9,0),
    ALTER COLUMN original_base_year_eligible_computed_full_assessed_value TYPE NUMERIC(9,0) USING original_base_year_eligible_computed_full_assessed_value::NUMERIC(9,0),
    ALTER COLUMN original_base_year_equalized_value TYPE NUMERIC(9,0) USING original_base_year_equalized_value::NUMERIC(9,0),
    ALTER COLUMN original_base_year_full_assessed_value TYPE NUMERIC(9,0) USING original_base_year_full_assessed_value::NUMERIC(9,0),
    ALTER COLUMN original_base_year_total_eligible_computed_equalized_value TYPE NUMERIC(9,0) USING original_base_year_total_eligible_computed_equalized_value::NUMERIC(9,0),
    ALTER COLUMN original_current_year_final_equalized_value_difference TYPE NUMERIC(9,0) USING original_current_year_final_equalized_value_difference::NUMERIC(9,0);
