-- Preserve the reviewed EIFD ASSESSMENT-MASTER values without changing the
-- shared valuation-preparation inputs stored on the same parcel rows.
ALTER TABLE assessment_parcels
    ADD COLUMN eifd_prior_land_value BIGINT,
    ADD COLUMN eifd_prior_improvement_value BIGINT,
    ADD COLUMN eifd_prior_total_value BIGINT,
    ADD COLUMN eifd_current_land_value BIGINT,
    ADD COLUMN eifd_current_improvement_value BIGINT,
    ADD COLUMN eifd_current_total_value BIGINT;

WITH reviewed_values (
    volume_number,
    parcel_number,
    prior_land_value,
    prior_improvement_value,
    prior_total_value,
    current_land_value,
    current_improvement_value,
    current_total_value
) AS (
    VALUES
        (1,   10011000010000,  30000,  70000, 100000,  30000,  70000, 100000),
        (9,   12022000020000,  35000,  90000, 125000,  35000, 105000, 140000),
        (25,  13066000060000,  40000, 135000, 175000,  50000, 125000, 175000),
        (36,  14077000070000,  60000, 140000, 200000,  60000, 165000, 225000),
        (63,  20033000030000,  75000, 175000, 250000,  70000, 160000, 230000),
        (86,  22055000050000, 150000, 175000, 325000, 160000, 205000, 365000),
        (193, 37044000040000,  20000,  30000,  50000,  25000,  45000,  70000),
        (350, 71011100110000,  20000,  60000,  80000,  20000,  55000,  75000),
        (508, 76022200120000,  35000,  75000, 110000,  35000,  75000, 110000),
        (528, 77033300130000,  25000,  65000,  90000,  28000,  72000, 100000)
)
UPDATE assessment_parcels parcel
SET eifd_prior_land_value = reviewed.prior_land_value,
    eifd_prior_improvement_value = reviewed.prior_improvement_value,
    eifd_prior_total_value = reviewed.prior_total_value,
    eifd_current_land_value = reviewed.current_land_value,
    eifd_current_improvement_value = reviewed.current_improvement_value,
    eifd_current_total_value = reviewed.current_total_value
FROM reviewed_values reviewed
WHERE parcel.volume_number = reviewed.volume_number
  AND parcel.parcel_number = reviewed.parcel_number;
