package com.adrovis.adrovis_backend.campaign.repository;

import com.adrovis.adrovis_backend.campaign.entity.CampaignRecipient;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRecipientRepository
        extends JpaRepository<CampaignRecipient, UUID> {

    Optional<CampaignRecipient>
    findByCampaignIdAndCandidateId(
            UUID campaignId,
            UUID candidateId
    );

    boolean existsByCampaignIdAndCandidateId(
            UUID campaignId,
            UUID candidateId
    );

    Optional<CampaignRecipient>
    findByCampaignAutomationKeyAndCandidateId(
            String automationKey,
            UUID candidateId
    );

    @Query("""
            select r
            from CampaignRecipient r
            join fetch r.candidate c
            join fetch r.campaign campaign
            where campaign.automationKey = :automationKey
              and r.journeyStatus in (
                    com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus.ACTIVE,
                    com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus.SUPPRESSED
              )
              and r.nextSendAt is not null
              and r.nextSendAt <= :now
            order by r.nextSendAt asc
            """)
    List<CampaignRecipient> findDueRecipients(
            @Param("automationKey") String automationKey,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
            select r
            from CampaignRecipient r
            join fetch r.candidate c
            join fetch r.campaign campaign
            where campaign.automationKey = :automationKey
              and c.normalizedEmail = :normalizedEmail
              and r.journeyStatus in (
                    com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus.ACTIVE,
                    com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus.SUPPRESSED
              )
              and r.nextSendAt is not null
              and r.nextSendAt <= :now
            order by r.nextSendAt asc
            """)
    List<CampaignRecipient> findDueRecipientsForTest(
            @Param("automationKey") String automationKey,
            @Param("normalizedEmail") String normalizedEmail,
            @Param("now") Instant now,
            Pageable pageable
    );

    List<CampaignRecipient>
    findAllByCampaignAutomationKeyOrderByCreatedAtDesc(
            String automationKey
    );
}