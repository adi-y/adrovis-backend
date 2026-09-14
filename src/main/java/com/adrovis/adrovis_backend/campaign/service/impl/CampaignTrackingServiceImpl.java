package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.repository.CampaignEmailRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignTrackingServiceImpl
        implements CampaignTrackingService {

    private final CampaignEmailRepository emailRepository;

    @Override
    @Transactional
    public void markOpened(UUID campaignEmailId) {
        emailRepository
                .findById(campaignEmailId)
                .ifPresent(email -> {
                    email.markOpened();
                    emailRepository.save(email);
                });
    }

    @Override
    @Transactional
    public void markClicked(UUID campaignEmailId) {
        emailRepository
                .findById(campaignEmailId)
                .ifPresent(email -> {
                    email.markClicked();
                    emailRepository.save(email);
                });
    }
}