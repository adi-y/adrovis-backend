package com.adrovis.adrovis_backend.career.service;

import com.adrovis.adrovis_backend.career.dto.request.CandidateOutreachRequest;
import com.adrovis.adrovis_backend.career.entity.CandidateOutreach;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.career.repository.CandidateOutreachRepository;
import com.adrovis.adrovis_backend.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidateOutreachService {

    private final ApplicationRepository applicationRepository;
    private final CandidateOutreachRepository candidateOutreachRepository;
    private final EmailService emailService;

    public Map<String, Integer> send(
            CandidateOutreachRequest request
    ) {

        String source = request.source()
                .trim()
                .toUpperCase();

        int received = request.candidates().size();
        int queued = 0;
        int alreadyApplied = 0;
        int alreadyContacted = 0;

        for (CandidateOutreachRequest.Candidate candidate
                : request.candidates()) {

            String email = candidate.email()
                    .trim()
                    .toLowerCase();

            if (applicationRepository
                    .existsByApplicantEmailIgnoreCase(email)) {

                alreadyApplied++;
                continue;
            }

            if (candidateOutreachRepository
                    .existsByEmailIgnoreCaseAndSource(email, source)) {

                alreadyContacted++;
                continue;
            }

            CandidateOutreach outreach =
                    new CandidateOutreach(
                            candidate.name(),
                            email,
                            source,
                            request.jobId()
                    );

            CandidateOutreach saved =
                    candidateOutreachRepository.save(outreach);

            emailService.sendCandidateOutreachEmailAsync(saved);

            queued++;
        }

        Map<String, Integer> result =
                new LinkedHashMap<>();

        result.put("received", received);
        result.put("queued", queued);
        result.put("alreadyApplied", alreadyApplied);
        result.put("alreadyContacted", alreadyContacted);

        return result;
    }
}