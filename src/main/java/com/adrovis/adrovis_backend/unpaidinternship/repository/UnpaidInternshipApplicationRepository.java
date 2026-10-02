package com.adrovis.adrovis_backend.unpaidinternship.repository;

import com.adrovis.adrovis_backend.unpaidinternship.entity.UnpaidInternshipApplication;
import com.adrovis.adrovis_backend.unpaidinternship.enums.UnpaidInternshipApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnpaidInternshipApplicationRepository
        extends JpaRepository<UnpaidInternshipApplication, UUID> {

    Optional<UnpaidInternshipApplication>
    findByApplicationId(String applicationId);

    boolean existsByEmailIgnoreCase(String email);

    Page<UnpaidInternshipApplication>
    findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<UnpaidInternshipApplication>
    findAllByStatusOrderByCreatedAtDesc(
            UnpaidInternshipApplicationStatus status,
            Pageable pageable
    );

    Optional<UnpaidInternshipApplication> findTopByOrderByApplicationIdDesc();
}