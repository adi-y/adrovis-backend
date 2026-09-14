package com.adrovis.adrovis_backend.campaign.entity;

import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailType;
import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "campaign_email",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_campaign_email_recipient_journey_week",
                        columnNames = {
                                "campaign_recipient_id",
                                "journey_version",
                                "week_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_campaign_email_campaign",
                        columnList = "campaign_id"
                ),
                @Index(
                        name = "idx_campaign_email_candidate",
                        columnList = "candidate_id"
                ),
                @Index(
                        name = "idx_campaign_email_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_campaign_email_provider_id",
                        columnList = "provider_message_id"
                ),
                @Index(
                        name = "idx_campaign_email_recipient_journey",
                        columnList = "campaign_recipient_id,journey_version"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignEmail extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "campaign_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_campaign_email_campaign"
            )
    )
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "campaign_recipient_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_campaign_email_recipient"
            )
    )
    private CampaignRecipient campaignRecipient;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "journey_version", nullable = false)
    private Integer journeyVersion;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private CampaignEmailType emailType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CampaignEmailStatus status =
            CampaignEmailStatus.QUEUED;

    @Column(name = "to_email", nullable = false, length = 255)
    private String toEmail;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Column(name = "cta_url", length = 2000)
    private String ctaUrl;

    @Column(name = "unsubscribe_url", length = 2000)
    private String unsubscribeUrl;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "clicked_at")
    private Instant clickedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    public CampaignEmail(
            Campaign campaign,
            CampaignRecipient campaignRecipient,
            UUID candidateId,
            UUID applicationId,
            Integer journeyVersion,
            Integer weekNumber,
            CampaignEmailType emailType,
            String toEmail,
            String subject,
            String templateKey,
            String ctaUrl,
            String unsubscribeUrl,
            Instant scheduledAt
    ) {
        if (campaign == null) {
            throw new IllegalArgumentException(
                    "Campaign is required."
            );
        }

        if (campaignRecipient == null) {
            throw new IllegalArgumentException(
                    "Campaign recipient is required."
            );
        }

        if (candidateId == null) {
            throw new IllegalArgumentException(
                    "Candidate ID is required."
            );
        }

        if (journeyVersion == null || journeyVersion < 1) {
            throw new IllegalArgumentException(
                    "Journey version must be greater than zero."
            );
        }

        if (weekNumber == null
                || weekNumber < 1
                || weekNumber > 4) {

            throw new IllegalArgumentException(
                    "Campaign week must be between 1 and 4."
            );
        }

        if (emailType == null) {
            throw new IllegalArgumentException(
                    "Campaign email type is required."
            );
        }

        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is required."
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Email subject is required."
            );
        }

        if (templateKey == null || templateKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Template key is required."
            );
        }

        this.campaign = campaign;
        this.campaignRecipient = campaignRecipient;
        this.candidateId = candidateId;
        this.applicationId = applicationId;
        this.journeyVersion = journeyVersion;
        this.weekNumber = weekNumber;
        this.emailType = emailType;
        this.toEmail = toEmail.trim();
        this.subject = subject.trim();
        this.templateKey = templateKey.trim();
        this.ctaUrl = ctaUrl;
        this.unsubscribeUrl = unsubscribeUrl;
        this.scheduledAt = scheduledAt;
    }

    public void markSent(String providerMessageId) {
        this.status = CampaignEmailStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = Instant.now();
        this.failedAt = null;
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = CampaignEmailStatus.FAILED;
        this.failedAt = Instant.now();
        this.failureReason =
                reason == null
                        ? "Unknown email provider failure."
                        : reason;
    }

    public void markSkipped(String reason) {
        this.status = CampaignEmailStatus.SKIPPED;
        this.failureReason = reason;
    }

    public void markCancelled(String reason) {
        this.status = CampaignEmailStatus.CANCELLED;
        this.failureReason = reason;
    }

    public void markOpened() {
        if (this.openedAt == null) {
            this.openedAt = Instant.now();
        }
    }

    public void markClicked() {
        if (this.clickedAt == null) {
            this.clickedAt = Instant.now();
        }
    }
}