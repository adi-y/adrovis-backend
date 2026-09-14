-- ============================================================================
-- V11__automated_program_campaign_journey.sql
-- ============================================================================
--
-- Automated Software Developer Internship campaign journey.
--
-- IMPORTANT:
--   1. PROGRAM applications only.
--   2. JOB applications are never enrolled.
--   3. Existing candidate/application/email DATA is NOT deleted.
--   4. The old campaign_email uniqueness CONSTRAINT is replaced only because
--      a candidate must be allowed to receive Week 1 again after a new
--      application journey starts.
--   5. One shared automated campaign definition is used.
--   6. Each candidate has an independent journey clock in campaign_recipient.
--
-- Production timing:
--   Initial outreach sent
--        -> next calendar day at 10:00 AM Asia/Kolkata -> Week 1
--        -> every 7 calendar days at 10:00 AM         -> Week 2
--        -> every 7 calendar days at 10:00 AM         -> Week 3
--        -> every 7 calendar days at 10:00 AM         -> Week 4
--
-- If a candidate later creates a PROGRAM application:
--   existing outreach journey is superseded
--        -> fresh APPLICATION_FOLLOW_UP journey
--        -> next calendar day at 10:00 AM -> Week 1
--        -> then every 7 days
--
-- TEST MODE timing is handled by Java code for new enrollments:
--   weekDurationSeconds = 120
--   schedulerDelayMs    = 120000
--
-- ============================================================================


-- ============================================================================
-- 1. CAMPAIGN AUTOMATION KEY
-- ============================================================================

ALTER TABLE public.campaign
    ADD COLUMN IF NOT EXISTS automation_key VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS
    uq_campaign_automation_key
ON public.campaign (automation_key)
WHERE automation_key IS NOT NULL;


-- ============================================================================
-- 2. PER-CANDIDATE JOURNEY STATE
-- ============================================================================

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS journey_type VARCHAR(30);

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS journey_status VARCHAR(20);

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS current_week INTEGER NOT NULL DEFAULT 0;

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS journey_version INTEGER NOT NULL DEFAULT 1;

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS next_send_at TIMESTAMPTZ;

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS enrolled_at TIMESTAMPTZ;

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS last_journey_sent_at TIMESTAMPTZ;

ALTER TABLE public.campaign_recipient
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;


-- ============================================================================
-- 3. SAFE DEFAULT/BACKFILL FOR EXISTING RECIPIENT ROWS
-- ============================================================================

UPDATE public.campaign_recipient
SET journey_type = 'OUTREACH_FOLLOW_UP'
WHERE journey_type IS NULL;

UPDATE public.campaign_recipient
SET journey_status = 'ACTIVE'
WHERE journey_status IS NULL;

ALTER TABLE public.campaign_recipient
    ALTER COLUMN journey_type
    SET DEFAULT 'OUTREACH_FOLLOW_UP';

ALTER TABLE public.campaign_recipient
    ALTER COLUMN journey_status
    SET DEFAULT 'ACTIVE';


-- ============================================================================
-- 4. RECIPIENT INDEXES
-- ============================================================================

CREATE INDEX IF NOT EXISTS
    idx_campaign_recipient_due
ON public.campaign_recipient (
    journey_status,
    next_send_at
);

CREATE INDEX IF NOT EXISTS
    idx_campaign_recipient_journey
ON public.campaign_recipient (
    campaign_id,
    journey_version,
    current_week
);


-- ============================================================================
-- 5. CAMPAIGN EMAIL JOURNEY VERSION
-- ============================================================================

ALTER TABLE public.campaign_email
    ADD COLUMN IF NOT EXISTS journey_version INTEGER;

UPDATE public.campaign_email ce
SET journey_version = COALESCE(
    (
        SELECT cr.journey_version
        FROM public.campaign_recipient cr
        WHERE cr.id = ce.campaign_recipient_id
    ),
    1
)
WHERE ce.journey_version IS NULL;

ALTER TABLE public.campaign_email
    ALTER COLUMN journey_version SET DEFAULT 1;

ALTER TABLE public.campaign_email
    ALTER COLUMN journey_version SET NOT NULL;


