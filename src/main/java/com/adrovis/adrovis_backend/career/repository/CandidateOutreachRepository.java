package com.adrovis.adrovis_backend.career.repository;

import com.adrovis.adrovis_backend.career.entity.CandidateOutreach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidateOutreachRepository
        extends JpaRepository<CandidateOutreach, UUID> {

    boolean existsByEmailIgnoreCaseAndSource(
            String email,
            String source
    );

    Optional<CandidateOutreach> findFirstByEmailIgnoreCase(
            String email
    );
}