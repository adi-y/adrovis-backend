-- V7: AI Interview Question Preparation feature
-- Adds generation-tracking columns to interview, and a new interview_question table.

ALTER TABLE public.interview
    ADD COLUMN question_generation_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    ADD COLUMN question_generation_error TEXT NULL,
    ADD COLUMN question_generation_started_at TIMESTAMPTZ NULL,
    ADD COLUMN question_generation_completed_at TIMESTAMPTZ NULL,
    ADD COLUMN question_generation_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN ai_closing_pitch JSONB NULL;

CREATE INDEX idx_interview_question_generation_status
    ON public.interview (question_generation_status);

CREATE TABLE public.interview_question (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    interview_id UUID NOT NULL,
    question_number INTEGER NOT NULL,

    category VARCHAR(20) NOT NULL,
    difficulty VARCHAR(10) NOT NULL,
    answer_source VARCHAR(20) NOT NULL,

    question TEXT NOT NULL,
    expected_answer TEXT NOT NULL,

    key_points JSONB NOT NULL DEFAULT '[]'::jsonb,
    follow_up_questions JSONB NOT NULL DEFAULT '[]'::jsonb,

    resume_basis TEXT NULL,
    interviewer_goal TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_interview_question
        PRIMARY KEY (id),

    CONSTRAINT fk_interview_question_interview
        FOREIGN KEY (interview_id)
        REFERENCES public.interview (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_interview_question_number
        UNIQUE (interview_id, question_number),

    CONSTRAINT ck_interview_question_number
        CHECK (question_number BETWEEN 1 AND 15),

    CONSTRAINT ck_interview_question_category
        CHECK (
            category IN ('PROJECT', 'SKILL', 'TECHNICAL', 'PRACTICAL', 'BEHAVIOURAL')
        ),

    CONSTRAINT ck_interview_question_difficulty
        CHECK (
            difficulty IN ('EASY', 'MEDIUM', 'TOUGH')
        ),

    CONSTRAINT ck_interview_question_answer_source
        CHECK (
            answer_source IN ('RESUME', 'KNOWLEDGE', 'BEHAVIOURAL')
        )
);

CREATE INDEX idx_interview_question_interview_id
    ON public.interview_question (interview_id);