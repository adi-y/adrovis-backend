package com.adrovis.adrovis_backend.career.entity;

import com.adrovis.adrovis_backend.career.enums.EmploymentType;
import com.adrovis.adrovis_backend.career.enums.JobStatus;
import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "job",
        indexes = {
                @Index(name = "idx_job_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job extends BaseAuditableEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    public Job(
            String title,
            String description,
            String location,
            EmploymentType employmentType,
            JobStatus status
    ) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.employmentType = employmentType;
        this.status = status;
    }
}