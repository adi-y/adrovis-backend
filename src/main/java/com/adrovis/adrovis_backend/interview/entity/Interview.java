package com.adrovis.adrovis_backend.interview.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionGenerationStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "interview")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue
    private UUID id;

    // Plain FK column (no JPA relationship) — keeps this module decoupled from the
    // exact shape of your existing Application entity. Resolve via ApplicationRepository
    // in the service layer whenever applicant details are needed.
    @Column(name = "application_id", nullable = false, unique = true)
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InterviewStatus status;

    @Column(name = "scheduled_start_utc")
    private OffsetDateTime scheduledStartUtc;

    @Column(name = "scheduled_end_utc")
    private OffsetDateTime scheduledEndUtc;

    @Column(name = "display_timezone", nullable = false, length = 50)
    private String displayTimezone;

    @Column(name = "meeting_link", length = 1000)
    private String meetingLink;

    @Column(name = "interviewer_name")
    private String interviewerName;

    @Column(name = "admin_note", columnDefinition = "text")
    private String adminNote;

    @Column(name = "candidate_note", columnDefinition = "text")
    private String candidateNote;

    // AI interview question generation
    @Enumerated(EnumType.STRING)
    @Column(name = "question_generation_status", nullable = false, length = 20)
    @Builder.Default
    private InterviewQuestionGenerationStatus questionGenerationStatus =
            InterviewQuestionGenerationStatus.NOT_STARTED;

    @Column(name = "question_generation_error", columnDefinition = "text")
    private String questionGenerationError;

    @Column(name = "question_generation_started_at")
    private OffsetDateTime questionGenerationStartedAt;

    @Column(name = "question_generation_completed_at")
    private OffsetDateTime questionGenerationCompletedAt;

    @Column(name = "question_generation_attempts", nullable = false)
    @Builder.Default
    private Integer questionGenerationAttempts = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_closing_pitch", columnDefinition = "jsonb")
    private String aiClosingPitch;

    @Column(name = "availability_requested_at")
    private OffsetDateTime availabilityRequestedAt;

    @Column(name = "availability_submitted_at")
    private OffsetDateTime availabilitySubmittedAt;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}