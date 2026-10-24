CREATE TABLE claim_documents (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(100) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_key VARCHAR(300) NOT NULL UNIQUE,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_claim_documents_claim FOREIGN KEY (claim_id) REFERENCES claims (id) ON DELETE RESTRICT,
    CONSTRAINT ck_claim_documents_file_size_positive CHECK (file_size > 0)
);

CREATE INDEX ix_claim_documents_claim_id ON claim_documents (claim_id);