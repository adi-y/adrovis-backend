package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.campaign.enums.CampaignEligibilityStatus;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;
import com.adrovis.adrovis_backend.career.entity.Application;

public interface CampaignEligibilityService {

    EligibilityResult evaluate(
            Candidate candidate,
            Application application
    );

    record EligibilityResult(
            CampaignEligibilityStatus status,
            Application application
    ) {
    }
}