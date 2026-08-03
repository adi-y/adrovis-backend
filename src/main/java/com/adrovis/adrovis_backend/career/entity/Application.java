package com.adrovis.adrovis_backend.career.entity;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import com.adrovis.adrovis_backend.common.exception.AppException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "application",
        indexes = {
                @Index(name = "idx_application_job_id", columnList = "job_id"),
                @Index(name = "idx_application_status", columnList = "application_status"),
                @Index(name = "idx_application_email", columnList = "applicant_email"),
                @Index(name = "idx_application_application_id", columnList = "application_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application extends BaseAuditableEntity {

    // Human-readable, externally-facing ID (e.g. APP202600001).
    // Distinct from the inherited UUID primary key — this is what
    // the frontend receives and echoes back on submit.
    @Column(name = "application_id", nullable = false, unique = true, length = 20)
    private String applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = true)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationType applicationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus applicationStatus;

    @Column(length = 150)
    private String jobTitleSnapshot;

    @Column(nullable = false, length = 255)
    private String applicantName;

    @Column(nullable = false, length = 255)
    private String applicantEmail;

    @Column(length = 20)
    private String applicantPhone;

    // PROGRAM only — null for JOB
    @Column(length = 200)
    private String college;

    // PROGRAM only — null for JOB
    @Column
    private Integer graduationYear;

    @Column(nullable = false, length = 500)
    private String resumeStorageKey;

    @Column(length = 1000)
    private String resumeUrl;

    @Column(nullable = false, length = 255)
    private String resumeOriginalName;

    @Column(nullable = false, length = 100)
    private String resumeMimeType;

    @Column(nullable = false)
    private Long resumeSizeBytes;

    // JOB only, optional
    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private boolean isConsent;

    // Nullable — set only when status transitions to SUBMITTED
    @Column
    private Instant submittedAt;

    public Application(
            String applicationId,
            Job job,
            ApplicationType applicationType,
            ApplicationStatus applicationStatus,
            String jobTitleSnapshot,
            String applicantName,
            String applicantEmail,
            String applicantPhone,
            String college,
            Integer graduationYear,
            String resumeStorageKey,
            String resumeUrl,
            String resumeOriginalName,
            String resumeMimeType,
            Long resumeSizeBytes,
            String note,
            boolean isConsent,
            Instant submittedAt
    ) {

        if (applicationType == ApplicationType.JOB && job == null) {
            throw new IllegalArgumentException("JOB applications must reference a Job.");
        }

        if (applicationType == ApplicationType.PROGRAM && job != null) {
            throw new IllegalArgumentException("PROGRAM applications must not reference a Job.");
        }

        this.applicationId = applicationId;
        this.job = job;
        this.applicationType = applicationType;
        this.applicationStatus = applicationStatus;
        this.jobTitleSnapshot = jobTitleSnapshot;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.college = college;
        this.graduationYear = graduationYear;
        this.resumeStorageKey = resumeStorageKey;
        this.resumeUrl = resumeUrl;
        this.resumeOriginalName = resumeOriginalName;
        this.resumeMimeType = resumeMimeType;
        this.resumeSizeBytes = resumeSizeBytes;
        this.note = note;
        this.isConsent = isConsent;
        this.submittedAt = submittedAt;
    }

    /**
     * Transitions a PENDING Program application to SUBMITTED.
     * Guards against double-submit (already SUBMITTED or beyond) — see SDD 12.4.
     */
    public void submitProgramApplication() {
        if (this.applicationStatus != ApplicationStatus.PENDING) {
            throw AppException.conflict("This application has already been submitted.");
        }
        this.applicationStatus = ApplicationStatus.SUBMITTED;
        this.isConsent = true;
        this.submittedAt = Instant.now();
    }

    public void changeStatus(ApplicationStatus applicationStatus) {
        if (applicationStatus == null) {
            throw new IllegalArgumentException("Application status cannot be null.");
        }
        this.applicationStatus = applicationStatus;
    }
}