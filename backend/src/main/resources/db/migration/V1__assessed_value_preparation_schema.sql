

CREATE TABLE assessment_details (
    id BIGSERIAL PRIMARY KEY,
    age INTEGER,
    area BIGINT,
    assessment_class INTEGER,
    cdu VARCHAR(2),
    condition_factor DECIMAL(3,1),
    corner_factor DECIMAL(5,4),
    decimal_scale INTEGER,
    depth BIGINT,
    depth_factor DECIMAL(5,3),
    detail_code VARCHAR(1),
    detail_type VARCHAR(1),
    extra_corner_factor DECIMAL(5,5),
    front_footage BIGINT,
    improvement_year INTEGER,
    key_parcel_number BIGINT,
    land_condition_factor DECIMAL(3,1),
    multicode INTEGER,
    occupancy_factor DECIMAL(3,1),
    occurrence_number INTEGER,
    parcel_number BIGINT,
    parcel_volume_number INTEGER,
    percent_assessed DECIMAL(7,5),
    reproduction_cost BIGINT,
    split_code VARCHAR(1),
    unit_measure VARCHAR(2),
    unit_price DECIMAL(7,2),
    valuation BIGINT
);


CREATE TABLE assessment_parcels (
    id BIGSERIAL PRIMARY KEY,
    archived_pre_conversion_proposed_total BIGINT,
    assessment_status VARCHAR(1),
    clerk_major_class VARCHAR(1),
    combined_homeowner_non_homeowner_value BIGINT,
    current_improvement_value BIGINT,
    current_land_value BIGINT,
    current_total_value BIGINT,
    detail_questionnaire_count INTEGER,
    farm_value BIGINT,
    overall_class INTEGER,
    parcel_number BIGINT,
    parcel_status VARCHAR(1),
    prior_improvement_value BIGINT,
    prior_land_value BIGINT,
    prior_total_value BIGINT,
    proposed_improvement_value BIGINT,
    proposed_land_value BIGINT,
    proposed_total_value BIGINT,
    sales_segment_count INTEGER,
    tax_code INTEGER,
    tax_type VARCHAR(1),
    volume_number INTEGER
);



