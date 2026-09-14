package com.adrovis.adrovis_backend.campaign.repository;

import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignEmailRepository
        extends JpaRepository<CampaignEmail, UUID> {

    Optional<CampaignEmail>
    findByCampaignRecipientIdAndJourneyVersionAndWeekNumber(
            UUID campaignRecipientId,
            Integer journeyVersion,
            Integer weekNumber
    );

    List<CampaignEmail>
    findAllByCampaignIdAndStatus(
            UUID campaignId,
            CampaignEmailStatus status
    );

    List<CampaignEmail>
    findAllByCandidateIdOrderByCreatedAtDesc(
            UUID candidateId
    );

    List<CampaignEmail>
    findAllByCampaignIdOrderByCreatedAtDesc(
            UUID campaignId
    );
}