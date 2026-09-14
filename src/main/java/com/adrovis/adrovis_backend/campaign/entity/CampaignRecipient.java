package com.adrovis.adrovis_backend.campaign.entity;

import com.adrovis.adrovis_backend.campaign.enums.CampaignEligibilityStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyType;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;
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

@Getter
@Entity
@Table(
        name = "campaign_recipient",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_campaign_recipient_campaign_candidate",
                        columnNames = {
                                "campaign_id",
                                "candidate_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_campaign_recipient_candidate",
                        columnList = "candidate_id"
                ),
                @Index(
                        name = "idx_campaign_recipient_application",
                        columnList = "application_id"
                ),
                @Index(
                        name = "idx_campaign_recipient_eligibility",
                        columnList = "eligibility_status"
                ),
                @Index(
                        name = "idx_campaign_recipient_due",
                        columnList = "journey_status,next_send_at"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignRecipient extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "campaign_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_campaign_recipient_campaign"
            )
    )
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_campaign_recipient_candidate"
            )
    )
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            foreignKey = @ForeignKey(
                    name = "fk_campaign_recipient_application"
            )
    )
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "eligibility_status",
            nullable = false,
            length = 40
    )
    private CampaignEligibilityStatus eligibilityStatus =
            CampaignEligibilityStatus.NOT_ELIGIBLE;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "journey_type",
            nullable = false,
            length = 30
    )
    private CampaignJourneyType journeyType =
            CampaignJourneyType.OUTREACH_FOLLOW_UP;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "journey_status",
            nullable = false,
            length = 20
    )
    private CampaignJourneyStatus journeyStatus =
            CampaignJourneyStatus.ACTIVE;

    /**
     * 0 before Week 1.
     * 1 after Week 1.
     * ...
     * 4 after Week 4.
     */
    @Column(name = "current_week", nullable = false)
    private Integer currentWeek = 0;

    /**
     * Increments every time the candidate starts a fresh journey.
     *
     * Example:
     * journeyVersion 1 = outreach follow-up
     * journeyVersion 2 = application follow-up
     */
    @Column(name = "journey_version", nullable = false)
    private Integer journeyVersion = 1;

    @Column(name = "next_send_at")
    private Instant nextSendAt;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "last_journey_sent_at")
    private Instant lastJourneySentAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "first_contacted_at")
    private Instant firstContactedAt;

    @Column(name = "last_contacted_at")
    private Instant lastContactedAt;

    @Column(name = "current_application_status", length = 30)
    private String currentApplicationStatus;

    public CampaignRecipient(
            Campaign campaign,
            Candidate candidate
    ) {
        if (campaign == null) {
            throw new IllegalArgumentException(
                    "Campaign is required."
            );
        }

        if (candidate == null) {
            throw new IllegalArgumentException(
                    "Candidate is required."
            );
        }

        this.campaign = campaign;
        this.candidate = candidate;
    }

    public void startOutreachJourney(
            Instant outreachSentAt,
            Instant firstSendAt
    ) {
        this.journeyType =
                CampaignJourneyType.OUTREACH_FOLLOW_UP;

        this.journeyStatus =
                CampaignJourneyStatus.ACTIVE;

        this.journeyVersion =
                Math.max(this.journeyVersion, 1);

        this.currentWeek = 0;
        this.enrolledAt = outreachSentAt;
        this.nextSendAt = firstSendAt;
        this.completedAt = null;
        this.lastJourneySentAt = null;
        this.application = null;
        this.currentApplicationStatus = null;
    }

    public void startApplicationJourney(
            Application application,
            Instant applicationCreatedAt,
            Instant firstSendAt
    ) {
        if (application == null) {
            throw new IllegalArgumentException(
                    "Program application is required."
            );
        }

        this.journeyType =
                CampaignJourneyType.APPLICATION_FOLLOW_UP;

        this.journeyStatus =
                CampaignJourneyStatus.ACTIVE;

        this.journeyVersion =
                this.journeyVersion + 1;

        this.currentWeek = 0;
        this.enrolledAt = applicationCreatedAt;
        this.nextSendAt = firstSendAt;
        this.completedAt = null;
        this.lastJourneySentAt = null;
        this.application = application;
        this.currentApplicationStatus =
                application.getApplicationStatus().name();
    }

    public void updateEligibility(
            CampaignEligibilityStatus eligibilityStatus,
            Application application
    ) {
        if (eligibilityStatus == null) {
            throw new IllegalArgumentException(
                    "Eligibility status is required."
            );
        }

        this.eligibilityStatus = eligibilityStatus;
        this.application = application;

        this.currentApplicationStatus =
                application == null
                        ? null
                        : application.getApplicationStatus().name();
    }

    public void markSuppressed() {
        if (this.journeyStatus
                != CampaignJourneyStatus.UNSUBSCRIBED
                && this.journeyStatus
                != CampaignJourneyStatus.COMPLETED) {

            this.journeyStatus =
                    CampaignJourneyStatus.SUPPRESSED;
        }
    }

    public void markInterested() {
        this.eligibilityStatus =
                CampaignEligibilityStatus.INTERESTED;

        this.journeyStatus =
                CampaignJourneyStatus.COMPLETED;

        this.completedAt =
                Instant.now();

        this.nextSendAt = null;
    }

    public void keepActive() {
        if (this.journeyStatus
                != CampaignJourneyStatus.UNSUBSCRIBED
                && this.journeyStatus
                != CampaignJourneyStatus.COMPLETED) {

            this.journeyStatus =
                    CampaignJourneyStatus.ACTIVE;
        }
    }

    public void markCompleted() {
        this.journeyStatus =
                CampaignJourneyStatus.COMPLETED;

        this.completedAt = Instant.now();
        this.nextSendAt = null;
    }

    public void markUnsubscribed() {
        this.journeyStatus =
                CampaignJourneyStatus.UNSUBSCRIBED;

        this.nextSendAt = null;
    }

    public void advanceAfterAttempt(
            int completedWeek,
            Instant nextSendAt
    ) {
        this.currentWeek = completedWeek;
        this.lastJourneySentAt = Instant.now();
        this.lastContactedAt = Instant.now();
        this.nextSendAt = nextSendAt;
        this.journeyStatus = CampaignJourneyStatus.ACTIVE;
    }

    public void advanceAfterSuppression(
            int completedWeek,
            Instant nextSendAt
    ) {
        this.currentWeek = completedWeek;
        this.nextSendAt = nextSendAt;

        if (this.journeyStatus
                != CampaignJourneyStatus.UNSUBSCRIBED
                && this.journeyStatus
                != CampaignJourneyStatus.COMPLETED) {

            this.journeyStatus =
                    CampaignJourneyStatus.SUPPRESSED;
        }
    }

    public void markContacted() {
        Instant now = Instant.now();

        if (this.firstContactedAt == null) {
            this.firstContactedAt = now;
        }

        this.lastContactedAt = now;
    }
}