CREATE TABLE leads
(
    id BINARY(16) NOT NULL,

    lead_type VARCHAR(30) NOT NULL,

    full_name VARCHAR(150) NOT NULL,

    company VARCHAR(150) NULL,

    email VARCHAR(150) NOT NULL,

    phone VARCHAR(20) NOT NULL,

    project_type VARCHAR(100) NULL,

    budget VARCHAR(100) NULL,

    message VARCHAR(2000) NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    version BIGINT NOT NULL,

    CONSTRAINT pk_leads
        PRIMARY KEY (id),

    CONSTRAINT chk_lead_type
        CHECK (lead_type IN ('CALLBACK', 'CONSULTATION')),

    CONSTRAINT chk_lead_status
        CHECK (status IN (
            'NEW',
            'CONTACTED',
            'QUALIFIED',
            'LOST',
            'CONVERTED'
        ))
);

CREATE INDEX idx_lead_type_status
    ON leads (lead_type, status);

CREATE INDEX idx_lead_created_at
    ON leads (created_at);

CREATE INDEX idx_lead_email
    ON leads (email);