CREATE TABLE claim_reviews (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL,
    reviewed_by_user_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_claim_reviews_claim FOREIGN KEY (claim_id) REFERENCES claims (id) ON DELETE RESTRICT,
    CONSTRAINT fk_claim_reviews_user FOREIGN KEY (reviewed_by_user_id) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_claim_reviews_action CHECK (action IN ('START_REVIEW', 'REQUEST_DOCUMENTS', 'RESUME_REVIEW', 'APPROVE', 'REJECT'))
);

CREATE INDEX ix_claim_reviews_claim_created ON claim_reviews (claim_id, created_at);
CREATE INDEX ix_claim_reviews_reviewer ON claim_reviews (reviewed_by_user_id);