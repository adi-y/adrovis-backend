package com.adrovis.adrovis_backend.career.entity;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
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
                @Index(name = "idx_application_status", columnList = "applicationStatus"),
                @Index(name = "idx_application_email", columnList = "applicantEmail")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = true)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationType applicationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus applicationStatus;

    @Column(nullable = false, length = 255)
    private String applicantName;

    @Column(nullable = false, length = 255)
    private String applicantEmail;

    @Column(length = 20)
    private String applicantPhone;

    @Column(nullable = false, length = 500)
    private String resumeStorageKey;

    @Column(length = 1000)
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(nullable = false)
    private Instant submittedAt;

    public Application(
            Job job,
            ApplicationType applicationType,
            ApplicationStatus applicationStatus,
            String applicantName,
            String applicantEmail,
            String applicantPhone,
            String resumeStorageKey,
            String resumeUrl,
            String coverLetter,
            Instant submittedAt
    ) {

        if (applicationType == ApplicationType.JOB && job == null) {
            throw new IllegalArgumentException(
                    "JOB applications must reference a Job."
            );
        }

        if (applicationType == ApplicationType.PROGRAM && job != null) {
            throw new IllegalArgumentException(
                    "PROGRAM applications must not reference a Job."
            );
        }

        this.job = job;
        this.applicationType = applicationType;
        this.applicationStatus = applicationStatus;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.resumeStorageKey = resumeStorageKey;
        this.resumeUrl = resumeUrl;
        this.coverLetter = coverLetter;
        this.submittedAt = submittedAt;
    }
    public void changeStatus(ApplicationStatus applicationStatus) {

        if (applicationStatus == null) {
            throw new IllegalArgumentException("Application status cannot be null.");
        }

        this.applicationStatus = applicationStatus;
    }
}