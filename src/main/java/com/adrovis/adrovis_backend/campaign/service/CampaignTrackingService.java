package com.adrovis.adrovis_backend.campaign.service;

import java.util.UUID;

public interface CampaignTrackingService {

    void markOpened(UUID campaignEmailId);

    void markClicked(UUID campaignEmailId);
}