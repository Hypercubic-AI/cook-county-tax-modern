

CREATE TABLE frozen_agency_adjustments (
    id BIGSERIAL PRIMARY KEY,
    agency_number VARCHAR(9),
    annexed_assessed_value BIGINT,
    annexed_equalized_value BIGINT,
    current288_value BIGINT,
    disconnected_assessed_value BIGINT,
    disconnected_equalized_value BIGINT,
    expired288_value BIGINT,
    expired_incentive_equalized_value BIGINT,
    expired_incentive_tax_amount DECIMAL(11,2),
    expired_incentive_value BIGINT,
    first_time_value BIGINT,
    frozen_equalized_value BIGINT,
    frozen_tax_amount DECIMAL(15,2),
    tax_code VARCHAR(5),
    tax_rate DECIMAL(6,3),
    tif_current_equalized_value BIGINT,
    tif_difference_equalized_value BIGINT,
    tif_prior_frozen_equalized_value BIGINT,
    total_frozen_value BIGINT
);