-- ============================================================================
-- 6. REPLACE OLD EMAIL UNIQUENESS RULE
-- ============================================================================
--
-- IMPORTANT:
-- This does NOT delete any campaign_email rows.
--
-- It removes only the old uniqueness rule:
--   recipient + week
--
-- and replaces it with:
--   recipient + journey_version + week
--
-- This is required so:
--
--   Journey 1 / Outreach / Week 1
--   Journey 2 / Application / Week 1
--
-- can both exist for the same person.
-- ============================================================================

ALTER TABLE public.campaign_email
    DROP CONSTRAINT IF EXISTS
        uq_campaign_email_campaign_recipient_week;

CREATE UNIQUE INDEX IF NOT EXISTS
    uq_campaign_email_recipient_journey_week
ON public.campaign_email (
    campaign_recipient_id,
    journey_version,
    week_number
);

CREATE INDEX IF NOT EXISTS
    idx_campaign_email_recipient_journey
ON public.campaign_email (
    campaign_recipient_id,
    journey_version
);


-- ============================================================================
-- 7. JOURNEY CHECK CONSTRAINTS
-- ============================================================================

ALTER TABLE public.campaign_recipient
    DROP CONSTRAINT IF EXISTS
        ck_campaign_recipient_journey_type;

ALTER TABLE public.campaign_recipient
    ADD CONSTRAINT ck_campaign_recipient_journey_type
    CHECK (
        journey_type IN (
            'OUTREACH_FOLLOW_UP',
            'APPLICATION_FOLLOW_UP'
        )
    );


ALTER TABLE public.campaign_recipient
    DROP CONSTRAINT IF EXISTS
        ck_campaign_recipient_journey_status;

ALTER TABLE public.campaign_recipient
    ADD CONSTRAINT ck_campaign_recipient_journey_status
    CHECK (
        journey_status IN (
            'ACTIVE',
            'SUPPRESSED',
            'COMPLETED',
            'UNSUBSCRIBED'
        )
    );


ALTER TABLE public.campaign_recipient
    DROP CONSTRAINT IF EXISTS
        ck_campaign_recipient_current_week;

ALTER TABLE public.campaign_recipient
    ADD CONSTRAINT ck_campaign_recipient_current_week
    CHECK (
        current_week BETWEEN 0 AND 4
    );


ALTER TABLE public.campaign_recipient
    DROP CONSTRAINT IF EXISTS
        ck_campaign_recipient_journey_version;

ALTER TABLE public.campaign_recipient
    ADD CONSTRAINT ck_campaign_recipient_journey_version
    CHECK (
        journey_version >= 1
    );


-- ============================================================================
-- 8. CREATE ONE LONG-RUNNING AUTOMATED PROGRAM CAMPAIGN
-- ============================================================================
--
-- This is the campaign definition.
-- It is NOT one campaign per candidate.
-- Candidate-level timing lives in campaign_recipient.
-- ============================================================================

INSERT INTO public.campaign (
    name,
    type,
    status,
    automation_key,
    total_weeks,
    current_week,
    scheduled_at,
    started_at,
    program_name_snapshot,
    fee_amount_snapshot,
    currency_snapshot,
    duration_snapshot,
    mode_snapshot
)
SELECT
    'Software Developer Internship - 4 Week Re-engagement',
    'INTERNSHIP_REENGAGEMENT',
    'RUNNING',
    'SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT',
    4,
    0,
    now(),
    now(),
    pc.program_name,
    pc.fee_amount,
    pc.currency,
    pc.duration,
    pc.mode
FROM public.program_configuration pc
WHERE pc.program_key = 'SOFTWARE_DEVELOPER_INTERNSHIP'
  AND pc.active = TRUE
  AND NOT EXISTS (
      SELECT 1
      FROM public.campaign c
      WHERE c.automation_key =
            'SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT'
  );


-- ============================================================================
-- 9. HISTORICAL PROGRAM OUTREACH BACKFILL
-- ============================================================================
--
-- Only outreach with:
--
--   job_id IS NULL
--   outreach_status = SENT
--
-- is enrolled.
--
-- Therefore JOB outreach is excluded.
--
-- Production:
--   next calendar day at 10:00 AM Asia/Kolkata
--
-- Historical records whose scheduled date is already in the past will be
-- picked up by the scheduler as due.
--
-- ============================================================================

