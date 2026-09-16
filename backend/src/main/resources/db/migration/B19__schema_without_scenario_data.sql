-- Initializes new deployments without scenario records.
-- Existing Flyway histories continue through versioned migrations unchanged.
-- Only the complete township catalog is initialized as maintained reference data.

CREATE TABLE agency_equalized_valuations (
    id bigint NOT NULL,
    agency_number character varying(9),
    annexed_property_equalized_value bigint,
    burden_percent numeric(5,2),
    connecting_agency1 character varying(9),
    connecting_agency2 character varying(9),
    connecting_agency3 character varying(9),
    connecting_agency4 character varying(9),
    cook_county_air_pollution_value bigint,
    cook_county_railroad_value bigint,
    cook_county_real_estate_value bigint,
    cook_county_use_tax_value bigint,
    de_kalb_county_equalized_value bigint,
    disconnected_property_equalized_value bigint,
    disconnected_tif_difference bigint,
    du_page_county_equalized_value bigint,
    grundy_county_equalized_value bigint,
    kane_county_equalized_value bigint,
    kankakee_county_equalized_value bigint,
    kendall_county_equalized_value bigint,
    la_salle_county_equalized_value bigint,
    lake_county_equalized_value bigint,
    limiting_tax_rate_override numeric(9,6),
    livingston_county_equalized_value bigint,
    mc_henry_county_equalized_value bigint,
    new_property_equalized_value bigint,
    overlap_annexed_property_equalized_value bigint,
    overlap_disconnected_property_equalized_value bigint,
    overlap_disconnected_tif_difference bigint,
    overlap_new_property_equalized_value bigint,
    parent_agency1 character varying(9),
    parent_agency2 character varying(9),
    parent_agency3 character varying(9),
    parent_agency4 character varying(9),
    parent_agency5 character varying(9),
    previous_tax_year1 integer,
    previous_tax_year1_extension numeric(13,2),
    previous_tax_year2 integer,
    previous_tax_year2_extension numeric(13,2),
    previous_tax_year3 integer,
    previous_tax_year3_extension numeric(13,2),
    tax_cap_indicator boolean,
    tax_year integer,
    will_county_equalized_value bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE agency_equalized_valuations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE agency_equalized_valuations_id_seq OWNED BY agency_equalized_valuations.id;

CREATE TABLE agency_references (
    agency_number character varying(9) NOT NULL,
    description character varying(44) NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE assessment_details (
    id bigint NOT NULL,
    age integer,
    area bigint,
    assessment_class integer,
    cdu character varying(2),
    condition_factor numeric(3,1),
    corner_factor numeric(5,4),
    decimal_scale integer,
    depth bigint,
    depth_factor numeric(5,3),
    detail_code character varying(1),
    detail_type character varying(1),
    extra_corner_factor numeric(5,5),
    front_footage bigint,
    improvement_year integer,
    key_parcel_number bigint,
    land_condition_factor numeric(3,1),
    multicode integer,
    occupancy_factor numeric(3,1),
    occurrence_number integer,
    parcel_number bigint,
    parcel_volume_number integer,
    percent_assessed numeric(7,5),
    reproduction_cost bigint,
    split_code character varying(1),
    unit_measure character varying(2),
    unit_price numeric(7,2),
    valuation bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE assessment_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE assessment_details_id_seq OWNED BY assessment_details.id;

CREATE TABLE assessment_parcel_source_records (
    parcel_number bigint NOT NULL,
    source_order integer NOT NULL,
    source_record_base64 text NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE assessment_parcels (
    id bigint NOT NULL,
    archived_pre_conversion_proposed_total bigint,
    assessment_status character varying(1),
    clerk_major_class character varying(1),
    combined_homeowner_non_homeowner_value bigint,
    current_improvement_value bigint,
    current_land_value bigint,
    current_total_value bigint,
    detail_questionnaire_count integer,
    farm_value bigint,
    overall_class integer,
    parcel_number bigint,
    parcel_status character varying(1),
    prior_improvement_value bigint,
    prior_land_value bigint,
    prior_total_value bigint,
    proposed_improvement_value bigint,
    proposed_land_value bigint,
    proposed_total_value bigint,
    sales_segment_count integer,
    tax_code integer,
    tax_type character varying(1),
    volume_number integer,
    prior_parcel_status character varying(1),
    eifd_prior_land_value bigint,
    eifd_prior_improvement_value bigint,
    eifd_prior_total_value bigint,
    eifd_current_land_value bigint,
    eifd_current_improvement_value bigint,
    eifd_current_total_value bigint,
    version bigint DEFAULT 0 NOT NULL,
    property_division_number character varying(14)
);

CREATE SEQUENCE assessment_parcels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE assessment_parcels_id_seq OWNED BY assessment_parcels.id;

CREATE TABLE batch_runs (
    id bigint NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    capability character varying(64) NOT NULL,
    idempotency_key character varying(128) NOT NULL,
    fingerprint character varying(512) NOT NULL,
    response_json jsonb NOT NULL
);

CREATE SEQUENCE batch_runs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE batch_runs_id_seq OWNED BY batch_runs.id;

CREATE TABLE frozen_agency_adjustments (
    id bigint NOT NULL,
    agency_number character varying(9),
    annexed_assessed_value bigint,
    annexed_equalized_value bigint,
    current288_value bigint,
    disconnected_assessed_value bigint,
    disconnected_equalized_value bigint,
    expired288_value bigint,
    expired_incentive_equalized_value bigint,
    expired_incentive_tax_amount numeric(11,2),
    expired_incentive_value bigint,
    first_time_value bigint,
    frozen_equalized_value bigint,
    frozen_tax_amount numeric(15,2),
    tax_code character varying(5),
    tax_rate numeric(6,3),
    tif_current_equalized_value bigint,
    tif_difference_equalized_value bigint,
    tif_prior_frozen_equalized_value bigint,
    total_frozen_value bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE frozen_agency_adjustments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE frozen_agency_adjustments_id_seq OWNED BY frozen_agency_adjustments.id;

CREATE TABLE frozen_valuations (
    id bigint NOT NULL,
    change_action_current_improvement_value bigint,
    change_action_current_land_value bigint,
    change_action_current_parcel_count bigint,
    change_action_current_total_value bigint,
    change_action_prior_improvement_value bigint,
    change_action_prior_land_value bigint,
    change_action_prior_parcel_count bigint,
    change_action_prior_total_value bigint,
    current_improvement_value bigint,
    current_land_value bigint,
    current_parcel_count bigint,
    current_total_value bigint,
    division_number character varying(14),
    no_change_action_current_improvement_value bigint,
    no_change_action_current_land_value bigint,
    no_change_action_current_parcel_count bigint,
    no_change_action_current_total_value bigint,
    no_change_action_prior_improvement_value bigint,
    no_change_action_prior_land_value bigint,
    no_change_action_prior_parcel_count bigint,
    no_change_action_prior_total_value bigint,
    prior_improvement_value bigint,
    prior_land_value bigint,
    prior_parcel_count bigint,
    prior_total_value bigint,
    proposed_actual_value bigint,
    proposed_current288_value bigint,
    proposed_expired288_value bigint,
    proposed_improvement_value bigint,
    proposed_total_value bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE frozen_valuations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE frozen_valuations_id_seq OWNED BY frozen_valuations.id;

CREATE TABLE homeowner_exemptions (
    id bigint NOT NULL,
    application_year integer,
    assessed_value bigint,
    assessment_class integer,
    certificate_of_error_number integer,
    city character varying(12),
    clerks_class integer,
    cooperative_quantity integer,
    eligibility_indicator integer,
    equalization_factor numeric(5,4),
    equalized_value bigint,
    exemption_type integer,
    key_parcel_number bigint,
    mailing_address character varying(22),
    occupancy_factor numeric(5,1),
    owner_name character varying(22),
    property_number bigint,
    proration numeric(7,6),
    record_code integer,
    response_status integer,
    secondary_response_status integer,
    split_code integer,
    state character varying(2),
    tax_code integer,
    tax_type integer,
    tertiary_status integer,
    volume_number integer,
    zip_code bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE homeowner_exemptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE homeowner_exemptions_id_seq OWNED BY homeowner_exemptions.id;

CREATE TABLE homeowner_masters (
    id bigint NOT NULL,
    application_year integer,
    assessed_value bigint,
    assessment_class integer,
    certificate_of_error_number integer,
    city character varying(12),
    cooperative_quantity integer,
    equalization_factor numeric(5,4),
    equalized_value bigint,
    exemption_type integer,
    mailing_address character varying(22),
    nphe_amount bigint,
    nphe_base_year integer,
    nphe_status character varying(2),
    occupancy_factor numeric(5,1),
    owner_name character varying(22),
    property_number bigint,
    proration numeric(7,6),
    response_status integer,
    secondary_response_status integer,
    state character varying(2),
    tax_code integer,
    tax_type integer,
    temporary_assessed_value bigint,
    tertiary_status integer,
    volume_number integer,
    zip_code bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE homeowner_masters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE homeowner_masters_id_seq OWNED BY homeowner_masters.id;

CREATE TABLE maintained_homestead_exemptions (
    id bigint NOT NULL,
    application_year integer,
    assessed_value bigint,
    assessment_class integer,
    certificate_of_error_number integer,
    city character varying(12),
    clerks_class integer,
    cooperative_quantity integer,
    equalization_factor numeric(5,4),
    equalized_value bigint,
    exemption_type integer,
    mailing_address character varying(22),
    nphe_amount bigint,
    nphe_base_year integer,
    nphe_status character varying(2),
    occupancy_factor numeric(5,1),
    owner_name character varying(22),
    property_number bigint,
    proration numeric(7,6),
    response_status integer,
    secondary_response_status integer,
    state character varying(2),
    tax_code integer,
    tax_type character varying(1),
    tertiary_status integer,
    volume_number integer,
    zip_code bigint,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE maintained_homestead_exemptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE maintained_homestead_exemptions_id_seq OWNED BY maintained_homestead_exemptions.id;

CREATE TABLE property_tax_renewals (
    source_order integer NOT NULL,
    property_number bigint NOT NULL,
    batch_number character varying(5) NOT NULL,
    matched boolean NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE senior_freeze_applicants (
    id bigint NOT NULL,
    age integer,
    applicant_address character varying(22),
    applicant_city character varying(12),
    applicant_first_name character varying(15),
    applicant_last_name character varying(20),
    applicant_middle_initial character varying(1),
    applicant_old_name character varying(22),
    applicant_state character varying(2),
    applicant_title character varying(2),
    applicant_zip_code bigint,
    base_year integer,
    base_year_eligible_equalized_value bigint,
    base_year_indicator character varying(1),
    batch_number integer,
    birth_date character varying(8),
    civil_service_benefits numeric(9,2),
    cooperative_senior_shares integer,
    denial_date integer,
    first_application_date integer,
    homeowner_base_year integer,
    homeowner_base_year_assessed_value bigint,
    homeowner_base_year_equalization_factor numeric(5,4),
    homeowner_base_year_equalized_value bigint,
    homeowner_eligibility_indicator integer,
    homeowner_status character varying(1),
    homestead_batch_number integer,
    homestead_percent_shares numeric(6,3),
    homestead_shares integer,
    homestead_status character varying(1),
    homestead_year_applied integer,
    interest_income numeric(9,2),
    last_application_date integer,
    life_care_facility_indicator character varying(1),
    maintenance_indicator integer,
    name_maintenance_indicator integer,
    net_capital_gain numeric(9,2),
    net_rental_income numeric(9,2),
    no_income_indicator character varying(1),
    notarized_indicator character varying(1),
    other_benefits numeric(9,2),
    other_income numeric(9,2),
    percent_senior_shares numeric(6,6),
    phone_number bigint,
    public_aid numeric(9,2),
    qualification_date integer,
    railroad_benefits numeric(9,2),
    returned_date integer,
    senior_freeze_percent numeric(2,1),
    senior_freeze_status character varying(1),
    signed_indicator character varying(1),
    social_security_income numeric(9,2),
    social_security_number bigint,
    total_income numeric(9,2),
    veterans_benefits numeric(9,2),
    wages numeric(9,2),
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE senior_freeze_applicants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE senior_freeze_applicants_id_seq OWNED BY senior_freeze_applicants.id;

CREATE TABLE senior_freeze_masters (
    id bigint NOT NULL,
    base_value_manual_calculation_indicator character varying(1),
    base_value_no_calculation_indicator character varying(1),
    base_value_year integer,
    base_value_year_class integer,
    base_year_eligible_computed_full_assessed_value bigint,
    base_year_equalized_value bigint,
    base_year_full_assessed_value bigint,
    base_year_total_eligible_computed_equalized_value bigint,
    building_shares integer,
    building_units integer,
    calculation_type character varying(1),
    class288_expiration_assessed_value bigint,
    class288_expiration_equalized_value bigint,
    class288_over_limit_assessed_value bigint,
    class288_over_limit_equalized_value bigint,
    current_year_class integer,
    current_year_eligible_computed_assessed_value bigint,
    current_year_eligible_computed_equalized_value bigint,
    current_year_farm_indicator character varying(1),
    current_year_final_equalized_value_difference bigint,
    current_year_full_assessed_value bigint,
    current_year_full_equalized_value bigint,
    current_year_not_eligible_assessed_value bigint,
    current_year_not_eligible_equalized_value bigint,
    homeowner_units integer,
    homestead_units integer,
    key_parcel_number bigint,
    mailing_city character varying(28),
    mailing_direction character varying(2),
    mailing_house_number character varying(5),
    mailing_state character varying(2),
    mailing_street character varying(22),
    mailing_suffix character varying(4),
    mailing_zip_code bigint,
    maintenance_indicator integer,
    master_name character varying(50),
    occupancy_factor numeric(5,1),
    original_base_value_year integer,
    original_base_year_eligible_computed_full_assessed_value bigint,
    original_base_year_equalized_value bigint,
    original_base_year_full_assessed_value bigint,
    original_base_year_total_eligible_computed_equalized_value bigint,
    original_current_year_final_equalized_value_difference bigint,
    original_manual_calculation_indicator character varying(1),
    property_proration numeric(7,6),
    record_code character varying(1),
    senior_freeze_shares integer,
    split_code integer,
    version bigint DEFAULT 0 NOT NULL
);

CREATE SEQUENCE senior_freeze_masters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE senior_freeze_masters_id_seq OWNED BY senior_freeze_masters.id;

CREATE TABLE tax_code_agency_slots (
    tax_code character varying(5) NOT NULL,
    slot_position integer NOT NULL,
    agency_number character varying(9) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT tax_code_agency_slots_slot_position_check CHECK (((slot_position >= 1) AND (slot_position <= 40)))
);

CREATE TABLE tax_code_master_references (
    tax_code character varying(5) NOT NULL,
    tax_rate numeric(7,3) NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE tax_rate_divisions (
    source_order integer NOT NULL,
    volume_number integer NOT NULL,
    parcel_number bigint NOT NULL,
    division_number bigint NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE tax_rate_equalized_values (
    source_order integer NOT NULL,
    volume_number integer NOT NULL,
    parcel_number bigint NOT NULL,
    tax_code integer NOT NULL,
    assessed_value bigint NOT NULL,
    equalized_value bigint NOT NULL,
    tax_type character varying(1) NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

CREATE TABLE town_references (
    town_number character varying(2) NOT NULL,
    name character varying(13) NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);

ALTER TABLE ONLY agency_equalized_valuations ALTER COLUMN id SET DEFAULT nextval('agency_equalized_valuations_id_seq'::regclass);

ALTER TABLE ONLY assessment_details ALTER COLUMN id SET DEFAULT nextval('assessment_details_id_seq'::regclass);

ALTER TABLE ONLY assessment_parcels ALTER COLUMN id SET DEFAULT nextval('assessment_parcels_id_seq'::regclass);

ALTER TABLE ONLY batch_runs ALTER COLUMN id SET DEFAULT nextval('batch_runs_id_seq'::regclass);

ALTER TABLE ONLY frozen_agency_adjustments ALTER COLUMN id SET DEFAULT nextval('frozen_agency_adjustments_id_seq'::regclass);

ALTER TABLE ONLY frozen_valuations ALTER COLUMN id SET DEFAULT nextval('frozen_valuations_id_seq'::regclass);

ALTER TABLE ONLY homeowner_exemptions ALTER COLUMN id SET DEFAULT nextval('homeowner_exemptions_id_seq'::regclass);

ALTER TABLE ONLY homeowner_masters ALTER COLUMN id SET DEFAULT nextval('homeowner_masters_id_seq'::regclass);

ALTER TABLE ONLY maintained_homestead_exemptions ALTER COLUMN id SET DEFAULT nextval('maintained_homestead_exemptions_id_seq'::regclass);

ALTER TABLE ONLY senior_freeze_applicants ALTER COLUMN id SET DEFAULT nextval('senior_freeze_applicants_id_seq'::regclass);

ALTER TABLE ONLY senior_freeze_masters ALTER COLUMN id SET DEFAULT nextval('senior_freeze_masters_id_seq'::regclass);

ALTER TABLE ONLY agency_equalized_valuations
    ADD CONSTRAINT agency_equalized_valuations_agency_number_key UNIQUE (agency_number);

ALTER TABLE ONLY agency_equalized_valuations
    ADD CONSTRAINT agency_equalized_valuations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY agency_references
    ADD CONSTRAINT agency_references_pkey PRIMARY KEY (agency_number);

ALTER TABLE ONLY assessment_details
    ADD CONSTRAINT assessment_details_pkey PRIMARY KEY (id);

ALTER TABLE ONLY assessment_parcel_source_records
    ADD CONSTRAINT assessment_parcel_source_records_pkey PRIMARY KEY (parcel_number);

ALTER TABLE ONLY assessment_parcel_source_records
    ADD CONSTRAINT assessment_parcel_source_records_source_order_key UNIQUE (source_order);

ALTER TABLE ONLY assessment_parcels
    ADD CONSTRAINT assessment_parcels_pkey PRIMARY KEY (id);

ALTER TABLE ONLY batch_runs
    ADD CONSTRAINT batch_runs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY frozen_agency_adjustments
    ADD CONSTRAINT frozen_agency_adjustments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY frozen_valuations
    ADD CONSTRAINT frozen_valuations_division_number_key UNIQUE (division_number);

ALTER TABLE ONLY frozen_valuations
    ADD CONSTRAINT frozen_valuations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY homeowner_exemptions
    ADD CONSTRAINT homeowner_exemptions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY homeowner_masters
    ADD CONSTRAINT homeowner_masters_pkey PRIMARY KEY (id);

ALTER TABLE ONLY maintained_homestead_exemptions
    ADD CONSTRAINT maintained_homestead_exemptions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY property_tax_renewals
    ADD CONSTRAINT property_tax_renewals_pkey PRIMARY KEY (source_order);

ALTER TABLE ONLY senior_freeze_applicants
    ADD CONSTRAINT senior_freeze_applicants_pkey PRIMARY KEY (id);

ALTER TABLE ONLY senior_freeze_masters
    ADD CONSTRAINT senior_freeze_masters_pkey PRIMARY KEY (id);

ALTER TABLE ONLY tax_code_agency_slots
    ADD CONSTRAINT tax_code_agency_slots_pkey PRIMARY KEY (tax_code, slot_position);

ALTER TABLE ONLY tax_code_master_references
    ADD CONSTRAINT tax_code_master_references_pkey PRIMARY KEY (tax_code);

ALTER TABLE ONLY tax_rate_divisions
    ADD CONSTRAINT tax_rate_divisions_pkey PRIMARY KEY (source_order);

ALTER TABLE ONLY tax_rate_equalized_values
    ADD CONSTRAINT tax_rate_equalized_values_pkey PRIMARY KEY (source_order);

ALTER TABLE ONLY town_references
    ADD CONSTRAINT town_references_pkey PRIMARY KEY (town_number);

ALTER TABLE ONLY batch_runs
    ADD CONSTRAINT uq_batch_runs_capability_key UNIQUE (capability, idempotency_key);

ALTER TABLE ONLY tax_code_agency_slots
    ADD CONSTRAINT tax_code_agency_slots_tax_code_fkey FOREIGN KEY (tax_code) REFERENCES tax_code_master_references(tax_code);

INSERT INTO town_references (town_number, name) VALUES
    ('10', 'BARRINGTON'),
    ('11', 'BERWYN'),
    ('12', 'BLOOM'),
    ('13', 'BREMEN'),
    ('14', 'CALUMET'),
    ('15', 'CICERO'),
    ('16', 'ELK GROVE'),
    ('17', 'EVANSTON'),
    ('18', 'HANOVER'),
    ('19', 'LEMONT'),
    ('20', 'LEYDEN'),
    ('21', 'LYONS'),
    ('22', 'MAINE'),
    ('23', 'NEW TRIER'),
    ('24', 'NILES'),
    ('25', 'NORTHFIELD'),
    ('26', 'NORWOOD PARK'),
    ('27', 'OAK PARK'),
    ('28', 'ORLAND'),
    ('29', 'PALATINE'),
    ('30', 'PALOS'),
    ('31', 'PROVISO'),
    ('32', 'RICH'),
    ('33', 'RIVER FOREST'),
    ('34', 'RIVERSIDE'),
    ('35', 'SCHAUMBURG'),
    ('36', 'STICKNEY'),
    ('37', 'THORNTON'),
    ('38', 'WHEELING'),
    ('39', 'WORTH'),
    ('70', 'HYDE PARK'),
    ('71', 'JEFFERSON'),
    ('72', 'LAKE'),
    ('73', 'LAKE VIEW'),
    ('74', 'NORTH'),
    ('75', 'ROGERS PARK'),
    ('76', 'SOUTH'),
    ('77', 'WEST');
