CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(20) NOT NULL,
    coverage_description VARCHAR(2000) NOT NULL,
    coverage_amount NUMERIC(19, 2) NOT NULL,
    premium_amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_products_category CHECK (category IN ('HEALTH', 'LIFE', 'VEHICLE')),
    CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_products_coverage_amount_positive CHECK (coverage_amount > 0),
    CONSTRAINT ck_products_premium_amount_positive CHECK (premium_amount > 0)
);

CREATE INDEX ix_products_category_status ON products (category, status);