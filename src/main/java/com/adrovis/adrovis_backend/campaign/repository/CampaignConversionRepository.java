package com.adrovis.adrovis_backend.campaign.repository;

import com.adrovis.adrovis_backend.campaign.entity.CampaignConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignConversionRepository
        extends JpaRepository<CampaignConversion, UUID> {

    List<CampaignConversion> findAllByCampaignIdOrderByConvertedAtDesc(
            UUID campaignId
    );

    List<CampaignConversion> findAllByCandidateIdOrderByConvertedAtDesc(
            UUID candidateId
    );

    boolean existsByCampaignIdAndCandidateIdAndConversionType(
            UUID campaignId,
            UUID candidateId,
            String conversionType
    );
}