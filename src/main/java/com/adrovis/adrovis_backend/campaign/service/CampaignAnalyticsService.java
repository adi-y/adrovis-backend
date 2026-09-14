package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.campaign.dto.response.CampaignRecipientAnalyticsResponse;

import java.util.List;

public interface CampaignAnalyticsService {

    List<CampaignRecipientAnalyticsResponse>
    getProgramRecipients();
}