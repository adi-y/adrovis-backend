package com.adrovis.adrovis_backend.campaign.dto.response;

import com.adrovis.adrovis_backend.campaign.entity.Campaign;
import com.adrovis.adrovis_backend.campaign.enums.CampaignStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(

        UUID id,
        String name,
        CampaignType type,
        CampaignStatus status,
        Integer totalWeeks,
        Integer currentWeek,
        Instant scheduledAt,
        Instant startedAt,
        Instant completedAt,
        Instant closedAt,
        String programName,
        BigDecimal feeAmount,
        String currency,
        String duration,
        String mode
) {

    public static CampaignResponse from(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getType(),
                campaign.getStatus(),
                campaign.getTotalWeeks(),
                campaign.getCurrentWeek(),
                campaign.getScheduledAt(),
                campaign.getStartedAt(),
                campaign.getCompletedAt(),
                campaign.getClosedAt(),
                campaign.getProgramNameSnapshot(),
                campaign.getFeeAmountSnapshot(),
                campaign.getCurrencySnapshot(),
                campaign.getDurationSnapshot(),
                campaign.getModeSnapshot()
        );
    }
}