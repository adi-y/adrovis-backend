-- ============================================================================
-- V12__fix_campaign_recipient_eligibility_constraint.sql
-- ============================================================================
--
-- The check constraint ck_campaign_recipient_eligibility was defined before
-- several CampaignEligibilityStatus enum values were introduced (INTERESTED,
-- APPLICATION_UNDER_REVIEW, APPLICATION_SHORTLISTED, APPLICATION_INTERVIEW,
-- APPLICATION_HIRED, APPLICATION_REJECTED, ALREADY_SENT, CAMPAIGN_CLOSED).
--
-- This caused a DataIntegrityViolationException whenever the application
-- tried to persist eligibility_status = 'INTERESTED' via the
-- /api/v1/public/campaigns/interest endpoint.
--
-- This migration realigns the constraint with the full, current
-- CampaignEligibilityStatus enum.
-- ============================================================================

ALTER TABLE public.campaign_recipient
    DROP CONSTRAINT IF EXISTS ck_campaign_recipient_eligibility;

ALTER TABLE public.campaign_recipient
    ADD CONSTRAINT ck_campaign_recipient_eligibility
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
            'INTERESTED',
            'ALREADY_SENT',
            'CAMPAIGN_CLOSED',
            'NOT_ELIGIBLE'
        )
    );