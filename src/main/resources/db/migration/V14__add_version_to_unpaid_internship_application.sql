ALTER TABLE unpaid_internship_application
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;