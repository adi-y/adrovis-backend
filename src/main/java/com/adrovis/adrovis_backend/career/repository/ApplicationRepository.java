package com.adrovis.adrovis_backend.career.repository;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.entity.Job;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findAllByJob(Job job);

    List<Application> findAllByApplicationStatus(ApplicationStatus status);

    Optional<Application> findByApplicationId(String applicationId);

    Optional<Application> findTopByOrderByApplicationIdDesc();

    boolean existsByApplicantEmailIgnoreCaseAndJob(
            String applicantEmail,
            Job job
    );

}