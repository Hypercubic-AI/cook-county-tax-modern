-- Remove only the synthetic detail occurrences introduced by V12. The reviewed
-- legacy ASSESSMENT-MASTER records do not expose separate positive details.
WITH synthetic_keys (
    parcel_volume_number,
    parcel_number,
    occurrence_number
) AS (
    VALUES
        (1,   10011000010000, 1),
        (1,   10011000010000, 2),
        (9,   12022000020000, 1),
        (9,   12022000020000, 2),
        (25,  13066000060000, 1),
        (25,  13066000060000, 2),
        (36,  14077000070000, 1),
        (36,  14077000070000, 2),
        (63,  20033000030000, 1),
        (63,  20033000030000, 2),
        (86,  22055000050000, 1),
        (86,  22055000050000, 2),
        (193, 37044000040000, 1),
        (193, 37044000040000, 2),
        (350, 71011100110000, 1),
        (350, 71011100110000, 2),
        (508, 76022200120000, 1),
        (508, 76022200120000, 2),
        (528, 77033300130000, 1),
        (528, 77033300130000, 2),
        (528, 77033300130000, 3)
)
DELETE FROM assessment_details detail
USING synthetic_keys
WHERE detail.parcel_volume_number = synthetic_keys.parcel_volume_number
  AND detail.parcel_number = synthetic_keys.parcel_number
  AND detail.occurrence_number = synthetic_keys.occurrence_number;
