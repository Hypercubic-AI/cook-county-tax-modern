-- Preserve source identifier widths and fixed-point precision without changing numeric values.

ALTER TABLE assessment_parcels
    ALTER COLUMN parcel_number TYPE VARCHAR(16)
    USING CASE WHEN parcel_number IS NULL THEN NULL WHEN parcel_number < 0 THEN '-' || LPAD(ABS(parcel_number)::TEXT, 15, '0') ELSE LPAD(parcel_number::TEXT, 15, '0') END;

ALTER TABLE assessment_parcels
    ALTER COLUMN volume_number TYPE VARCHAR(4)
    USING CASE WHEN volume_number IS NULL THEN NULL WHEN volume_number < 0 THEN '-' || LPAD(ABS(volume_number)::TEXT, 3, '0') ELSE LPAD(volume_number::TEXT, 3, '0') END;

ALTER TABLE assessment_parcels
    ALTER COLUMN tax_code TYPE VARCHAR(6)
    USING CASE WHEN tax_code IS NULL THEN NULL WHEN tax_code < 0 THEN '-' || LPAD(ABS(tax_code)::TEXT, 5, '0') ELSE LPAD(tax_code::TEXT, 5, '0') END;

ALTER TABLE assessment_details
    ALTER COLUMN parcel_number TYPE VARCHAR(16)
    USING CASE WHEN parcel_number IS NULL THEN NULL WHEN parcel_number < 0 THEN '-' || LPAD(ABS(parcel_number)::TEXT, 15, '0') ELSE LPAD(parcel_number::TEXT, 15, '0') END;

ALTER TABLE assessment_details
    ALTER COLUMN parcel_volume_number TYPE VARCHAR(4)
    USING CASE WHEN parcel_volume_number IS NULL THEN NULL WHEN parcel_volume_number < 0 THEN '-' || LPAD(ABS(parcel_volume_number)::TEXT, 3, '0') ELSE LPAD(parcel_volume_number::TEXT, 3, '0') END;

ALTER TABLE assessment_details
    ALTER COLUMN key_parcel_number TYPE VARCHAR(16)
    USING CASE WHEN key_parcel_number IS NULL THEN NULL WHEN key_parcel_number < 0 THEN '-' || LPAD(ABS(key_parcel_number)::TEXT, 15, '0') ELSE LPAD(key_parcel_number::TEXT, 15, '0') END;

ALTER TABLE assessment_parcel_source_records
    ALTER COLUMN parcel_number TYPE VARCHAR(16)
    USING CASE WHEN parcel_number IS NULL THEN NULL WHEN parcel_number < 0 THEN '-' || LPAD(ABS(parcel_number)::TEXT, 15, '0') ELSE LPAD(parcel_number::TEXT, 15, '0') END;

ALTER TABLE assessment_parcels
    ALTER COLUMN archived_pre_conversion_proposed_total TYPE NUMERIC(9,0)
    USING archived_pre_conversion_proposed_total::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN combined_homeowner_non_homeowner_value TYPE NUMERIC(9,0)
    USING combined_homeowner_non_homeowner_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN current_improvement_value TYPE NUMERIC(9,0)
    USING current_improvement_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN current_land_value TYPE NUMERIC(9,0)
    USING current_land_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN current_total_value TYPE NUMERIC(9,0)
    USING current_total_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN farm_value TYPE NUMERIC(9,0)
    USING farm_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN prior_improvement_value TYPE NUMERIC(9,0)
    USING prior_improvement_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN prior_land_value TYPE NUMERIC(9,0)
    USING prior_land_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN prior_total_value TYPE NUMERIC(9,0)
    USING prior_total_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN proposed_improvement_value TYPE NUMERIC(9,0)
    USING proposed_improvement_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN proposed_land_value TYPE NUMERIC(9,0)
    USING proposed_land_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN proposed_total_value TYPE NUMERIC(9,0)
    USING proposed_total_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN eifd_prior_land_value TYPE NUMERIC(9,0)
    USING eifd_prior_land_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN eifd_prior_improvement_value TYPE NUMERIC(9,0)
    USING eifd_prior_improvement_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN eifd_prior_total_value TYPE NUMERIC(9,0)
    USING eifd_prior_total_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN eifd_current_land_value TYPE NUMERIC(9,0)
    USING eifd_current_land_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN eifd_current_improvement_value TYPE NUMERIC(9,0)
    USING eifd_current_improvement_value::NUMERIC(9,0);

