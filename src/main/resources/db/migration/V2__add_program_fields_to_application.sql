-- V2__add_program_fields_to_application.sql

ALTER TABLE application
    ADD COLUMN application_id VARCHAR(20) NOT NULL,
    ADD COLUMN job_title_snapshot VARCHAR(150) NULL,
    ADD COLUMN college VARCHAR(200) NULL,
    ADD COLUMN graduation_year INT NULL,
    ADD COLUMN resume_original_name VARCHAR(255) NOT NULL,
    ADD COLUMN resume_mime_type VARCHAR(100) NOT NULL,
    ADD COLUMN resume_size_bytes BIGINT NOT NULL,
    ADD COLUMN note TEXT NULL,
    ADD COLUMN is_consent BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY COLUMN submitted_at TIMESTAMP(6) NULL;

ALTER TABLE application
    ADD CONSTRAINT uq_application_application_id UNIQUE (application_id);

CREATE INDEX idx_application_application_id
    ON application(application_id);