package com.adrovis.adrovis_backend.campaign.entity;

import com.adrovis.adrovis_backend.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "campaign_conversion",
        indexes = {
                @Index(
                        name = "idx_campaign_conversion_campaign",
                        columnList = "campaign_id"
                ),
                @Index(
                        name = "idx_campaign_conversion_candidate",
                        columnList = "candidate_id"
                ),
                @Index(
                        name = "idx_campaign_conversion_application",
                        columnList = "application_id"
                ),
                @Index(
                        name = "idx_campaign_conversion_type",
                        columnList = "conversion_type"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignConversion extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "campaign_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_campaign_conversion_campaign"
            )
    )
    private Campaign campaign;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "conversion_type", nullable = false, length = 40)
    private String conversionType;

    @Column(name = "converted_at", nullable = false)
    private Instant convertedAt;

    public CampaignConversion(
            Campaign campaign,
            UUID candidateId,
            UUID applicationId,
            String conversionType
    ) {
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign is required.");
        }

        if (candidateId == null) {
            throw new IllegalArgumentException(
                    "Candidate ID is required."
            );
        }

        if (conversionType == null || conversionType.isBlank()) {
            throw new IllegalArgumentException(
                    "Conversion type is required."
            );
        }

        this.campaign = campaign;
        this.candidateId = candidateId;
        this.applicationId = applicationId;
        this.conversionType = conversionType.trim().toUpperCase();
        this.convertedAt = Instant.now();
    }
}