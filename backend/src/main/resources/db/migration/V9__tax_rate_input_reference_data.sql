-- Tax-rate preparation reads PS.EQUALVAL and PS.DIVSION, which are distinct
-- source records from the assessment-master data persisted by V1/V2.

CREATE TABLE tax_rate_equalized_values (
    source_order INTEGER PRIMARY KEY,
    volume_number INTEGER NOT NULL,
    parcel_number BIGINT NOT NULL,
    tax_code INTEGER NOT NULL,
    assessed_value BIGINT NOT NULL,
    equalized_value BIGINT NOT NULL,
    tax_type VARCHAR(1) NOT NULL
);

CREATE TABLE tax_rate_divisions (
    source_order INTEGER PRIMARY KEY,
    volume_number INTEGER NOT NULL,
    parcel_number BIGINT NOT NULL,
    division_number BIGINT NOT NULL
);

INSERT INTO tax_rate_equalized_values
    (source_order, volume_number, parcel_number, tax_code, assessed_value, equalized_value, tax_type)
VALUES
    (1, 1, 10011000010000, 10001, 100000, 100000, '0'),
    (2, 2, 12022000020000, 12001, 125000, 101000, '0'),
    (3, 3, 13066000060000, 13001, 175000, 102000, '1'),
    (4, 4, 14077000070000, 14001, 325000, 103000, '2'),
    (5, 5, 20033000030000, 20001, 250000, 104000, '0'),
    (6, 6, 22055000050000, 22001, 750000, 105000, '3'),
    (7, 7, 37044000040000, 37001, 50000, 106000, '0'),
    (8, 10, 71011100110000, 71001, 80000, 107000, '5'),
    (9, 11, 76022200120000, 76001, 110000, 108000, '0'),
    (10, 12, 77033300130000, 77001, 90000, 109000, '0');

INSERT INTO tax_rate_divisions
    (source_order, volume_number, parcel_number, division_number)
VALUES
    (1, 1, 10011000010000, 1000001),
    (2, 2, 12022000020000, 1000002),
    (3, 3, 13066000060000, 1000003),
    (4, 4, 14077000070000, 1000004),
    (5, 5, 20033000030000, 1000005),
    (6, 6, 22055000050000, 1000006),
    (7, 7, 37044000040000, 1000007),
    (8, 10, 71011100110000, 1000008),
    (9, 11, 76022200120000, 1000009),
    (10, 12, 77033300130000, 1000010);
