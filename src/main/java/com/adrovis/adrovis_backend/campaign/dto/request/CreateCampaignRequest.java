package com.adrovis.adrovis_backend.campaign.dto.request;

import com.adrovis.adrovis_backend.campaign.enums.CampaignType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateCampaignRequest(

        @NotBlank(message = "Campaign name is required.")
        @Size(max = 150)
        String name,

        @NotNull(message = "Campaign type is required.")
        CampaignType type,

        @NotNull(message = "scheduledAt is required.")
        @FutureOrPresent(message = "scheduledAt must be in the future.")
        Instant scheduledAt
) {
}