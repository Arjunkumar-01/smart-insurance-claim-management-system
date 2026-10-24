CREATE TABLE application_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT ck_application_roles_name CHECK (name IN ('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'ADMINISTRATOR'))
);

INSERT INTO application_roles (id, name) VALUES
    ('00000000-0000-0000-0000-000000000001', 'CUSTOMER'),
    ('00000000-0000-0000-0000-000000000002', 'AGENT'),
    ('00000000-0000-0000-0000-000000000003', 'CLAIMS_OFFICER'),
    ('00000000-0000-0000-0000-000000000004', 'ADMINISTRATOR');

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    email_normalized VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_app_users_account_status CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    CONSTRAINT ck_app_users_email_normalized CHECK (email_normalized = LOWER(TRIM(email)))
);

CREATE UNIQUE INDEX ux_app_users_email_normalized ON app_users (email_normalized);
CREATE INDEX ix_app_users_account_status ON app_users (account_status);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES application_roles (id) ON DELETE RESTRICT
);

CREATE INDEX ix_user_roles_role_id ON user_roles (role_id);