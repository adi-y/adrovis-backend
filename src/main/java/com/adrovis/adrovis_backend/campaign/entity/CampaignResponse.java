package com.adrovis.adrovis_backend.campaign.entity;

import com.adrovis.adrovis_backend.campaign.enums.CampaignResponseType;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "campaign_response",
        indexes = {
                @Index(
                        name = "idx_campaign_response_candidate",
                        columnList = "candidate_id"
                ),
                @Index(
                        name = "idx_campaign_response_application",
                        columnList = "application_id"
                ),
                @Index(
                        name = "idx_campaign_response_type",
                        columnList = "response_type"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignResponse extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "campaign_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_campaign_response_campaign"
            )
    )
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "campaign_email_id",
            foreignKey = @ForeignKey(
                    name = "fk_campaign_response_email"
            )
    )
    private CampaignEmail campaignEmail;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", nullable = false, length = 30)
    private CampaignResponseType responseType;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public CampaignResponse(
            Campaign campaign,
            CampaignEmail campaignEmail,
            UUID candidateId,
            UUID applicationId,
            CampaignResponseType responseType,
            String responseText
    ) {
        this.campaign = campaign;
        this.campaignEmail = campaignEmail;
        this.candidateId = candidateId;
        this.applicationId = applicationId;
        this.responseType = responseType;
        this.responseText = responseText;
        this.receivedAt = Instant.now();
    }
}