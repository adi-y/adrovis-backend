package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignEmailStatus;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignDeliveryService;
import com.adrovis.adrovis_backend.campaign.service.CampaignEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignDeliveryServiceImpl
        implements CampaignDeliveryService {

    private final CampaignEmailRepository campaignEmailRepository;

    private final CampaignEmailService campaignEmailService;

    private final CampaignProperties campaignProperties;

    @Override
    public void deliverQueuedCampaignEmails() {

        int dailyQuota =
                campaignProperties.getBrevoDailyQuota();

        if (dailyQuota <= 0) {

            log.warn(
                    "Brevo campaign delivery skipped because daily quota is <= 0. quota={}",
                    dailyQuota
            );

            return;
        }

        ZoneId zone =
                ZoneId.of(
                        campaignProperties.getTimezone()
                );

        ZonedDateTime nowInZone =
                ZonedDateTime.now(zone);

        LocalDate today =
                nowInZone.toLocalDate();

        Instant startOfDay =
                today
                        .atStartOfDay(zone)
                        .toInstant();

        Instant startOfNextDay =
                today
                        .plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant();

        long sentToday =
                campaignEmailRepository
                        .countByStatusAndSentAtBetween(
                                CampaignEmailStatus.SENT,
                                startOfDay,
                                startOfNextDay
                        );

        long remainingQuota =
                dailyQuota - sentToday;

        if (remainingQuota <= 0) {

            log.info(
                    "Brevo campaign daily quota exhausted. " +
                            "quota={}, sentToday={}",
                    dailyQuota,
                    sentToday
            );

            return;
        }

        int configuredBatchSize =
                campaignProperties
                        .getBrevoDeliveryBatchSize();

        int deliveryLimit =
                (int) Math.min(
                        remainingQuota,
                        Math.max(
                                1,
                                configuredBatchSize
                        )
                );

        List<CampaignEmailStatus> deliverableStatuses =
                List.of(
                        CampaignEmailStatus.QUEUED,
                        CampaignEmailStatus.FAILED
                );

        List<CampaignEmail> pendingEmails;

        if (campaignProperties.isTestMode()) {

            String testEmail =
                    campaignProperties.getTestEmail();

            if (testEmail == null || testEmail.isBlank()) {
                throw new IllegalStateException(
                        "Campaign test mode is enabled but CAMPAIGN_TEST_EMAIL is not configured."
                );
            }

            pendingEmails =
                    campaignEmailRepository
                            .findPendingForDeliveryForTest(
                                    deliverableStatuses,
                                    testEmail.trim(),
                                    Instant.now(),
                                    PageRequest.of(
                                            0,
                                            deliveryLimit
                                    )
                            );

        } else {

            pendingEmails =
                    campaignEmailRepository
                            .findPendingForDelivery(
                                    deliverableStatuses,
                                    Instant.now(),
                                    PageRequest.of(
                                            0,
                                            deliveryLimit
                                    )
                            );
        }

        if (pendingEmails.isEmpty()) {

            log.debug(
                    "No queued campaign emails available for Brevo delivery."
            );

            return;
        }

        log.info(
                "Starting Brevo campaign delivery. " +
                        "dailyQuota={}, sentToday={}, remainingQuota={}, " +
                        "emailsSelected={}",
                dailyQuota,
                sentToday,
                remainingQuota,
                pendingEmails.size()
        );

        for (CampaignEmail email : pendingEmails) {

            try {

                campaignEmailService.sendThroughBrevo(
                        email
                );

            } catch (RuntimeException ex) {

                /*
                 * The individual email service intentionally keeps the
                 * campaign email retryable.
                 *
                 * Do NOT stop the complete batch because one email failed.
                 */
                log.error(
                        "Brevo campaign email delivery failed; " +
                                "email remains retryable. emailId={}, recipient={}",
                        email.getId(),
                        email.getToEmail(),
                        ex
                );
            }
        }

        log.info(
                "Brevo campaign delivery pass completed. " +
                        "selected={}, dailyQuota={}, sentBeforePass={}",
                pendingEmails.size(),
                dailyQuota,
                sentToday
        );
    }
}