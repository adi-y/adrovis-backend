package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.campaign.entity.Campaign;

import java.util.UUID;

public interface CampaignDispatchService {

    void dispatchDueRecipients();

    void dispatchDueRecipients(UUID campaignId);

    /**
     * Backward-compatible admin/manual endpoint.
     */
    default void dispatchNextDueWeek(Campaign campaign) {
        if (campaign != null) {
            dispatchDueRecipients(campaign.getId());
        }
    }
}