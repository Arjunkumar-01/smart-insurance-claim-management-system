CREATE TABLE claims (
    id UUID PRIMARY KEY,
    claim_number VARCHAR(40) NOT NULL UNIQUE,
    policy_id UUID NOT NULL,
    claim_amount NUMERIC(19, 2) NOT NULL,
    incident_date DATE NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_claims_policy FOREIGN KEY (policy_id) REFERENCES policies (id) ON DELETE RESTRICT,
    CONSTRAINT ck_claims_status CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'DOCUMENTS_REQUIRED', 'APPROVED', 'REJECTED', 'SETTLED')),
    CONSTRAINT ck_claims_amount_positive CHECK (claim_amount > 0)
);

CREATE INDEX ix_claims_policy_submitted ON claims (policy_id, submitted_at);
CREATE INDEX ix_claims_status ON claims (status);