ALTER TABLE assessment_parcels
    ALTER COLUMN eifd_current_total_value TYPE NUMERIC(9,0)
    USING eifd_current_total_value::NUMERIC(9,0);

ALTER TABLE assessment_details
    ALTER COLUMN reproduction_cost TYPE NUMERIC(9,0)
    USING reproduction_cost::NUMERIC(9,0);

ALTER TABLE assessment_details
    ALTER COLUMN valuation TYPE NUMERIC(9,0)
    USING valuation::NUMERIC(9,0);

ALTER TABLE tax_rate_equalized_values
    ALTER COLUMN volume_number TYPE VARCHAR(3)
    USING CASE WHEN volume_number IS NULL THEN NULL ELSE LPAD(volume_number::TEXT, 3, '0') END;

ALTER TABLE tax_rate_equalized_values
    ALTER COLUMN parcel_number TYPE VARCHAR(15)
    USING CASE WHEN parcel_number IS NULL THEN NULL ELSE LPAD(parcel_number::TEXT, 15, '0') END;

ALTER TABLE tax_rate_equalized_values
    ALTER COLUMN tax_code TYPE VARCHAR(5)
    USING CASE WHEN tax_code IS NULL THEN NULL ELSE LPAD(tax_code::TEXT, 5, '0') END;

ALTER TABLE tax_rate_divisions
    ALTER COLUMN volume_number TYPE VARCHAR(3)
    USING CASE WHEN volume_number IS NULL THEN NULL ELSE LPAD(volume_number::TEXT, 3, '0') END;

ALTER TABLE tax_rate_divisions
    ALTER COLUMN parcel_number TYPE VARCHAR(15)
    USING CASE WHEN parcel_number IS NULL THEN NULL ELSE LPAD(parcel_number::TEXT, 15, '0') END;

ALTER TABLE tax_rate_divisions
    ALTER COLUMN division_number TYPE VARCHAR(14)
    USING CASE WHEN division_number IS NULL THEN NULL ELSE LPAD(division_number::TEXT, 14, '0') END;

ALTER TABLE tax_rate_equalized_values
    ALTER COLUMN assessed_value TYPE NUMERIC(11,0)
    USING assessed_value::NUMERIC(11,0);

ALTER TABLE tax_rate_equalized_values
    ALTER COLUMN equalized_value TYPE NUMERIC(11,0)
    USING equalized_value::NUMERIC(11,0);

UPDATE frozen_valuations
SET division_number = LPAD(division_number, 14, '0')
WHERE division_number IS NOT NULL AND CHAR_LENGTH(division_number) < 14;

UPDATE frozen_agency_adjustments
SET tax_code = LPAD(tax_code, 5, '0')
WHERE tax_code IS NOT NULL AND CHAR_LENGTH(tax_code) < 5;

UPDATE frozen_agency_adjustments
SET agency_number = LPAD(agency_number, 9, '0')
WHERE agency_number IS NOT NULL AND CHAR_LENGTH(agency_number) < 9;

UPDATE agency_equalized_valuations
SET agency_number = LPAD(agency_number, 9, '0')
WHERE agency_number IS NOT NULL AND CHAR_LENGTH(agency_number) < 9;

