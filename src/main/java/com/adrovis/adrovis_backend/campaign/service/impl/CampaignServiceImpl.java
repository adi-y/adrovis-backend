package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.config.CampaignProperties;
import com.adrovis.adrovis_backend.campaign.dto.request.CreateCampaignRequest;
import com.adrovis.adrovis_backend.campaign.entity.Campaign;
import com.adrovis.adrovis_backend.campaign.enums.CampaignStatus;
import com.adrovis.adrovis_backend.campaign.repository.CampaignRepository;
import com.adrovis.adrovis_backend.campaign.service.CampaignService;
import com.adrovis.adrovis_backend.common.exception.AppException;
import com.adrovis.adrovis_backend.program.entity.ProgramConfiguration;
import com.adrovis.adrovis_backend.program.repository.ProgramConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl
        implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final ProgramConfigurationRepository programConfigurationRepository;
    private final CampaignProperties campaignProperties;

    @Override
    @Transactional
    public Campaign create(
            CreateCampaignRequest request
    ) {

        String programKey =
                campaignProperties.getProgramKey();

        if (programKey == null
                || programKey.isBlank()) {

            throw new IllegalStateException(
                    "Campaign program key is not configured."
            );
        }

        ProgramConfiguration program =
                programConfigurationRepository
                        .findByProgramKeyAndActiveTrue(
                                programKey
                        )
                        .orElseThrow(() ->
                                AppException.notFound(
                                        "Active internship program configuration not found."
                                )
                        );

        Campaign campaign =
                new Campaign(
                        request.name(),
                        request.type(),
                        program.getProgramName(),
                        program.getFeeAmount(),
                        program.getCurrency(),
                        program.getDuration(),
                        program.getMode()
                );

        campaign.schedule(
                request.scheduledAt()
        );

        return campaignRepository.save(
                campaign
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findAll() {
        return campaignRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign get(
            UUID campaignId
    ) {

        return campaignRepository
                .findById(campaignId)
                .orElseThrow(() ->
                        AppException.notFound(
                                "Campaign not found."
                        )
                );
    }

    @Override
    @Transactional
    public void close(
            UUID campaignId
    ) {

        Campaign campaign =
                get(campaignId);

        if (campaign.getStatus()
                == CampaignStatus.CLOSED) {
            return;
        }

        campaign.close();
    }
}