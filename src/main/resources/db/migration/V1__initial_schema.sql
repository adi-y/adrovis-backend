CREATE TABLE job (
    id BINARY(16) NOT NULL,

    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,

    employment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_job PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_job_status
    ON job(status);

CREATE TABLE application (
    id BINARY(16) NOT NULL,

    job_id BINARY(16),

    application_type VARCHAR(20) NOT NULL,
    application_status VARCHAR(20) NOT NULL,

    applicant_name VARCHAR(255) NOT NULL,
    applicant_email VARCHAR(255) NOT NULL,
    applicant_phone VARCHAR(20),

    resume_storage_key VARCHAR(500) NOT NULL,
    resume_url VARCHAR(1000),

    cover_letter TEXT,

    submitted_at TIMESTAMP(6) NOT NULL,

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_application PRIMARY KEY (id),

    CONSTRAINT fk_application_job
        FOREIGN KEY (job_id)
        REFERENCES job(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_application_job_id
    ON application(job_id);

CREATE INDEX idx_application_status
    ON application(application_status);

CREATE INDEX idx_application_email
    ON application(applicant_email);