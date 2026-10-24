CREATE TABLE policies (
    id UUID PRIMARY KEY,
    policy_number VARCHAR(40) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    product_id UUID NOT NULL,
    premium_amount NUMERIC(19, 2) NOT NULL,
    coverage_amount NUMERIC(19, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_policies_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_policies_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT ck_policies_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT ck_policies_premium_positive CHECK (premium_amount > 0),
    CONSTRAINT ck_policies_coverage_positive CHECK (coverage_amount > 0),
    CONSTRAINT ck_policies_dates_valid CHECK (end_date > start_date)
);

CREATE INDEX ix_policies_customer_created ON policies (customer_id, created_at);
CREATE INDEX ix_policies_product_id ON policies (product_id);
CREATE INDEX ix_policies_status ON policies (status);

CREATE TABLE policy_nominees (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    relationship VARCHAR(80) NOT NULL,
    contact_number VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_policy_nominees_policy FOREIGN KEY (policy_id) REFERENCES policies (id) ON DELETE RESTRICT
);

CREATE TABLE policy_cancellation_requests (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(1000),
    decision_comment VARCHAR(1000),
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_policy_cancellation_policy FOREIGN KEY (policy_id) REFERENCES policies (id) ON DELETE RESTRICT,
    CONSTRAINT ck_policy_cancellation_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX ix_policy_cancellation_status_requested ON policy_cancellation_requests (status, requested_at);
CREATE INDEX ix_policy_cancellation_policy ON policy_cancellation_requests (policy_id);