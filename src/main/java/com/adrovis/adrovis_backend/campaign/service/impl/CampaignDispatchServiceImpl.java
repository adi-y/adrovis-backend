package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.entity.Campaign;
import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.entity.CampaignRecipient;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEligibilityStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailType;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRecipientRepository;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignDispatchService;
import com.adrovis.adrovis_backend.campaign.service.CampaignEligibilityService;
import com.adrovis.adrovis_backend.campaign.service.CampaignEmailService;
import com.adrovis.adrovis_backend.campaign.service.CampaignLinkService;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignDispatchServiceImpl
        implements CampaignDispatchService {

    /**
     * Master campaign switch.
     *
     * false = no automated campaign processing.
     * true  = normal dispatcher behaviour, subject to testMode.
     *
     * This is read directly from configuration so CampaignProperties and
     * CampaignScheduler do not need to change.
     */
    @Value("${app.campaign.enabled:false}")
    private boolean enabled;

    private final CampaignProperties properties;
    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final CampaignEmailRepository emailRepository;
    private final ApplicationRepository applicationRepository;
    private final CampaignEligibilityService eligibilityService;
    private final CampaignLinkService linkService;
    private final CampaignEmailService emailService;

    @Override
    public void dispatchDueRecipients() {

        if (!enabled) {
            return;
        }

        Campaign campaign =
                campaignRepository
                        .findByAutomationKey(
                                CampaignEnrollmentServiceImpl.AUTOMATION_KEY
                        )
                        .orElse(null);

        if (campaign == null) {
            return;
        }

        dispatchDueRecipients(campaign.getId());
    }

    @Override
    @Transactional
    public void dispatchDueRecipients(
            UUID campaignId
    ) {
        if (!enabled) {
            return;
        }

        Campaign campaign =
                campaignRepository
                        .findById(campaignId)
                        .orElse(null);

        if (campaign == null) {
            return;
        }

        if (campaign.getStatus()
                == com.adrovis.adrovis_backend.campaign.enums.CampaignStatus.CLOSED
                || campaign.getStatus()
                == com.adrovis.adrovis_backend.campaign.enums.CampaignStatus.CANCELLED) {
            return;
        }

        campaign.start();

        Instant now = Instant.now();

        List<CampaignRecipient> recipients;

        if (properties.isTestMode()) {

            String testEmail =
                    normalize(properties.getTestEmail());

            if (testEmail.isBlank()) {
                throw new IllegalStateException(
                        "Campaign test mode is enabled but CAMPAIGN_TEST_EMAIL is not configured."
                );
            }

            recipients =
                    recipientRepository.findDueRecipientsForTest(
                            campaign.getAutomationKey(),
                            testEmail,
                            now,
                            PageRequest.of(
                                    0,
                                    properties.getBatchSize()
                            )
                    );

        } else {

            recipients =
                    recipientRepository.findDueRecipients(
                            campaign.getAutomationKey(),
                            now,
                            PageRequest.of(
                                    0,
                                    properties.getBatchSize()
                            )
                    );
        }

        for (CampaignRecipient recipient : recipients) {

            try {
                dispatchOne(
                        campaign,
                        recipient,
                        now
                );
            } catch (RuntimeException ex) {

                log.error(
                        "Campaign recipient dispatch failed. recipientId={}, candidateId={}",
                        recipient.getId(),
                        recipient.getCandidate().getId(),
                        ex
                );
            }
        }
    }

    private void dispatchOne(
            Campaign campaign,
            CampaignRecipient recipient,
            Instant now
    ) {
        Candidate candidate =
                recipient.getCandidate();

        if (candidate.isMarketingOptOut()) {

            recipient.updateEligibility(
                    CampaignEligibilityStatus.UNSUBSCRIBED,
                    recipient.getApplication()
            );

            recipient.markUnsubscribed();

            recipientRepository.save(recipient);

            return;
        }

        Application application =
                applicationRepository
                        .findTopByApplicantEmailIgnoreCaseAndApplicationTypeOrderByCreatedAtDesc(
                                candidate.getEmail(),
                                ApplicationType.PROGRAM
                        )
                        .orElse(null);

        /*
         * PROGRAM ONLY:
         * Ignore JOB applications completely.
         */
        CampaignEligibilityService.EligibilityResult eligibility =
                eligibilityService.evaluate(
                        candidate,
                        application
                );

        recipient.updateEligibility(
                eligibility.status(),
                application
        );

        int nextWeek =
                recipient.getCurrentWeek() + 1;

        if (nextWeek > 4) {
            recipient.markCompleted();
            recipientRepository.save(recipient);
            return;
        }

        CampaignEmailType emailType =
                emailTypeFor(nextWeek);

        /*
         * Suppressed statuses still move through the candidate's
         * four-week schedule, but no marketing email is sent.
         */
        if (!isSendable(eligibility.status())) {

            recipient.markSuppressed();

            CampaignEmail skipped =
                    emailRepository
                            .findByCampaignRecipientIdAndJourneyVersionAndWeekNumber(
                                    recipient.getId(),
                                    recipient.getJourneyVersion(),
                                    nextWeek
                            )
                            .orElseGet(() ->
                                    new CampaignEmail(
                                            campaign,
                                            recipient,
                                            candidate.getId(),
                                            application == null
                                                    ? null
                                                    : application.getId(),
                                            recipient.getJourneyVersion(),
                                            nextWeek,
                                            emailType,
                                            candidate.getEmail(),
                                            subjectFor(emailType),
                                            templateFor(emailType),
                                            null,
                                            unsubscribeUrl(candidate),
                                            now
                                    )
                            );

            skipped.markSkipped(
                    eligibility.status().name()
            );

            emailRepository.save(skipped);

            if (eligibility.status()
                    == CampaignEligibilityStatus.UNSUBSCRIBED) {

                recipient.markUnsubscribed();

            } else if (nextWeek >= 4) {

                recipient.markCompleted();

            } else {

                recipient.advanceAfterSuppression(
                        nextWeek,
                        calculateNextWeekSendAt(
                                now
                        )
                );
            }

            recipientRepository.save(recipient);

            return;
        }

        CampaignEmail email =
                emailRepository
                        .findByCampaignRecipientIdAndJourneyVersionAndWeekNumber(
                                recipient.getId(),
                                recipient.getJourneyVersion(),
                                nextWeek
                        )
                        .orElseGet(() ->
                                buildEmail(
                                        campaign,
                                        recipient,
                                        candidate,
                                        application,
                                        nextWeek,
                                        emailType,
                                        now
                                )
                        );

        if (email.getStatus() == CampaignEmailStatus.SENT) {
            return;
        }

        if (email.getStatus() == CampaignEmailStatus.FAILED) {
            return;
        }

        /*
         * Queue exactly one row for this candidate + journey + week.
         * Database uniqueness protects against duplicate scheduler execution.
         */
        try {
            emailRepository.saveAndFlush(email);
        } catch (DataIntegrityViolationException ex) {

            log.debug(
                    "Campaign email already queued by another dispatcher. recipientId={}, journeyVersion={}, week={}",
                    recipient.getId(),
                    recipient.getJourneyVersion(),
                    nextWeek
            );

            return;
        }

        recipient.markContacted();

        if (nextWeek >= 4) {
            recipient.markCompleted();
        } else {
            recipient.advanceAfterAttempt(
                    nextWeek,
                    calculateNextWeekSendAt(now)
            );
        }

        /*
         * Mark the row queued before leaving the transaction.
         * The async sender owns provider delivery state.
         */
        recipientRepository.save(recipient);

        emailService.sendAsync(email);
    }

    private boolean isSendable(
            CampaignEligibilityStatus status
    ) {
        return status
                == CampaignEligibilityStatus.ELIGIBLE
                || status
                == CampaignEligibilityStatus.APPLICATION_PENDING
                || status
                == CampaignEligibilityStatus.APPLICATION_SUBMITTED;
    }

    private CampaignEmail buildEmail(
            Campaign campaign,
            CampaignRecipient recipient,
            Candidate candidate,
            Application application,
            int week,
            CampaignEmailType emailType,
            Instant scheduledAt
    ) {

        String ctaUrl =
                buildCtaUrl(
                        candidate,
                        application
                );

        return new CampaignEmail(
                campaign,
                recipient,
                candidate.getId(),
                application == null
                        ? null
                        : application.getId(),
                recipient.getJourneyVersion(),
                week,
                emailType,
                candidate.getEmail(),
                subjectFor(emailType),
                templateFor(emailType),
                ctaUrl,
                unsubscribeUrl(candidate),
                scheduledAt
        );
    }

    private Instant calculateNextWeekSendAt(
            Instant source
    ) {

        if (properties.isTestMode()) {
            return source.plusSeconds(
                    properties.getWeekDurationSeconds()
            );
        }

        ZoneId zone =
                ZoneId.of(properties.getTimezone());

        ZonedDateTime current =
                source.atZone(zone);

        LocalDate nextDate =
                current.toLocalDate()
                        .plusDays(7);

        LocalTime sendTime =
                LocalTime.parse(
                        properties.getDailySendTime()
                );

        return ZonedDateTime.of(
                        nextDate,
                        sendTime,
                        zone
                )
                .toInstant();
    }

    private CampaignEmailType emailTypeFor(
            int week
    ) {
        return switch (week) {
            case 1 ->
                    CampaignEmailType.WEEK_1_REENGAGEMENT;

            case 2 ->
                    CampaignEmailType.WEEK_2_EXPERIENCE;

            case 3 ->
                    CampaignEmailType.WEEK_3_ENGINEERING_WORKFLOW;

            case 4 ->
                    CampaignEmailType.WEEK_4_OUTCOMES;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported campaign week: " + week
                    );
        };
    }

    private String subjectFor(
            CampaignEmailType type
    ) {
        return switch (type) {

            case WEEK_1_REENGAGEMENT ->
                    "We're still hiring - Software Developer Internship at ADROVIS";

            case WEEK_2_EXPERIENCE ->
                    "What you'll actually experience at ADROVIS";

            case WEEK_3_ENGINEERING_WORKFLOW ->
                    "A closer look at the engineering experience at ADROVIS";

            case WEEK_4_OUTCOMES ->
                    "Your final ADROVIS internship follow-up";
        };
    }

    private String templateFor(
            CampaignEmailType type
    ) {
        return type.name();
    }

    private String buildCtaUrl(
            Candidate candidate,
            Application application
    ) {
        /*
         * No application:
         * normal internship application page.
         */
        if (application == null) {
            return frontendBaseUrl()
                    + "/careers/internship";
        }

        /*
         * PROGRAM PENDING:
         * secure continuation link.
         */
        if (application.getApplicationType()
                == ApplicationType.PROGRAM
                && application.getApplicationStatus()
                == ApplicationStatus.PENDING) {

            String token =
                    linkService.generateContinuationToken(
                            application.getApplicationId(),
                            candidate.getEmail()
                    );

            return frontendBaseUrl()
                    + "/careers/internship"
                    + "?applicationId="
                    + url(application.getApplicationId())
                    + "&token="
                    + url(token);
        }

        /*
         * PROGRAM SUBMITTED:
         * simple YES confirmation email.
         */
        if (application.getApplicationType()
                == ApplicationType.PROGRAM
                && application.getApplicationStatus()
                == ApplicationStatus.SUBMITTED) {

            String subject =
                    "ADROVIS Internship - "
                            + application.getApplicationId();

            String body =
                    "YES\n"
                            + "Application ID: "
                            + application.getApplicationId();

            return "mailto:"
                    + properties.getReplyToEmail()
                    + "?subject="
                    + url(subject)
                    + "&body="
                    + url(body);
        }

        return null;
    }

    private String frontendBaseUrl() {
        String configuredFrontend =
                properties.getFrontendBaseUrl();

        if (configuredFrontend != null
                && !configuredFrontend.isBlank()) {
            return trimTrailingSlash(configuredFrontend);
        }

        return trimTrailingSlash(properties.getBaseUrl());
    }

    private String trimTrailingSlash(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "https://www.adrovis.com";
        }

        return value.replaceAll("/+$", "");
    }

    private String unsubscribeUrl(
            Candidate candidate
    ) {
        String token =
                linkService.generateUnsubscribeToken(
                        candidate.getId()
                );

        return properties.getPublicBaseUrl()
                + "/api/v1/public/campaigns/unsubscribe?token="
                + url(token);
    }

    private String normalize(
            String email
    ) {
        return email == null
                ? ""
                : email.trim().toLowerCase();
    }

    private String url(
            String value
    ) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
