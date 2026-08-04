package com.adrovis.adrovis_backend.contact.entity;

import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import com.adrovis.adrovis_backend.contact.enums.LeadStatus;
import com.adrovis.adrovis_backend.contact.enums.LeadType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a lead submitted through the public Contact section.
 *
 * <p>
 * A Lead can originate from one of two flows:
 * <ul>
 *     <li>Request Callback</li>
 *     <li>Schedule Consultation</li>
 * </ul>
 *
 * Consultation-specific fields remain {@code null} for callback requests.
 *
 * This entity intentionally contains no business logic. All validation,
 * state transitions and persistence rules belong to the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "leads",
        indexes = {
                @Index(
                        name = "idx_lead_type_status",
                        columnList = "lead_type,status"
                ),
                @Index(
                        name = "idx_lead_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_lead_email",
                        columnList = "email"
                )
        }
)
public class Lead extends BaseAuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "lead_type",
            nullable = false,
            length = 30
    )
    private LeadType leadType;

    @Column(
            name = "full_name",
            nullable = false,
            length = 150
    )
    private String fullName;

    @Column(
            name = "company",
            length = 150
    )
    private String company;

    @Column(
            name = "email",
            nullable = false,
            length = 150
    )
    private String email;

    @Column(
            name = "phone",
            nullable = false,
            length = 20
    )
    private String phone;

    @Column(
            name = "project_type",
            length = 100
    )
    private String projectType;

    @Column(
            name = "budget",
            length = 100
    )
    private String budget;

    @Column(
            name = "message",
            length = 2000
    )
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Setter(AccessLevel.NONE)
    private LeadStatus status;

    /**
     * Initializes a newly created lead.
     *
     * <p>
     * Every newly created lead always starts with
     * {@link LeadStatus#NEW}.
     * </p>
     *
     * @param leadType lead type
     */
    public void initialize(LeadType leadType) {
        this.leadType = leadType;
        this.status = LeadStatus.NEW;
    }

    /**
     * Updates the lifecycle status of this lead.
     * Reserved for future Admin module.
     *
     * @param status new status
     */
    public void updateStatus(LeadStatus status) {
        this.status = java.util.Objects.requireNonNull(
                status,
                "Lead status must not be null."
        );
    }
}