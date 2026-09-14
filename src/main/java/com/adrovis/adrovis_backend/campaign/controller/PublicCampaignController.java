package com.adrovis.adrovis_backend.campaign.controller;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.enums.CampaignJourneyStatus;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRecipientRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignLinkService;
import com.adrovis.adrovis_backend.campaign.service.CampaignTrackingService;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;
import com.adrovis.adrovis_backend.candidate.repository.CandidateRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/campaigns")
public class PublicCampaignController {

    private final CampaignRecipientRepository campaignRecipientRepository;

    private static final String INTERNSHIP_URL =
            "https://www.adrovis.com/careers/internship";

    private static final byte[] TRANSPARENT_GIF = new byte[]{
            71, 73, 70, 56, 57, 97,
            1, 0, 1, 0, -128, 0,
            0, 0, 0, 0, -1, -1,
            -1, 33, -7, 4, 1,
            0, 0, 0, 0, 44,
            0, 0, 0, 0, 1, 0,
            1, 0, 0, 2, 2,
            68, 1, 0, 59
    };

    private final CampaignLinkService linkService;
    private final CandidateRepository candidateRepository;
    private final CampaignTrackingService trackingService;
    private final CampaignEmailRepository campaignEmailRepository;
    private final CampaignProperties campaignProperties;

    @GetMapping("/unsubscribe")
    public void unsubscribe(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {

        try {

            UUID candidateId =
                    linkService.validateUnsubscribeToken(
                            token
                    );

            Candidate candidate =
                    candidateRepository
                            .findById(candidateId)
                            .orElse(null);

            if (candidate != null) {
                candidate.optOutOfMarketing();
                candidateRepository.save(candidate);
            }

        } catch (IllegalArgumentException ex) {

            /*
             * Invalid/expired unsubscribe links must never
             * accidentally unsubscribe anybody.
             *
             * They simply land on the normal internship page.
             */
        }

        response.sendRedirect(
                INTERNSHIP_URL
        );
    }

    @GetMapping("/open")
    public void open(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {

        try {

            UUID campaignEmailId =
                    linkService.validateTrackingToken(
                            token,
                            "OPEN"
                    );

            trackingService.markOpened(
                    campaignEmailId
            );

        } catch (IllegalArgumentException ex) {

            /*
             * Tracking is non-critical.
             *
             * If the token is invalid/expired, still return
             * a valid 1x1 image so mail clients do not receive
             * a 500 from the tracking endpoint.
             */
        }

        writeTransparentGif(
                response
        );
    }

    @GetMapping("/click")
    public void click(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {

        try {

            UUID campaignEmailId =
                    linkService.validateTrackingToken(
                            token,
                            "CLICK"
                    );

            trackingService.markClicked(
                    campaignEmailId
            );

            CampaignEmail email =
                    campaignEmailRepository
                            .findById(campaignEmailId)
                            .orElse(null);

            if (email == null
                    || email.getCtaUrl() == null
                    || email.getCtaUrl().isBlank()) {

                response.sendRedirect(
                        INTERNSHIP_URL
                );

                return;
            }

            response.sendRedirect(
                    email.getCtaUrl()
            );

        } catch (IllegalArgumentException ex) {

            /*
             * Tracking-link failure must not expose an internal
             * stack trace or return a 500 to a candidate.
             */
            response.sendRedirect(
                    INTERNSHIP_URL
            );
        }
    }

    private void writeTransparentGif(
            HttpServletResponse response
    ) throws IOException {

        response.setHeader(
                HttpHeaders.CACHE_CONTROL,
                "no-store, no-cache, must-revalidate, max-age=0"
        );

        response.setContentType(
                MediaType.IMAGE_GIF_VALUE
        );

        response.getOutputStream()
                .write(TRANSPARENT_GIF);
    }


    @Transactional
    @GetMapping("/interest")
    public void markInterested(
            @RequestParam UUID recipientId,
            HttpServletResponse response
    ) throws IOException {

        campaignRecipientRepository
                .findById(recipientId)
                .ifPresent(recipient -> {

                    if (recipient.getJourneyStatus() != CampaignJourneyStatus.COMPLETED
                            && recipient.getJourneyStatus() != CampaignJourneyStatus.UNSUBSCRIBED) {

                        recipient.markInterested();
                        campaignRecipientRepository.save(recipient);
                    }
                });

        response.sendRedirect(
                campaignProperties.getFrontendBaseUrl()
                        + "/careers/internship/confirmed?recipientId="
                        + recipientId
        );
    }
}