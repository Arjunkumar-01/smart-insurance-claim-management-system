CREATE TABLE claim_status_history (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_by_user_id UUID,
    change_reason VARCHAR(500) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_claim_status_history_claim FOREIGN KEY (claim_id) REFERENCES claims (id) ON DELETE RESTRICT,
    CONSTRAINT fk_claim_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_claim_status_history_from_status CHECK (from_status IS NULL OR from_status IN ('SUBMITTED', 'UNDER_REVIEW', 'DOCUMENTS_REQUIRED', 'APPROVED', 'REJECTED', 'SETTLED')),
    CONSTRAINT ck_claim_status_history_to_status CHECK (to_status IN ('SUBMITTED', 'UNDER_REVIEW', 'DOCUMENTS_REQUIRED', 'APPROVED', 'REJECTED', 'SETTLED'))
);

CREATE INDEX ix_claim_status_history_claim_changed ON claim_status_history (claim_id, changed_at, id);