package com.adrovis.adrovis_backend.unpaidinternship.entity;

import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import com.adrovis.adrovis_backend.unpaidinternship.enums.UnpaidInternshipApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "unpaid_internship_application")
@Getter
@Setter
@NoArgsConstructor
public class UnpaidInternshipApplication extends BaseAuditableEntity {

    @Column(name = "application_id", nullable = false, unique = true, length = 30)
    private String applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UnpaidInternshipApplicationStatus status;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 200)
    private String college;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @Column(length = 100)
    private String batch;

    @Column(name = "internship_title", nullable = false)
    private String internshipTitle;

    @Column(name = "application_source", nullable = false, length = 100)
    private String applicationSource;

    @Column(name = "resume_storage_path", nullable = false, length = 500)
    private String resumeStoragePath;

    @Column(name = "resume_original_name", nullable = false, length = 255)
    private String resumeOriginalName;

    @Column(name = "resume_mime_type", nullable = false, length = 100)
    private String resumeMimeType;

    @Column(name = "resume_size_bytes", nullable = false)
    private Long resumeSizeBytes;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "email_sent", nullable = false)
    private boolean emailSent = false;

    public UnpaidInternshipApplication(
            String applicationId,
            String fullName,
            String email,
            String phone,
            String college,
            Integer graduationYear,
            String batch,
            String internshipTitle,
            String applicationSource,
            String resumeStoragePath,
            String resumeOriginalName,
            String resumeMimeType,
            Long resumeSizeBytes
    ) {
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.college = college;
        this.graduationYear = graduationYear;
        this.batch = batch;
        this.internshipTitle = internshipTitle;
        this.applicationSource = applicationSource;
        this.resumeStoragePath = resumeStoragePath;
        this.resumeOriginalName = resumeOriginalName;
        this.resumeMimeType = resumeMimeType;
        this.resumeSizeBytes = resumeSizeBytes;
        this.status = UnpaidInternshipApplicationStatus.SUBMITTED;
        this.submittedAt = Instant.now();
    }

    public void changeStatus(UnpaidInternshipApplicationStatus status) {
        this.status = status;
    }

    public void markEmailSent() {
        this.emailSent = true;
    }
}