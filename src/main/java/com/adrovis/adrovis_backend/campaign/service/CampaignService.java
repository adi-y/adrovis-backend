package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.campaign.dto.request.CreateCampaignRequest;
import com.adrovis.adrovis_backend.campaign.entity.Campaign;

import java.util.List;
import java.util.UUID;

public interface CampaignService {

    Campaign create(CreateCampaignRequest request);

    List<Campaign> findAll();

    Campaign get(UUID campaignId);

    void close(UUID campaignId);
}