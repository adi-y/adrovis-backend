package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.dto.response.CampaignRecipientAnalyticsResponse;
import com.adrovis.adrovis_backend.campaign.entity.CampaignEmail;
import com.adrovis.adrovis_backend.campaign.entity.CampaignRecipient;
import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRecipientRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignAnalyticsService;
import com.adrovis.adrovis_backend.career.repository.CandidateOutreachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignAnalyticsServiceImpl
        implements CampaignAnalyticsService {

    public static final String PROGRAM_AUTOMATION_KEY =
            CampaignEnrollmentServiceImpl.AUTOMATION_KEY;

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignEmailRepository emailRepository;
    private final CandidateOutreachRepository
            candidateOutreachRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CampaignRecipientAnalyticsResponse>
    getProgramRecipients() {

        List<CampaignRecipient> recipients =
                recipientRepository
                        .findAllByCampaignAutomationKeyOrderByCreatedAtDesc(
                                PROGRAM_AUTOMATION_KEY
                        );

        return recipients.stream()
                .map(this::toResponse)
                .toList();
    }

    private CampaignRecipientAnalyticsResponse
    toResponse(
            CampaignRecipient recipient
    ) {

        var application =
                recipient.getApplication();

        String applicationReference =
                application == null
                        ? null
                        : application.getApplicationId();

        List<CampaignEmail> emails =
                emailRepository
                        .findAllByCandidateIdOrderByCreatedAtDesc(
                                recipient.getCandidate().getId()
                        )
                        .stream()
                        .filter(email ->
                                email.getCampaign()
                                        .getId()
                                        .equals(
                                                recipient
                                                        .getCampaign()
                                                        .getId()
                                        )
                        )
                        .toList();

        Instant outreachSentAt =
                candidateOutreachRepository
                        .findFirstByEmailIgnoreCase(
                                recipient.getCandidate().getEmail()
                        )
                        .map(
                                outreach ->
                                        outreach.getOutreachSentAt()
                        )
                        .orElse(null);

        return CampaignRecipientAnalyticsResponse.from(
                recipient,
                applicationReference,
                outreachSentAt,
                emails
        );
    }
}