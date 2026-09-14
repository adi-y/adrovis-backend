package com.adrovis.adrovis_backend.campaign.dto.response;

import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.entity.CampaignRecipient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CampaignRecipientAnalyticsResponse(

        UUID candidateId,

        String name,

        String email,

        boolean unsubscribed,

        Instant outreachSentAt,

        UUID applicationId,

        String applicationReference,

        String applicationStatus,

        String journeyType,

        String journeyStatus,

        Integer journeyVersion,

        Integer currentWeek,

        Instant enrolledAt,

        Instant nextSendAt,

        Instant lastJourneySentAt,

        Instant completedAt,

        List<CampaignEmailResponse> emails
) {

    public static CampaignRecipientAnalyticsResponse from(
            CampaignRecipient recipient,
            String applicationReference,
            Instant outreachSentAt,
            List<CampaignEmail> emails
    ) {

        return new CampaignRecipientAnalyticsResponse(
                recipient.getCandidate().getId(),
                recipient.getCandidate().getName(),
                recipient.getCandidate().getEmail(),
                recipient.getCandidate().isMarketingOptOut(),
                outreachSentAt,
                recipient.getApplication() == null
                        ? null
                        : recipient.getApplication().getId(),
                applicationReference,
                recipient.getCurrentApplicationStatus(),
                recipient.getJourneyType().name(),
                recipient.getJourneyStatus().name(),
                recipient.getJourneyVersion(),
                recipient.getCurrentWeek(),
                recipient.getEnrolledAt(),
                recipient.getNextSendAt(),
                recipient.getLastJourneySentAt(),
                recipient.getCompletedAt(),
                emails.stream()
                        .map(CampaignEmailResponse::from)
                        .toList()
        );
    }
}