INSERT INTO public.campaign_recipient (
    id,
    campaign_id,
    candidate_id,
    application_id,
    eligibility_status,
    current_application_status,
    journey_type,
    journey_status,
    current_week,
    journey_version,
    next_send_at,
    enrolled_at,
    first_contacted_at,
    last_contacted_at,
    created_at,
    updated_at,
    version
)
SELECT
    gen_random_uuid(),
    c.id,
    candidate.id,
    NULL,
    'ELIGIBLE',
    NULL,
    'OUTREACH_FOLLOW_UP',
    'ACTIVE',
    0,
    1,

    /*
     * Next calendar day at 10:00 AM Asia/Kolkata.
     */
    (
        (
            (
                co.outreach_sent_at
                AT TIME ZONE 'Asia/Kolkata'
            )::date
            + 1
        )
        + TIME '10:00'
    ) AT TIME ZONE 'Asia/Kolkata',

    co.outreach_sent_at,
    co.outreach_sent_at,
    co.outreach_sent_at,
    now(),
    now(),
    0

FROM public.candidate_outreach co

JOIN public.candidate candidate
    ON lower(candidate.email) = lower(co.email)

JOIN public.campaign c
    ON c.automation_key =
       'SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT'

WHERE co.job_id IS NULL
  AND co.outreach_status = 'SENT'
  AND co.outreach_sent_at IS NOT NULL

  AND NOT EXISTS (
      SELECT 1
      FROM public.campaign_recipient cr
      WHERE cr.campaign_id = c.id
        AND cr.candidate_id = candidate.id
  );


-- ============================================================================
-- 10. HISTORICAL PROGRAM APPLICATIONS WITHOUT OUTREACH RECIPIENT
-- ============================================================================
--
-- This covers candidates who:
--
--   visited the portal directly
--   created a PROGRAM application
--
-- without having received/been stored as candidate_outreach.
--
-- JOB applications are excluded.
--
-- These applicants receive an APPLICATION_FOLLOW_UP journey.
--
-- Production first campaign email:
--   next calendar day at 10:00 AM Asia/Kolkata
-- ============================================================================

INSERT INTO public.campaign_recipient (
    id,
    campaign_id,
    candidate_id,
    application_id,
    eligibility_status,
    current_application_status,
    journey_type,
    journey_status,
    current_week,
    journey_version,
    next_send_at,
    enrolled_at,
    first_contacted_at,
    last_contacted_at,
    created_at,
    updated_at,
    version
)
SELECT
    gen_random_uuid(),
    c.id,
    candidate.id,
    rpa.id,

    CASE
        WHEN rpa.application_status = 'PENDING'
            THEN 'APPLICATION_PENDING'

        WHEN rpa.application_status = 'SUBMITTED'
            THEN 'APPLICATION_SUBMITTED'

        WHEN rpa.application_status = 'UNDER_REVIEW'
            THEN 'APPLICATION_UNDER_REVIEW'

        WHEN rpa.application_status = 'SHORTLISTED'
            THEN 'APPLICATION_SHORTLISTED'

        WHEN rpa.application_status = 'HIRED'
            THEN 'APPLICATION_HIRED'

        WHEN rpa.application_status = 'REJECTED'
            THEN 'APPLICATION_REJECTED'

        ELSE 'NOT_ELIGIBLE'
    END,

    rpa.application_status,

    'APPLICATION_FOLLOW_UP',

    CASE
        WHEN rpa.application_status IN (
            'UNDER_REVIEW',
            'SHORTLISTED',
            'HIRED',
            'REJECTED'
        )
        THEN 'SUPPRESSED'

        ELSE 'ACTIVE'
    END,

    0,
    1,

    /*
     * Next calendar day at 10:00 AM Asia/Kolkata.
     */
    (
        (
            (
                rpa.created_at
                AT TIME ZONE 'Asia/Kolkata'
            )::date
            + 1
        )
        + TIME '10:00'
    ) AT TIME ZONE 'Asia/Kolkata',

    rpa.created_at,
    NULL,
    NULL,
    now(),
    now(),
    0

FROM (
    SELECT
        a.*,
        row_number() OVER (
            PARTITION BY lower(a.applicant_email)
            ORDER BY a.created_at DESC
        ) AS rn

    FROM public.application a

    WHERE a.application_type = 'PROGRAM'
) rpa

JOIN public.candidate candidate
    ON lower(candidate.email) =
       lower(rpa.applicant_email)

