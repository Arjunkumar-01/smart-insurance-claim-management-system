CREATE TABLE customers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    phone_number VARCHAR(30) NOT NULL,
    date_of_birth DATE,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE RESTRICT
);

CREATE TABLE customer_kyc (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL UNIQUE,
    identity_type VARCHAR(50) NOT NULL,
    identity_number VARCHAR(100) NOT NULL,
    kyc_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_customer_kyc_status CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT fk_customer_kyc_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT
);

