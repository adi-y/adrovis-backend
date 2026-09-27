package com.adrovis.adrovis_backend.campaign.repository;

import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    @Query("""
            select count(e)
            from CampaignEmail e
            where e.status = :status
              and e.sentAt >= :start
              and e.sentAt < :end
            """)
    long countByStatusAndSentAtBetween(
            @Param("status") CampaignEmailStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
            select e
            from CampaignEmail e
            where e.status in :statuses
              and (
                    e.scheduledAt is null
                    or e.scheduledAt <= :now
              )
            order by
                e.scheduledAt asc,
                e.createdAt asc
            """)
    List<CampaignEmail> findPendingForDelivery(
            @Param("statuses") List<CampaignEmailStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
        select e
        from CampaignEmail e
        where e.status in :statuses
          and lower(e.toEmail) = lower(:testEmail)
          and (
                e.scheduledAt is null
                or e.scheduledAt <= :now
          )
        order by
            e.scheduledAt asc,
            e.createdAt asc
        """)
    List<CampaignEmail> findPendingForDeliveryForTest(
            @Param("statuses") List<CampaignEmailStatus> statuses,
            @Param("testEmail") String testEmail,
            @Param("now") Instant now,
            Pageable pageable
    );
}