JOIN public.campaign c
    ON c.automation_key =
       'SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT'

WHERE rpa.rn = 1

  AND NOT EXISTS (
      SELECT 1
      FROM public.campaign_recipient cr
      WHERE cr.campaign_id = c.id
        AND cr.candidate_id = candidate.id
  );


-- ============================================================================
-- 11. EXISTING PROGRAM OUTREACH RECIPIENTS WITH PROGRAM APPLICATIONS
-- ============================================================================
--
-- If someone already existed in the outreach journey and has since created
-- a PROGRAM application, the application becomes the active journey.
--
-- This intentionally creates a new journey version:
--
--   Version 1 = OUTREACH_FOLLOW_UP
--   Version 2 = APPLICATION_FOLLOW_UP
--
-- This allows the candidate to receive a fresh Week 1.
--
-- ============================================================================

WITH ranked_program_applications AS (
    SELECT
        a.*,

        row_number() OVER (
            PARTITION BY lower(a.applicant_email)
            ORDER BY a.created_at DESC
        ) AS rn

    FROM public.application a

    WHERE a.application_type = 'PROGRAM'
)

UPDATE public.campaign_recipient cr

SET
    application_id =
        rpa.id,

    current_application_status =
        rpa.application_status,

    journey_type =
        'APPLICATION_FOLLOW_UP',

    journey_status =
        CASE
            WHEN rpa.application_status IN (
                'UNDER_REVIEW',
                'SHORTLISTED',
                'HIRED',
                'REJECTED'
            )
            THEN 'SUPPRESSED'

            ELSE 'ACTIVE'
        END,

    current_week =
        0,

    journey_version =
        cr.journey_version + 1,

    next_send_at =
        (
            (
                (
                    rpa.created_at
                    AT TIME ZONE 'Asia/Kolkata'
                )::date
                + 1
            )
            + TIME '10:00'
        ) AT TIME ZONE 'Asia/Kolkata',

    enrolled_at =
        rpa.created_at,

    completed_at =
        NULL

FROM ranked_program_applications rpa

WHERE rpa.rn = 1

  AND lower(
        (
            SELECT cand.email
            FROM public.candidate cand
            WHERE cand.id = cr.candidate_id
        )
      ) = lower(rpa.applicant_email)

  AND EXISTS (
      SELECT 1
      FROM public.campaign c
      WHERE c.id = cr.campaign_id
        AND c.automation_key =
            'SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT'
  );


-- ============================================================================
-- 12. FINAL NORMALIZATION FOR EXISTING APPLICATION JOURNEYS
-- ============================================================================
--
-- Update eligibility/status on every latest PROGRAM application recipient.
-- This does not touch JOB applications.
--
-- ============================================================================

UPDATE public.campaign_recipient cr

SET
    current_application_status =
        a.application_status,

    eligibility_status =
        CASE
            WHEN a.application_status = 'PENDING'
                THEN 'APPLICATION_PENDING'

            WHEN a.application_status = 'SUBMITTED'
                THEN 'APPLICATION_SUBMITTED'

            WHEN a.application_status = 'UNDER_REVIEW'
                THEN 'APPLICATION_UNDER_REVIEW'

            WHEN a.application_status = 'SHORTLISTED'
                THEN 'APPLICATION_SHORTLISTED'

            WHEN a.application_status = 'HIRED'
                THEN 'APPLICATION_HIRED'

            WHEN a.application_status = 'REJECTED'
                THEN 'APPLICATION_REJECTED'

            ELSE 'NOT_ELIGIBLE'
        END,

    journey_status =
        CASE
            WHEN a.application_status IN (
                'UNDER_REVIEW',
                'SHORTLISTED',
                'HIRED',
                'REJECTED'
            )
            THEN 'SUPPRESSED'

            WHEN cr.journey_status <> 'UNSUBSCRIBED'
            THEN 'ACTIVE'

            ELSE cr.journey_status
        END

FROM public.application a

WHERE a.id = cr.application_id

  AND a.application_type = 'PROGRAM'

  AND EXISTS (
      SELECT 1
      FROM public.campaign c
      WHERE c.id = cr.campaign_id
        AND c.automation_key =
            'SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT'
  );


-- ============================================================================
-- END V11
-- ============================================================================