package com.adrovis.adrovis_backend.campaign.scheduler;

import com.adrovis.adrovis_backend.campaign.service.CampaignDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignDeliveryScheduler {

    private final CampaignDeliveryService deliveryService;

    /**
     * Checks the persistent campaign email queue every hour.
     *
     * The service itself enforces the 300/day Brevo quota.
     */
    @Scheduled(
            fixedDelayString =
                    "${app.campaign.brevo-delivery-scheduler-delay-ms:3600000}"
    )
    public void deliverCampaignEmails() {

        try {

            deliveryService.deliverQueuedCampaignEmails();

        } catch (RuntimeException ex) {

            log.error(
                    "Brevo campaign delivery scheduler failed.",
                    ex
            );
        }
    }
}