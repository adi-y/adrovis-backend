-- V2__add_program_fields_to_application.sql

ALTER TABLE application
    ADD COLUMN application_id VARCHAR(20) NOT NULL,
    ADD COLUMN job_title_snapshot VARCHAR(150),
    ADD COLUMN college VARCHAR(200),
    ADD COLUMN graduation_year INTEGER,
    ADD COLUMN resume_original_name VARCHAR(255) NOT NULL,
    ADD COLUMN resume_mime_type VARCHAR(100) NOT NULL,
    ADD COLUMN resume_size_bytes BIGINT NOT NULL,
    ADD COLUMN note TEXT,
    ADD COLUMN is_consent BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE application
    ALTER COLUMN submitted_at DROP NOT NULL;

ALTER TABLE application
    ADD CONSTRAINT uq_application_application_id
    UNIQUE (application_id);

CREATE INDEX idx_application_application_id
    ON application(application_id);