ALTER TABLE frozen_valuations
    ALTER COLUMN change_action_current_improvement_value TYPE NUMERIC(13,0)
    USING change_action_current_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN change_action_current_land_value TYPE NUMERIC(13,0)
    USING change_action_current_land_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN change_action_current_total_value TYPE NUMERIC(13,0)
    USING change_action_current_total_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN change_action_prior_improvement_value TYPE NUMERIC(13,0)
    USING change_action_prior_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN change_action_prior_land_value TYPE NUMERIC(13,0)
    USING change_action_prior_land_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN change_action_prior_total_value TYPE NUMERIC(13,0)
    USING change_action_prior_total_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN current_improvement_value TYPE NUMERIC(13,0)
    USING current_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN current_land_value TYPE NUMERIC(13,0)
    USING current_land_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN current_total_value TYPE NUMERIC(13,0)
    USING current_total_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN no_change_action_current_improvement_value TYPE NUMERIC(13,0)
    USING no_change_action_current_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN no_change_action_current_land_value TYPE NUMERIC(13,0)
    USING no_change_action_current_land_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN no_change_action_current_total_value TYPE NUMERIC(13,0)
    USING no_change_action_current_total_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN no_change_action_prior_improvement_value TYPE NUMERIC(13,0)
    USING no_change_action_prior_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN no_change_action_prior_land_value TYPE NUMERIC(13,0)
    USING no_change_action_prior_land_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN no_change_action_prior_total_value TYPE NUMERIC(13,0)
    USING no_change_action_prior_total_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN prior_improvement_value TYPE NUMERIC(13,0)
    USING prior_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN prior_land_value TYPE NUMERIC(13,0)
    USING prior_land_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN prior_total_value TYPE NUMERIC(13,0)
    USING prior_total_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN proposed_actual_value TYPE NUMERIC(13,0)
    USING proposed_actual_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN proposed_current288_value TYPE NUMERIC(13,0)
    USING proposed_current288_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN proposed_expired288_value TYPE NUMERIC(13,0)
    USING proposed_expired288_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN proposed_improvement_value TYPE NUMERIC(13,0)
    USING proposed_improvement_value::NUMERIC(13,0);

