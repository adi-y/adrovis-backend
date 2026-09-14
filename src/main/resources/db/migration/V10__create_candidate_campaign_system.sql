-- V10: Candidate identity + internship campaign system
--
-- Purpose:
-- 1. Central candidate identity keyed by normalized email.
-- 2. Four-week internship re-engagement campaigns.
-- 3. One database record for every campaign email attempt/decision.
-- 4. Candidate marketing opt-out without deleting recruitment records.
-- 5. Campaign response + conversion tracking.
-- 6. Database-backed program/commercial configuration.
--
-- Latest existing migration before this change: V9.


-- ============================================================
-- 1. CANDIDATE
-- ============================================================

CREATE TABLE public.candidate (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    normalized_email VARCHAR(255) NOT NULL,

    marketing_opt_out BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_opt_out_at TIMESTAMPTZ NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_candidate
        PRIMARY KEY (id),

    CONSTRAINT uq_candidate_normalized_email
        UNIQUE (normalized_email)
);

CREATE INDEX idx_candidate_normalized_email
    ON public.candidate (normalized_email);

CREATE INDEX idx_candidate_marketing_opt_out
    ON public.candidate (marketing_opt_out);


-- ============================================================
-- 2. PROGRAM CONFIGURATION
-- ============================================================

CREATE TABLE public.program_configuration (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    program_key VARCHAR(100) NOT NULL,
    program_name VARCHAR(150) NOT NULL,

    fee_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    fee_amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,

    duration VARCHAR(50) NOT NULL,
    mode VARCHAR(50) NOT NULL,

    application_url VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_program_configuration
        PRIMARY KEY (id),

    CONSTRAINT uq_program_configuration_key
        UNIQUE (program_key),

    CONSTRAINT ck_program_configuration_fee
        CHECK (fee_amount >= 0)
);

CREATE INDEX idx_program_configuration_active
    ON public.program_configuration (active);


INSERT INTO public.program_configuration (
    program_key,
    program_name,
    fee_enabled,
    fee_amount,
    currency,
    duration,
    mode,
    application_url,
    active
)
VALUES (
    'SOFTWARE_DEVELOPER_INTERNSHIP',
    'Software Developer Internship',
    TRUE,
    499.00,
    'INR',
    '3 Months',
    'Remote/Hybrid',
    'https://www.adrovis.com/careers/internship',
    TRUE
);


-- ============================================================
-- 3. CAMPAIGN
-- ============================================================

