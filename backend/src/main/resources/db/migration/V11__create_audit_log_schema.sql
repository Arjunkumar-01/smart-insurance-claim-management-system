CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    actor_user_id UUID,
    resource_type VARCHAR(50),
    resource_id UUID,
    description VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_audit_logs_action CHECK (action IN ('USER_REGISTERED','ACCOUNT_STATUS_CHANGED','PRODUCT_CREATED','PRODUCT_UPDATED','POLICY_PURCHASED','CLAIM_SUBMITTED','CLAIM_REVIEW_STARTED','CLAIM_DOCUMENTS_REQUESTED','CLAIM_REVIEW_RESUMED','CLAIM_APPROVED','CLAIM_REJECTED','CLAIM_SETTLED','PREMIUM_PAYMENT_COMPLETED'))
);

CREATE INDEX ix_audit_logs_occurred_at ON audit_logs (occurred_at);
CREATE INDEX ix_audit_logs_action ON audit_logs (action);
CREATE INDEX ix_audit_logs_resource ON audit_logs (resource_type, resource_id);