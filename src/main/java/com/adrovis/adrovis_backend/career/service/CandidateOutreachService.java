package com.adrovis.adrovis_backend.career.service;

import com.adrovis.adrovis_backend.campaign.service.CampaignEnrollmentService;
import com.adrovis.adrovis_backend.candidate.service.CandidateService;
import com.adrovis.adrovis_backend.career.dto.request.CandidateOutreachRequest;
import com.adrovis.adrovis_backend.career.entity.CandidateOutreach;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.career.repository.CandidateOutreachRepository;
import com.adrovis.adrovis_backend.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidateOutreachService {

    private final ApplicationRepository applicationRepository;
    private final CandidateOutreachRepository
            candidateOutreachRepository;
    private final CandidateService candidateService;
    private final EmailService emailService;
    private final CampaignEnrollmentService
            campaignEnrollmentService;

    @Transactional
    public Map<String, Integer> send(
            CandidateOutreachRequest request
    ) {

        String source =
                request.source()
                        .trim()
                        .toUpperCase();

        int received =
                request.candidates().size();

        int queued = 0;
        int alreadyApplied = 0;
        int alreadyContacted = 0;

        for (CandidateOutreachRequest.Candidate candidateRequest
                : request.candidates()) {

            String email =
                    candidateRequest.email()
                            .trim()
                            .toLowerCase();

            var candidate =
                    candidateService.getOrCreate(
                            candidateRequest.name(),
                            email
                    );

            /*
             * PROGRAM ONLY.
             *
             * A JOB application must NOT suppress
             * Software Developer Internship outreach.
             */
            if (applicationRepository
                    .existsByApplicantEmailIgnoreCaseAndApplicationType(
                            email,
                            ApplicationType.PROGRAM
                    )) {

                alreadyApplied++;
                continue;
            }

            if (candidateOutreachRepository
                    .existsByEmailIgnoreCaseAndSource(
                            email,
                            source
                    )) {

                alreadyContacted++;
                continue;
            }

            /*
             * jobId != null means this is a job outreach.
             * It must NOT enter the internship campaign.
             */
            CandidateOutreach outreach =
                    new CandidateOutreach(
                            candidateRequest.name(),
                            email,
                            source,
                            request.jobId()
                    );

            CandidateOutreach saved =
                    candidateOutreachRepository.save(
                            outreach
                    );

            /*
             * Initial outreach is sent immediately.
             *
             * The campaign journey is enrolled only after
             * that initial email is actually sent successfully.
             */
            emailService.sendCandidateOutreachEmailAsync(
                    saved
            );

            queued++;
        }

        Map<String, Integer> result =
                new LinkedHashMap<>();

        result.put(
                "received",
                received
        );

        result.put(
                "queued",
                queued
        );

        result.put(
                "alreadyApplied",
                alreadyApplied
        );

        result.put(
                "alreadyContacted",
                alreadyContacted
        );

        return result;
    }
}