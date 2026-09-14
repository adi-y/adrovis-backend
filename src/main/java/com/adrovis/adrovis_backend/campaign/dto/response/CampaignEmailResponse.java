package com.adrovis.adrovis_backend.campaign.dto.response;

import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailType;

import java.time.Instant;
import java.util.UUID;

public record CampaignEmailResponse(

        UUID id,
        UUID candidateId,
        UUID applicationId,
        Integer weekNumber,
        CampaignEmailType emailType,
        CampaignEmailStatus status,
        String toEmail,
        String subject,
        Instant scheduledAt,
        Instant sentAt,
        Instant openedAt,
        Instant clickedAt,
        Instant failedAt,
        String failureReason,
        String providerMessageId
) {

    public static CampaignEmailResponse from(
            CampaignEmail email
    ) {
        return new CampaignEmailResponse(
                email.getId(),
                email.getCandidateId(),
                email.getApplicationId(),
                email.getWeekNumber(),
                email.getEmailType(),
                email.getStatus(),
                email.getToEmail(),
                email.getSubject(),
                email.getScheduledAt(),
                email.getSentAt(),
                email.getOpenedAt(),
                email.getClickedAt(),
                email.getFailedAt(),
                email.getFailureReason(),
                email.getProviderMessageId()
        );
    }
}