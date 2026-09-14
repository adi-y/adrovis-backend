package com.adrovis.adrovis_backend.campaign.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.candidate.entity.Candidate;

import java.time.Instant;

public interface CampaignEnrollmentService {

    void enrollOutreach(
            Candidate candidate,
            Instant outreachSentAt
    );

    void startApplicationJourney(
            Candidate candidate,
            Application application
    );
}