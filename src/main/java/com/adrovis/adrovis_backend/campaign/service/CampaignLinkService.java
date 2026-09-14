package com.adrovis.adrovis_backend.campaign.service;

import java.util.UUID;

public interface CampaignLinkService {

    String generateContinuationToken(
            String applicationId,
            String email
    );

    boolean validateContinuationToken(
            String token,
            String applicationId,
            String email
    );

    String generateUnsubscribeToken(UUID candidateId);

    UUID validateUnsubscribeToken(String token);

    String generateTrackingToken(UUID campaignEmailId, String event);

    UUID validateTrackingToken(
            String token,
            String event
    );
}