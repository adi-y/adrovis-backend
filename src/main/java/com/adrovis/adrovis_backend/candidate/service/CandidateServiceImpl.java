package com.adrovis.adrovis_backend.candidate.service;

import com.adrovis.adrovis_backend.candidate.entity.Candidate;
import com.adrovis.adrovis_backend.candidate.repository.CandidateRepository;
import com.adrovis.adrovis_backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;

    @Override
    @Transactional
    public Candidate getOrCreate(
            String name,
            String email
    ) {
        String normalizedEmail = normalizeEmail(email);

        return candidateRepository
                .findByNormalizedEmail(normalizedEmail)
                .map(candidate -> {
                    candidate.updateIdentity(name, email);
                    return candidate;
                })
                .orElseGet(() ->
                        candidateRepository.save(
                                new Candidate(name, email)
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Candidate findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return candidateRepository
                .findByNormalizedEmail(normalizedEmail)
                .orElseThrow(() ->
                        AppException.notFound("Candidate not found.")
                );
    }

    @Override
    @Transactional
    public void optOutOfMarketing(String email) {
        Candidate candidate = findByEmail(email);
        candidate.optOutOfMarketing();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMarketingOptedOut(String email) {
        return findByEmail(email).isMarketingOptOut();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Candidate email cannot be blank."
            );
        }

        return email.trim().toLowerCase();
    }
}