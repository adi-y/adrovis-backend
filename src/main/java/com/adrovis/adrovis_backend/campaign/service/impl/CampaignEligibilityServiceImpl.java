package com.adrovis.adrovis_backend.campaign.service.impl;

import com.adrovis.adrovis_backend.campaign.enums.CampaignEligibilityStatus;
import com.adrovis.adrovis_backend.campaign.service.CampaignEligibilityService;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import org.springframework.stereotype.Service;

@Service
public class CampaignEligibilityServiceImpl
        implements CampaignEligibilityService {

    @Override
    public EligibilityResult evaluate(
            Candidate candidate,
            Application application
    ) {
        if (candidate.isMarketingOptOut()) {
            return new EligibilityResult(
                    CampaignEligibilityStatus.UNSUBSCRIBED,
                    application
            );
        }

        if (application == null) {
            return new EligibilityResult(
                    CampaignEligibilityStatus.ELIGIBLE,
                    null
            );
        }

        ApplicationStatus status =
                application.getApplicationStatus();

        return switch (status) {
            case PENDING ->
                    new EligibilityResult(
                            CampaignEligibilityStatus.APPLICATION_PENDING,
                            application
                    );

            case SUBMITTED ->
                    new EligibilityResult(
                            CampaignEligibilityStatus.APPLICATION_SUBMITTED,
                            application
                    );

            case UNDER_REVIEW ->
                    new EligibilityResult(
                            CampaignEligibilityStatus.APPLICATION_UNDER_REVIEW,
                            application
                    );

            case SHORTLISTED ->
                    new EligibilityResult(
                            CampaignEligibilityStatus.APPLICATION_SHORTLISTED,
                            application
                    );

            case HIRED ->
                    new EligibilityResult(
                            CampaignEligibilityStatus.APPLICATION_HIRED,
                            application
                    );

            case REJECTED ->
                    new EligibilityResult(
                            CampaignEligibilityStatus.APPLICATION_REJECTED,
                            application
                    );
        };
    }
}