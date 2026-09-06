package com.adrovis.adrovis_backend.career.entity;

import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "candidate_outreach",
        indexes = {
                @Index(
                        name = "idx_candidate_outreach_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_candidate_outreach_source",
                        columnList = "source"
                ),
                @Index(
                        name = "idx_candidate_outreach_application",
                        columnList = "application_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateOutreach extends BaseAuditableEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "job_id")
    private java.util.UUID jobId;

    @Column(nullable = false, length = 30)
    private String outreachStatus = "PENDING";

    @Column
    private Instant outreachSentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            foreignKey = @ForeignKey(name = "fk_candidate_outreach_application")
    )
    private Application application;

    public CandidateOutreach(
            String name,
            String email,
            String source,
            java.util.UUID jobId
    ) {
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.source = source.trim().toUpperCase();
        this.jobId = jobId;
    }

    public void markSent() {
        this.outreachStatus = "SENT";
        this.outreachSentAt = Instant.now();
    }

    public void markFailed() {
        this.outreachStatus = "FAILED";
    }

    public void linkApplication(Application application) {
        this.application = application;
        this.outreachStatus = "APPLIED";
    }
}