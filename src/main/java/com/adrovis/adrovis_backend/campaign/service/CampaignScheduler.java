package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.campaign.service.CampaignDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignScheduler {

    private final CampaignDispatchService dispatchService;

    @Scheduled(
            fixedDelayString = "${app.campaign.scheduler-delay-ms:3600000}"
    )
    public void dispatchDueCampaigns() {

        try {

            dispatchService.dispatchDueRecipients();

        } catch (RuntimeException ex) {

            log.error(
                    "Automated internship campaign scheduler failed.",
                    ex
            );
        }
    }
}