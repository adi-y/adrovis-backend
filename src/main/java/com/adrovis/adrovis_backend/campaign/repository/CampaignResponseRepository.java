package com.adrovis.adrovis_backend.campaign.repository;

import com.adrovis.adrovis_backend.campaign.entity.CampaignResponse;
import com.adrovis.adrovis_backend.campaign.enums.CampaignResponseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignResponseRepository
        extends JpaRepository<CampaignResponse, UUID> {

    List<CampaignResponse> findAllByCandidateIdOrderByReceivedAtDesc(
            UUID candidateId
    );

    List<CampaignResponse> findAllByCampaignIdAndResponseType(
            UUID campaignId,
            CampaignResponseType responseType
    );

    boolean existsByCampaignIdAndCandidateIdAndResponseType(
            UUID campaignId,
            UUID candidateId,
            CampaignResponseType responseType
    );
}