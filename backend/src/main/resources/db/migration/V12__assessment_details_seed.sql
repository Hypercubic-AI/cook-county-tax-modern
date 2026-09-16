-- Deterministic detail occurrences for the ten reviewed ASSESSMENT-MASTER parcels.
-- The values cover supported land and improvement valuation paths while retaining
-- the reviewed parcel volume/property keys and realistic assessed-value splits.

WITH seed (
    parcel_volume_number,
    parcel_number,
    occurrence_number,
    multicode,
    detail_type,
    detail_code,
    assessment_class,
    valuation,
    cdu,
    decimal_scale,
    unit_measure,
    front_footage,
    depth,
    depth_factor,
    corner_factor,
    extra_corner_factor,
    land_condition_factor,
    area,
    unit_price,
    reproduction_cost,
    improvement_year,
    age,
    occupancy_factor,
    condition_factor,
    percent_assessed,
    key_parcel_number,
    split_code
) AS (
    VALUES
        -- Baseline residential: class-200 land and an area-valued class-202 building.
        (1,   10011000010000, 1, 1, '1', '1', 200,  20000, NULL, 1, 'FF', 1000, 120, 1.000, 1.0000, NULL::DECIMAL(5,5), NULL::DECIMAL(3,1), NULL, 200.00, NULL,   NULL, 5, NULL, NULL, NULL,     10011000010000, NULL),
        (1,   10011000010000, 2, 1, '2', '1', 202,  80000, 'AV', 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, 2000,  62.50, NULL,   98, 5, 90.0, 80.0, 80.00000, 10011000010000, 'A'),

        -- Residential renewal: reproduction-cost valuation and a first-year improvement.
        (9,   12022000020000, 1, 1, '1', '1', 200,  25000, NULL, 1, 'FF', 1000, 125, 1.000, 1.0000, NULL, NULL, NULL, 250.00, NULL,   NULL, 8, NULL, NULL, NULL,     12022000020000, NULL),
        (9,   12022000020000, 2, 1, '3', '2', 203, 100000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 125000, 5, 1, 95.0, 80.0, NULL,     12022000020000, 'B'),

        -- Commercial parcel: non-residential land plus a conditioned Type-4 building.
        (25,  13066000060000, 1, 1, '1', '1', 100,  35000, NULL, 1, 'FF', 1000, 140, 1.000, 1.0000, NULL, NULL, NULL, 350.00, NULL,   NULL, 12, NULL, NULL, NULL,    13066000060000, NULL),
        (25,  13066000060000, 2, 1, '4', '3', 201, 140000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 200000, 90, 12, 85.0, 70.0, NULL,    13066000060000, 'C'),

        -- Multifamily parcel: positive-occupancy Type-5 normalization and conversion.
        (36,  14077000070000, 1, 1, '1', '1', 200,  65000, NULL, 1, 'FF', 1000, 130, 1.000, 1.0000, NULL, NULL, NULL, 650.00, NULL,   NULL, 7, NULL, NULL, NULL,     14077000070000, NULL),
        (36,  14077000070000, 2, 1, '5', '4', 212, 260000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 325000, 12, 7, 80.0, 80.0, NULL,     14077000070000, 'D'),

        -- Residential class-295 parcel: area, condition, and assessed-percent factors.
        (63,  20033000030000, 1, 1, '1', '1', 200,  50000, NULL, 1, 'FF', 1000, 150, 1.000, 1.0000, NULL, NULL, NULL, 500.00, NULL,   NULL, 10, NULL, NULL, NULL,    20033000030000, NULL),
        (63,  20033000030000, 2, 1, '2', '1', 295, 200000, 'AV', 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, 4000, 100.00, NULL,   0, 10, 87.5, 80.0, 62.50000, 20033000030000, 'E'),

        -- Agricultural parcel: class-239 land and the paired class-224 improvement.
        (86,  22055000050000, 1, 1, '1', '1', 239, 150000, NULL, 1, 'FF', 2000, 200, 1.000, 1.0000, NULL, NULL, NULL, 750.00, NULL,   NULL, 15, NULL, NULL, NULL,    22055000050000, NULL),
        (86,  22055000050000, 2, 1, '3', '2', 224, 600000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 750000, 85, 15, 90.0, 80.0, NULL,    22055000050000, 'F'),

        -- Exempt parcel: EX land follows the zero-value path; class 299 remains positive.
        (193, 37044000040000, 1, 1, '1', '1', 200,      0, NULL, 1, 'EX', 1000, 100, 1.000, 1.0000, NULL, NULL, NULL, 100.00, NULL,   NULL, 20, NULL, NULL, NULL,    37044000040000, NULL),
        (193, 37044000040000, 2, 1, '4', '3', 299,  50000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 125000, 75, 20, 50.0, 80.0, 50.00000, 37044000040000, 'G'),

        -- Municipal parcel: Type-2 YR cleanup plus class-278 residential bucketing.
        (350, 71011100110000, 1, 1, '1', '1', 200,  16000, NULL, 1, 'FF', 1000, 110, 1.000, 1.0000, NULL, NULL, NULL, 160.00, NULL,   NULL, 6, NULL, NULL, NULL,     71011100110000, NULL),
        (350, 71011100110000, 2, 1, '2', '1', 278,  64000, 'YR', 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, 1000, 100.00, NULL,   18, 6, 70.0, 80.0, 80.00000, 71011100110000, 'H'),

        -- Senior renewal: the class-297 value clears the enumerated threshold.
        (508, 76022200120000, 1, 1, '1', '1', 200,  22000, NULL, 1, 'FF', 1000, 115, 1.000, 1.0000, NULL, NULL, NULL, 220.00, NULL,   NULL, 1, NULL, NULL, NULL,     76022200120000, NULL),
        (508, 76022200120000, 2, 1, '3', '2', 297,  88000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 110000, 24, 1, 80.0, 80.0, NULL,     76022200120000, 'I'),

        -- Senior-freeze parcel: class-288 relief coexists with a class-234 improvement.
        (528, 77033300130000, 1, 1, '1', '1', 200,  17000, NULL, 1, 'FF', 1000, 105, 1.000, 1.0000, NULL, NULL, NULL, 170.00, NULL,   NULL, 9, NULL, NULL, NULL,     77033300130000, NULL),
        (528, 77033300130000, 2, 1, '3', '2', 288,  25000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL, 100000, 24, 9, 75.0, NULL, NULL,     77033300130000, 'J'),
        (528, 77033300130000, 3, 1, '4', '3', 234,  48000, NULL, 0, 'SF', NULL, NULL, NULL,  NULL,   NULL, NULL, NULL, NULL,  80000, 95, 9, 75.0, 80.0, 75.00000, 77033300130000, 'J')
)
INSERT INTO assessment_details (
    parcel_volume_number,
    parcel_number,
    occurrence_number,
    multicode,
    detail_type,
    detail_code,
    assessment_class,
    valuation,
    cdu,
    decimal_scale,
    unit_measure,
    front_footage,
    depth,
    depth_factor,
    corner_factor,
    extra_corner_factor,
    land_condition_factor,
    area,
    unit_price,
    reproduction_cost,
    improvement_year,
    age,
    occupancy_factor,
    condition_factor,
    percent_assessed,
    key_parcel_number,
    split_code
)
SELECT
    seed.parcel_volume_number,
    seed.parcel_number,
    seed.occurrence_number,
    seed.multicode,
    seed.detail_type,
    seed.detail_code,
    seed.assessment_class,
    seed.valuation,
    seed.cdu,
    seed.decimal_scale,
    seed.unit_measure,
    seed.front_footage,
    seed.depth,
    seed.depth_factor,
    seed.corner_factor,
    seed.extra_corner_factor,
    seed.land_condition_factor,
    seed.area,
    seed.unit_price,
    seed.reproduction_cost,
    seed.improvement_year,
    seed.age,
    seed.occupancy_factor,
    seed.condition_factor,
    seed.percent_assessed,
    seed.key_parcel_number,
    seed.split_code
FROM seed
WHERE EXISTS (
    SELECT 1
    FROM assessment_parcels parcel
    WHERE parcel.volume_number = seed.parcel_volume_number
      AND parcel.parcel_number = seed.parcel_number
)
AND NOT EXISTS (
    SELECT 1
    FROM assessment_details existing
    WHERE existing.parcel_volume_number = seed.parcel_volume_number
      AND existing.parcel_number = seed.parcel_number
      AND existing.occurrence_number = seed.occurrence_number
)
ON CONFLICT DO NOTHING;
