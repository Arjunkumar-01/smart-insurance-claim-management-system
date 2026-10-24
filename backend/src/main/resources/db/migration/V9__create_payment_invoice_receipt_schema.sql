CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    policy_id UUID NOT NULL UNIQUE,
    amount NUMERIC(19, 2) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_invoices_policy FOREIGN KEY (policy_id) REFERENCES policies (id) ON DELETE RESTRICT,
    CONSTRAINT ck_invoices_amount_positive CHECK (amount > 0)
);

CREATE INDEX ix_invoices_policy_id ON invoices (policy_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    payment_number VARCHAR(40) NOT NULL UNIQUE,
    policy_id UUID NOT NULL,
    invoice_id UUID NOT NULL UNIQUE,
    amount NUMERIC(19, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_reference VARCHAR(60) NOT NULL UNIQUE,
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_payments_policy FOREIGN KEY (policy_id) REFERENCES policies (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE RESTRICT,
    CONSTRAINT ck_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_payments_method CHECK (payment_method IN ('CARD', 'BANK_TRANSFER', 'UPI')),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE receipts (
    id UUID PRIMARY KEY,
    receipt_number VARCHAR(40) NOT NULL UNIQUE,
    payment_id UUID NOT NULL UNIQUE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_receipts_payment FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE RESTRICT
);
