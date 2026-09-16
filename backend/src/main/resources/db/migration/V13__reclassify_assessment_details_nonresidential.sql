-- The reviewed ASREA852/ASREA853 runs classify all ten seed parcels as
-- non-residential. Move only V12's improvement occurrences into recognized
-- major-9 exempt classes, outside both the enumerated and broad eligibility sets.

WITH class_mapping (
    parcel_volume_number,
    parcel_number,
    occurrence_number,
    prior_class,
    replacement_class
) AS (
    VALUES
        (1,   10011000010000, 2, 202, 913),
        (9,   12022000020000, 2, 203, 914),
        (25,  13066000060000, 2, 201, 915),
        (36,  14077000070000, 2, 212, 918),
        (63,  20033000030000, 2, 295, 919),
        (86,  22055000050000, 2, 224, 920),
        (193, 37044000040000, 2, 299, 921),
        (350, 71011100110000, 2, 278, 996),
        (508, 76022200120000, 2, 297, 959),
        (528, 77033300130000, 2, 288, 991),
        (528, 77033300130000, 3, 234, 913)
)
UPDATE assessment_details detail
SET assessment_class = class_mapping.replacement_class
FROM class_mapping
WHERE detail.parcel_volume_number = class_mapping.parcel_volume_number
  AND detail.parcel_number = class_mapping.parcel_number
  AND detail.occurrence_number = class_mapping.occurrence_number
  AND detail.assessment_class = class_mapping.prior_class
  AND detail.detail_type IN ('2', '3', '4', '5');
