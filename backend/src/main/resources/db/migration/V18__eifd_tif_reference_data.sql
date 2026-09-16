-- Maintained reference inputs use repository-backed tables.
-- Only the complete township catalog is initialized here.
ALTER TABLE assessment_parcels
    ADD COLUMN property_division_number VARCHAR(14);


CREATE TABLE agency_references (
    agency_number VARCHAR(9) PRIMARY KEY,
    description VARCHAR(44) NOT NULL
);


CREATE TABLE town_references (
    town_number VARCHAR(2) PRIMARY KEY,
    name VARCHAR(13) NOT NULL
);

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

CREATE TABLE tax_code_master_references (
    tax_code VARCHAR(5) PRIMARY KEY,
    tax_rate DECIMAL(7,3) NOT NULL
);


CREATE TABLE tax_code_agency_slots (
    tax_code VARCHAR(5) NOT NULL REFERENCES tax_code_master_references(tax_code),
    slot_position INTEGER NOT NULL CHECK (slot_position BETWEEN 1 AND 40),
    agency_number VARCHAR(9) NOT NULL,
    PRIMARY KEY (tax_code, slot_position)
);

