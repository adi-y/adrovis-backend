CREATE TABLE unpaid_internship_application (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    application_id VARCHAR(30) NOT NULL UNIQUE,

    status VARCHAR(30) NOT NULL,

    full_name VARCHAR(150) NOT NULL,

    email VARCHAR(150) NOT NULL,

    phone VARCHAR(20) NOT NULL,

    college VARCHAR(200) NOT NULL,

    graduation_year INTEGER NOT NULL,

    batch VARCHAR(100),

    internship_title VARCHAR(255) NOT NULL,

    application_source VARCHAR(100) NOT NULL,

    resume_storage_path VARCHAR(500) NOT NULL,

    resume_original_name VARCHAR(255) NOT NULL,

    resume_mime_type VARCHAR(100) NOT NULL,

    resume_size_bytes BIGINT NOT NULL,

    submitted_at TIMESTAMPTZ NOT NULL,

    email_sent BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_unpaid_internship_status
        CHECK (
            status IN (
                'SUBMITTED',
                'UNDER_REVIEW',
                'SHORTLISTED',
                'REJECTED',
                'HIRED'
            )
        )
);

CREATE INDEX idx_unpaid_internship_status
    ON unpaid_internship_application(status);

CREATE INDEX idx_unpaid_internship_email
    ON unpaid_internship_application(email);

CREATE INDEX idx_unpaid_internship_created_at
    ON unpaid_internship_application(created_at);