package com.adrovis.adrovis_backend.candidate.service;

import com.adrovis.adrovis_backend.candidate.entity.Candidate;

public interface CandidateService {

    Candidate getOrCreate(
            String name,
            String email
    );

    Candidate findByEmail(String email);

    void optOutOfMarketing(String email);

    boolean isMarketingOptedOut(String email);
}