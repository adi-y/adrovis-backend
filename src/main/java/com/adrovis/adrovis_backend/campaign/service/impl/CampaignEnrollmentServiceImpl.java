package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.entity.Campaign;
import com.adrovis.adrovis_backend.campaign.entity.CampaignRecipient;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyType;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRecipientRepository;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignEnrollmentService;
import com.adrovis.adrovis_backend.campaign.enums.CampaignStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignType;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;
import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;
import com.adrovis.adrovis_backend.program.repository.ProgramConfigurationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CampaignEnrollmentServiceImpl
        implements CampaignEnrollmentService {

    public static final String AUTOMATION_KEY =
            "SOFTWARE_DEVELOPER_INTERNSHIP_REENGAGEMENT";

    private static final String CAMPAIGN_NAME =
            "Software Developer Internship - 4 Week Re-engagement";

    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final ProgramConfigurationRepository
            programConfigurationRepository;
    private final CampaignProperties properties;

    @Override
    @Transactional
    public void enrollOutreach(
            Candidate candidate,
            Instant outreachSentAt
    ) {
        if (candidate == null || outreachSentAt == null) {
            return;
        }

        Campaign campaign = ensureCampaign();

        CampaignRecipient recipient =
                recipientRepository
                        .findByCampaignAutomationKeyAndCandidateId(
                                AUTOMATION_KEY,
                                candidate.getId()
                        )
                        .orElseGet(() ->
                                recipientRepository.save(
                                        new CampaignRecipient(
                                                campaign,
                                                candidate
                                        )
                                )
                        );

        /*
         * Application journey always owns the candidate after they apply.
         */
        if (recipient.getJourneyType()
                == CampaignJourneyType.APPLICATION_FOLLOW_UP) {
            return;
        }

        recipient.startOutreachJourney(
                outreachSentAt,
                calculateFirstFollowUp(outreachSentAt)
        );

        recipientRepository.save(recipient);
    }

    @Override
    @Transactional
    public void startApplicationJourney(
            Candidate candidate,
            Application application
    ) {
        if (candidate == null || application == null) {
            return;
        }

        /*
         * PROGRAM ONLY.
         */
        if (application.getApplicationType()
                != ApplicationType.PROGRAM) {
            return;
        }

        Campaign campaign = ensureCampaign();

        CampaignRecipient recipient =
                recipientRepository
                        .findByCampaignAutomationKeyAndCandidateId(
                                AUTOMATION_KEY,
                                candidate.getId()
                        )
                        .orElseGet(() ->
                                recipientRepository.save(
                                        new CampaignRecipient(
                                                campaign,
                                                candidate
                                        )
                                )
                        );

        Instant createdAt =
                application.getCreatedAt() != null
                        ? application.getCreatedAt()
                        : Instant.now();

        recipient.startApplicationJourney(
                application,
                createdAt,
                calculateFirstFollowUp(createdAt)
        );

        recipientRepository.save(recipient);
    }

    private Campaign ensureCampaign() {

        return campaignRepository
                .findByAutomationKey(AUTOMATION_KEY)
                .orElseGet(() -> {

                    ProgramConfiguration program =
                            programConfigurationRepository
                                    .findByProgramKeyAndActiveTrue(
                                            properties.getProgramKey()
                                    )
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Active internship program configuration not found."
                                            )
                                    );

                    Campaign campaign =
                            new Campaign(
                                    CAMPAIGN_NAME,
                                    CampaignType.INTERNSHIP_REENGAGEMENT,
                                    AUTOMATION_KEY,
                                    program.getProgramName(),
                                    program.getFeeAmount(),
                                    program.getCurrency(),
                                    program.getDuration(),
                                    program.getMode()
                            );

                    campaign.start();

                    try {
                        return campaignRepository.save(campaign);
                    } catch (DataIntegrityViolationException ex) {
                        return campaignRepository
                                .findByAutomationKey(
                                        AUTOMATION_KEY
                                )
                                .orElseThrow(() -> ex);
                    }
                });
    }

    private Instant calculateFirstFollowUp(
            Instant sourceInstant
    ) {
        /*
         * TEST:
         * +2 minutes when weekDurationSeconds = 120.
         */
        if (properties.isTestMode()) {
            return sourceInstant.plusSeconds(
                    properties.getWeekDurationSeconds()
            );
        }

        /*
         * PRODUCTION:
         * next calendar day at 10:00 Asia/Kolkata.
         */
        ZoneId zone =
                ZoneId.of(properties.getTimezone());

        LocalDate nextDay =
                sourceInstant
                        .atZone(zone)
                        .toLocalDate()
                        .plusDays(1);

        LocalTime sendTime =
                LocalTime.parse(
                        properties.getDailySendTime()
                );

        return ZonedDateTime
                .of(nextDay, sendTime, zone)
                .toInstant();
    }
}