CREATE TABLE public.campaign (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    name VARCHAR(150) NOT NULL,

    type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    total_weeks INTEGER NOT NULL DEFAULT 4,
    current_week INTEGER NOT NULL DEFAULT 0,

    scheduled_at TIMESTAMPTZ NULL,
    started_at TIMESTAMPTZ NULL,
    completed_at TIMESTAMPTZ NULL,
    closed_at TIMESTAMPTZ NULL,

    program_name_snapshot VARCHAR(150) NOT NULL,
    fee_amount_snapshot NUMERIC(12, 2) NOT NULL,
    currency_snapshot VARCHAR(3) NOT NULL,
    duration_snapshot VARCHAR(50) NOT NULL,
    mode_snapshot VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_campaign
        PRIMARY KEY (id),

    CONSTRAINT ck_campaign_type
        CHECK (
            type IN (
                'INTERNSHIP_REENGAGEMENT'
            )
        ),

    CONSTRAINT ck_campaign_status
        CHECK (
            status IN (
                'DRAFT',
                'SCHEDULED',
                'RUNNING',
                'COMPLETED',
                'CLOSED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_campaign_total_weeks
        CHECK (total_weeks BETWEEN 1 AND 4),

    CONSTRAINT ck_campaign_current_week
        CHECK (
            current_week BETWEEN 0 AND total_weeks
        ),

    CONSTRAINT ck_campaign_fee_snapshot
        CHECK (fee_amount_snapshot >= 0)
);

CREATE INDEX idx_campaign_status
    ON public.campaign (status);

CREATE INDEX idx_campaign_scheduled_at
    ON public.campaign (scheduled_at);


-- ============================================================
-- 4. CAMPAIGN RECIPIENT
-- ============================================================

CREATE TABLE public.campaign_recipient (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    campaign_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    application_id UUID NULL,

    eligibility_status VARCHAR(40) NOT NULL DEFAULT 'NOT_ELIGIBLE',
    current_application_status VARCHAR(30) NULL,

    first_contacted_at TIMESTAMPTZ NULL,
    last_contacted_at TIMESTAMPTZ NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_campaign_recipient
        PRIMARY KEY (id),

    CONSTRAINT fk_campaign_recipient_campaign
        FOREIGN KEY (campaign_id)
        REFERENCES public.campaign(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_recipient_candidate
        FOREIGN KEY (candidate_id)
        REFERENCES public.candidate(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_recipient_application
        FOREIGN KEY (application_id)
        REFERENCES public.application(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_campaign_recipient_campaign_candidate
        UNIQUE (campaign_id, candidate_id),

    CONSTRAINT ck_campaign_recipient_eligibility
        CHECK (
            eligibility_status IN (
                'ELIGIBLE',
                'UNSUBSCRIBED',
                'APPLICATION_PENDING',
                'APPLICATION_SUBMITTED',
                'APPLICATION_UNDER_REVIEW',
                'APPLICATION_SHORTLISTED',
                'APPLICATION_INTERVIEW',
                'APPLICATION_HIRED',
                'APPLICATION_REJECTED',
                'ALREADY_SENT',
                'CAMPAIGN_CLOSED',
                'NOT_ELIGIBLE'
            )
        )
);

CREATE INDEX idx_campaign_recipient_candidate
    ON public.campaign_recipient (candidate_id);

CREATE INDEX idx_campaign_recipient_application
    ON public.campaign_recipient (application_id);

CREATE INDEX idx_campaign_recipient_eligibility
    ON public.campaign_recipient (eligibility_status);


-- ============================================================
-- 5. CAMPAIGN EMAIL
-- ============================================================

CREATE TABLE public.campaign_email (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    campaign_id UUID NOT NULL,
    campaign_recipient_id UUID NOT NULL,

    candidate_id UUID NOT NULL,
    application_id UUID NULL,

    week_number INTEGER NOT NULL,

    email_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',

    to_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_key VARCHAR(100) NOT NULL,

    cta_url VARCHAR(2000) NULL,
    unsubscribe_url VARCHAR(2000) NULL,

    provider_message_id VARCHAR(255) NULL,

    scheduled_at TIMESTAMPTZ NULL,
    sent_at TIMESTAMPTZ NULL,
    opened_at TIMESTAMPTZ NULL,
    clicked_at TIMESTAMPTZ NULL,
    failed_at TIMESTAMPTZ NULL,

    failure_reason TEXT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_campaign_email
        PRIMARY KEY (id),

    CONSTRAINT fk_campaign_email_campaign
        FOREIGN KEY (campaign_id)
        REFERENCES public.campaign(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_email_recipient
        FOREIGN KEY (campaign_recipient_id)
        REFERENCES public.campaign_recipient(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_email_candidate
        FOREIGN KEY (candidate_id)
        REFERENCES public.candidate(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_email_application
        FOREIGN KEY (application_id)
        REFERENCES public.application(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_campaign_email_campaign_recipient_week
        UNIQUE (
            campaign_recipient_id,
            week_number
        ),

    CONSTRAINT ck_campaign_email_week
        CHECK (week_number BETWEEN 1 AND 4),

    CONSTRAINT ck_campaign_email_type
        CHECK (
            email_type IN (
                'WEEK_1_REENGAGEMENT',
                'WEEK_2_EXPERIENCE',
                'WEEK_3_ENGINEERING_WORKFLOW',
                'WEEK_4_OUTCOMES'
            )
        ),

    CONSTRAINT ck_campaign_email_status
        CHECK (
            status IN (
                'QUEUED',
                'SENT',
                'FAILED',
                'SKIPPED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_campaign_email_campaign
    ON public.campaign_email (campaign_id);

CREATE INDEX idx_campaign_email_candidate
    ON public.campaign_email (candidate_id);

CREATE INDEX idx_campaign_email_status
    ON public.campaign_email (status);

CREATE INDEX idx_campaign_email_provider_id
    ON public.campaign_email (provider_message_id);


-- ============================================================
-- 6. CAMPAIGN RESPONSE
-- ============================================================

CREATE TABLE public.campaign_response (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    campaign_id UUID NOT NULL,
    campaign_email_id UUID NULL,

    candidate_id UUID NOT NULL,
    application_id UUID NULL,

    response_type VARCHAR(30) NOT NULL,
    response_text TEXT NULL,

    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_campaign_response
        PRIMARY KEY (id),

    CONSTRAINT fk_campaign_response_campaign
        FOREIGN KEY (campaign_id)
        REFERENCES public.campaign(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_response_email
        FOREIGN KEY (campaign_email_id)
        REFERENCES public.campaign_email(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_campaign_response_candidate
        FOREIGN KEY (candidate_id)
        REFERENCES public.candidate(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_response_application
        FOREIGN KEY (application_id)
        REFERENCES public.application(id)
        ON DELETE SET NULL,

    CONSTRAINT ck_campaign_response_type
        CHECK (
            response_type IN (
                'INTERESTED',
                'QUESTION',
                'NOT_INTERESTED',
                'REQUEST_CALLBACK',
                'OTHER'
            )
        )
);

CREATE INDEX idx_campaign_response_candidate
    ON public.campaign_response (candidate_id);

CREATE INDEX idx_campaign_response_application
    ON public.campaign_response (application_id);

CREATE INDEX idx_campaign_response_type
    ON public.campaign_response (response_type);


-- ============================================================
-- 7. CAMPAIGN CONVERSION
-- ============================================================

CREATE TABLE public.campaign_conversion (
    id UUID NOT NULL DEFAULT gen_random_uuid(),

    campaign_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    application_id UUID NULL,

    conversion_type VARCHAR(40) NOT NULL,
    converted_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_campaign_conversion
        PRIMARY KEY (id),

    CONSTRAINT fk_campaign_conversion_campaign
        FOREIGN KEY (campaign_id)
        REFERENCES public.campaign(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_conversion_candidate
        FOREIGN KEY (candidate_id)
        REFERENCES public.candidate(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_campaign_conversion_application
        FOREIGN KEY (application_id)
        REFERENCES public.application(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_campaign_conversion_campaign
    ON public.campaign_conversion (campaign_id);

CREATE INDEX idx_campaign_conversion_candidate
    ON public.campaign_conversion (candidate_id);

CREATE INDEX idx_campaign_conversion_application
    ON public.campaign_conversion (application_id);

CREATE INDEX idx_campaign_conversion_type
    ON public.campaign_conversion (conversion_type);


-- ============================================================
-- 8. REPORTING INDEXES
-- ============================================================

CREATE INDEX idx_campaign_email_campaign_created
    ON public.campaign_email (
        campaign_id,
        created_at DESC
    );

CREATE INDEX idx_campaign_response_campaign_received
    ON public.campaign_response (
        campaign_id,
        received_at DESC
    );

CREATE INDEX idx_campaign_conversion_campaign_converted
    ON public.campaign_conversion (
        campaign_id,
        converted_at DESC
    );


-- ============================================================
-- 9. BACKFILL CANDIDATE IDENTITY FROM EXISTING PROGRAM
--    APPLICATIONS
--
--    These candidates already exist in recruitment data.
-- ============================================================

INSERT INTO public.candidate (
    id,
    name,
    email,
    normalized_email,
    marketing_opt_out
)
SELECT DISTINCT ON (
    LOWER(TRIM(a.applicant_email))
)
    gen_random_uuid(),
    a.applicant_name,
    a.applicant_email,
    LOWER(TRIM(a.applicant_email)),
    FALSE
FROM public.application a
WHERE a.application_type = 'PROGRAM'
ORDER BY
    LOWER(TRIM(a.applicant_email)),
    a.created_at DESC
ON CONFLICT (normalized_email)
DO NOTHING;


-- ============================================================
-- 10. BACKFILL CANDIDATE IDENTITY FROM EXISTING OUTREACH
--
--     This is important for candidates who were contacted but
--     never created an application.
--
--     Those candidates must remain eligible for the campaign:
--
--     Candidate exists
--     Application = NONE
--     Eligibility = ELIGIBLE
--     CTA = normal internship page
-- ============================================================

INSERT INTO public.candidate (
    id,
    name,
    email,
    normalized_email,
    marketing_opt_out
)
SELECT DISTINCT ON (
    LOWER(TRIM(co.email))
)
    gen_random_uuid(),
    co.name,
    co.email,
    LOWER(TRIM(co.email)),
    FALSE
FROM public.candidate_outreach co
WHERE co.email IS NOT NULL
  AND TRIM(co.email) <> ''
ORDER BY
    LOWER(TRIM(co.email))
ON CONFLICT (normalized_email)
DO NOTHING;