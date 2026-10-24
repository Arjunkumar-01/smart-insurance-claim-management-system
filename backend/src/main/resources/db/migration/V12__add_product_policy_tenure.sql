ALTER TABLE products ADD COLUMN tenure_months INTEGER;

UPDATE products SET tenure_months = 12 WHERE tenure_months IS NULL;

ALTER TABLE products ALTER COLUMN tenure_months SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT ck_products_tenure_months_positive CHECK (tenure_months > 0);

ALTER TABLE products DROP CONSTRAINT ck_products_category;
ALTER TABLE products ADD CONSTRAINT ck_products_category CHECK (category IN ('HEALTH', 'LIFE', 'VEHICLE', 'TRAVEL'));