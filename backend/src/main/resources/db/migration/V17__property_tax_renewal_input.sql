CREATE TABLE property_tax_renewals (
    source_order INTEGER PRIMARY KEY,
    property_number BIGINT NOT NULL,
    batch_number VARCHAR(5) NOT NULL,
    matched BOOLEAN NOT NULL
);

