package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;

public interface CampaignEmailService {

    void sendThroughBrevo(CampaignEmail campaignEmail);
}