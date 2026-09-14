-- V9: Candidate outreach + application source attribution

ALTER TABLE public.application
    ADD COLUMN source VARCHAR(50) NOT NULL DEFAULT 'WEBSITE';

CREATE INDEX idx_application_source
    ON public.application (source);


CREATE TABLE public.candidate_outreach (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,

    source VARCHAR(50) NOT NULL,

    job_id UUID NULL,

    outreach_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    outreach_sent_at TIMESTAMPTZ NULL,

    application_id UUID NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT candidate_outreach_pkey
        PRIMARY KEY (id),

    CONSTRAINT fk_candidate_outreach_job
        FOREIGN KEY (job_id)
        REFERENCES public.job(id),

    CONSTRAINT fk_candidate_outreach_application
        FOREIGN KEY (application_id)
        REFERENCES public.application(id),

    CONSTRAINT ck_candidate_outreach_status
        CHECK (
            outreach_status IN (
                'PENDING',
                'SENT',
                'APPLIED',
                'FAILED'
            )
        )
);

CREATE INDEX idx_candidate_outreach_email
    ON public.candidate_outreach (LOWER(email));

CREATE INDEX idx_candidate_outreach_source
    ON public.candidate_outreach (source);

CREATE INDEX idx_candidate_outreach_application
    ON public.candidate_outreach (application_id);

CREATE UNIQUE INDEX uq_candidate_outreach_email_source
    ON public.candidate_outreach (
        LOWER(email),
        source
    );