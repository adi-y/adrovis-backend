package com.adrovis.adrovis_backend.campaign.repository;

import com.adrovis.adrovis_backend.campaign.entity.Campaign;
import com.adrovis.adrovis_backend.campaign.enums.CampaignStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository
        extends JpaRepository<Campaign, UUID> {

    List<Campaign> findAllByStatus(
            CampaignStatus status
    );

    Optional<Campaign>
    findByAutomationKey(String automationKey);

    Optional<Campaign>
    findFirstByTypeAndStatus(
            CampaignType type,
            CampaignStatus status
    );
}