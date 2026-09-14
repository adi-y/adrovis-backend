package com.adrovis.adrovis_backend.campaign.dto.response;

import com.adrovis.adrovis_backend.campaign.entity.CampaignResponse;
import com.adrovis.adrovis_backend.campaign.enums.CampaignResponseType;

import java.time.Instant;
import java.util.UUID;

public record CampaignResponseItem(

        UUID id,
        UUID candidateId,
        UUID applicationId,
        CampaignResponseType responseType,
        String responseText,
        Instant receivedAt
) {

    public static CampaignResponseItem from(
            CampaignResponse response
    ) {
        return new CampaignResponseItem(
                response.getId(),
                response.getCandidateId(),
                response.getApplicationId(),
                response.getResponseType(),
                response.getResponseText(),
                response.getReceivedAt()
        );
    }
}