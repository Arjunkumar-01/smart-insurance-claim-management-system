CREATE TABLE claim_settlements (
    id UUID PRIMARY KEY,
    settlement_number VARCHAR(40) NOT NULL UNIQUE,
    claim_id UUID NOT NULL UNIQUE,
    settlement_amount NUMERIC(19, 2) NOT NULL,
    settled_by_user_id UUID NOT NULL,
    settled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_claim_settlements_claim FOREIGN KEY (claim_id) REFERENCES claims (id) ON DELETE RESTRICT,
    CONSTRAINT fk_claim_settlements_user FOREIGN KEY (settled_by_user_id) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_claim_settlements_amount_positive CHECK (settlement_amount > 0)
);

CREATE INDEX ix_claim_settlements_settled_by ON claim_settlements (settled_by_user_id);