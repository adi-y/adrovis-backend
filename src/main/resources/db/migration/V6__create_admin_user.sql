CREATE TABLE admin_user (
    id UUID NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_admin_user
        PRIMARY KEY (id),

    CONSTRAINT uk_admin_user_email
        UNIQUE (email),

    CONSTRAINT chk_admin_user_role
        CHECK (role IN ('ADMIN'))
);

CREATE INDEX idx_admin_user_email
    ON admin_user(email);