ALTER TABLE frozen_valuations
    ALTER COLUMN proposed_total_value TYPE NUMERIC(13,0)
    USING proposed_total_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN annexed_assessed_value TYPE NUMERIC(13,0)
    USING annexed_assessed_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN annexed_equalized_value TYPE NUMERIC(13,0)
    USING annexed_equalized_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN current288_value TYPE NUMERIC(13,0)
    USING current288_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN disconnected_assessed_value TYPE NUMERIC(13,0)
    USING disconnected_assessed_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN disconnected_equalized_value TYPE NUMERIC(13,0)
    USING disconnected_equalized_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN expired288_value TYPE NUMERIC(13,0)
    USING expired288_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN expired_incentive_equalized_value TYPE NUMERIC(11,0)
    USING expired_incentive_equalized_value::NUMERIC(11,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN expired_incentive_value TYPE NUMERIC(11,0)
    USING expired_incentive_value::NUMERIC(11,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN first_time_value TYPE NUMERIC(13,0)
    USING first_time_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN frozen_equalized_value TYPE NUMERIC(13,0)
    USING frozen_equalized_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN tif_current_equalized_value TYPE NUMERIC(13,0)
    USING tif_current_equalized_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN tif_difference_equalized_value TYPE NUMERIC(13,0)
    USING tif_difference_equalized_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN tif_prior_frozen_equalized_value TYPE NUMERIC(13,0)
    USING tif_prior_frozen_equalized_value::NUMERIC(13,0);

ALTER TABLE frozen_agency_adjustments
    ALTER COLUMN total_frozen_value TYPE NUMERIC(13,0)
    USING total_frozen_value::NUMERIC(13,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN annexed_property_equalized_value TYPE NUMERIC(11,0)
    USING annexed_property_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN cook_county_air_pollution_value TYPE NUMERIC(11,0)
    USING cook_county_air_pollution_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN cook_county_railroad_value TYPE NUMERIC(11,0)
    USING cook_county_railroad_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN cook_county_real_estate_value TYPE NUMERIC(13,0)
    USING cook_county_real_estate_value::NUMERIC(13,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN cook_county_use_tax_value TYPE NUMERIC(11,0)
    USING cook_county_use_tax_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN de_kalb_county_equalized_value TYPE NUMERIC(11,0)
    USING de_kalb_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN disconnected_property_equalized_value TYPE NUMERIC(11,0)
    USING disconnected_property_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN disconnected_tif_difference TYPE NUMERIC(11,0)
    USING disconnected_tif_difference::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN du_page_county_equalized_value TYPE NUMERIC(11,0)
    USING du_page_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN grundy_county_equalized_value TYPE NUMERIC(11,0)
    USING grundy_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN kane_county_equalized_value TYPE NUMERIC(11,0)
    USING kane_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN kankakee_county_equalized_value TYPE NUMERIC(11,0)
    USING kankakee_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN kendall_county_equalized_value TYPE NUMERIC(11,0)
    USING kendall_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN la_salle_county_equalized_value TYPE NUMERIC(11,0)
    USING la_salle_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN lake_county_equalized_value TYPE NUMERIC(11,0)
    USING lake_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN livingston_county_equalized_value TYPE NUMERIC(11,0)
    USING livingston_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN mc_henry_county_equalized_value TYPE NUMERIC(11,0)
    USING mc_henry_county_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN new_property_equalized_value TYPE NUMERIC(11,0)
    USING new_property_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN overlap_annexed_property_equalized_value TYPE NUMERIC(11,0)
    USING overlap_annexed_property_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN overlap_disconnected_property_equalized_value TYPE NUMERIC(11,0)
    USING overlap_disconnected_property_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN overlap_disconnected_tif_difference TYPE NUMERIC(11,0)
    USING overlap_disconnected_tif_difference::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN overlap_new_property_equalized_value TYPE NUMERIC(11,0)
    USING overlap_new_property_equalized_value::NUMERIC(11,0);

ALTER TABLE agency_equalized_valuations
    ALTER COLUMN will_county_equalized_value TYPE NUMERIC(11,0)
    USING will_county_equalized_value::NUMERIC(11,0);

ALTER TABLE assessment_parcels
    ADD CONSTRAINT ck_assessment_parcels_parcel_number_source_width CHECK (parcel_number ~ '^-?[0-9]{15}$');

ALTER TABLE assessment_parcels
    ADD CONSTRAINT ck_assessment_parcels_volume_number_source_width CHECK (volume_number ~ '^-?[0-9]{3}$');

ALTER TABLE assessment_parcels
    ADD CONSTRAINT ck_assessment_parcels_tax_code_source_width CHECK (tax_code ~ '^-?[0-9]{5}$');

ALTER TABLE assessment_details
    ADD CONSTRAINT ck_assessment_details_parcel_number_source_width CHECK (parcel_number ~ '^-?[0-9]{15}$');

ALTER TABLE assessment_details
    ADD CONSTRAINT ck_assessment_details_parcel_volume_number_source_width CHECK (parcel_volume_number ~ '^-?[0-9]{3}$');

ALTER TABLE assessment_details
    ADD CONSTRAINT ck_assessment_details_key_parcel_number_source_width CHECK (key_parcel_number ~ '^-?[0-9]{15}$');

ALTER TABLE assessment_parcel_source_records
    ADD CONSTRAINT ck_assessment_parcel_source_records_parcel_number_source_width CHECK (parcel_number ~ '^-?[0-9]{15}$');

ALTER TABLE tax_rate_equalized_values
    ADD CONSTRAINT ck_tax_rate_equalized_values_volume_number_source_width CHECK (volume_number ~ '^[0-9]{3}$');

ALTER TABLE tax_rate_equalized_values
    ADD CONSTRAINT ck_tax_rate_equalized_values_parcel_number_source_width CHECK (parcel_number ~ '^[0-9]{15}$');

ALTER TABLE tax_rate_equalized_values
    ADD CONSTRAINT ck_tax_rate_equalized_values_tax_code_source_width CHECK (tax_code ~ '^[0-9]{5}$');

ALTER TABLE tax_rate_divisions
    ADD CONSTRAINT ck_tax_rate_divisions_volume_number_source_width CHECK (volume_number ~ '^[0-9]{3}$');

ALTER TABLE tax_rate_divisions
    ADD CONSTRAINT ck_tax_rate_divisions_parcel_number_source_width CHECK (parcel_number ~ '^[0-9]{15}$');

ALTER TABLE tax_rate_divisions
    ADD CONSTRAINT ck_tax_rate_divisions_division_number_source_width CHECK (division_number ~ '^[0-9]{14}$');

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_division_number_source_width CHECK (division_number ~ '^[0-9]{14}$');

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_change_action_current_parcel_count_source_precision
    CHECK (change_action_current_parcel_count BETWEEN -9999999999999 AND 9999999999999);

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_change_action_prior_parcel_count_source_precision
    CHECK (change_action_prior_parcel_count BETWEEN -9999999999999 AND 9999999999999);

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_current_parcel_count_source_precision
    CHECK (current_parcel_count BETWEEN -9999999999999 AND 9999999999999);

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_no_change_action_current_parcel_count_source_precision
    CHECK (no_change_action_current_parcel_count BETWEEN -9999999999999 AND 9999999999999);

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_no_change_action_prior_parcel_count_source_precision
    CHECK (no_change_action_prior_parcel_count BETWEEN -9999999999999 AND 9999999999999);

ALTER TABLE frozen_valuations
    ADD CONSTRAINT ck_frozen_valuations_prior_parcel_count_source_precision
    CHECK (prior_parcel_count BETWEEN -9999999999999 AND 9999999999999);

ALTER TABLE frozen_agency_adjustments
    ADD CONSTRAINT ck_frozen_agency_adjustments_tax_code_source_width CHECK (tax_code ~ '^[0-9]{5}$');

ALTER TABLE frozen_agency_adjustments
    ADD CONSTRAINT ck_frozen_agency_adjustments_agency_number_source_width CHECK (agency_number ~ '^[0-9]{9}$');

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_agency_number_source_width CHECK (agency_number ~ '^[0-9]{9}$');

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_annexed_property_equalized_value_unsigned CHECK (annexed_property_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_cook_county_air_pollution_value_unsigned CHECK (cook_county_air_pollution_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_cook_county_railroad_value_unsigned CHECK (cook_county_railroad_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_cook_county_real_estate_value_unsigned CHECK (cook_county_real_estate_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_cook_county_use_tax_value_unsigned CHECK (cook_county_use_tax_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_de_kalb_county_equalized_value_unsigned CHECK (de_kalb_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_disconnected_property_equalized_value_unsigned CHECK (disconnected_property_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_disconnected_tif_difference_unsigned CHECK (disconnected_tif_difference >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_du_page_county_equalized_value_unsigned CHECK (du_page_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_grundy_county_equalized_value_unsigned CHECK (grundy_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_kane_county_equalized_value_unsigned CHECK (kane_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_kankakee_county_equalized_value_unsigned CHECK (kankakee_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_kendall_county_equalized_value_unsigned CHECK (kendall_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_la_salle_county_equalized_value_unsigned CHECK (la_salle_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_lake_county_equalized_value_unsigned CHECK (lake_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_livingston_county_equalized_value_unsigned CHECK (livingston_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_mc_henry_county_equalized_value_unsigned CHECK (mc_henry_county_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_new_property_equalized_value_unsigned CHECK (new_property_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_overlap_annexed_property_equalized_value_unsigned CHECK (overlap_annexed_property_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_overlap_disconnected_property_equalized_value_unsigned CHECK (overlap_disconnected_property_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_overlap_disconnected_tif_difference_unsigned CHECK (overlap_disconnected_tif_difference >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_overlap_new_property_equalized_value_unsigned CHECK (overlap_new_property_equalized_value >= 0);

ALTER TABLE agency_equalized_valuations
    ADD CONSTRAINT ck_agency_equalized_valuations_will_county_equalized_value_unsigned CHECK (will_county_equalized_value >= 0);

CREATE INDEX ix_assessment_details_source_parcel
    ON assessment_details (parcel_volume_number, parcel_number, occurrence_number);
