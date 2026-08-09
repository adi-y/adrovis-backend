package com.adrovis.adrovis_backend.interview.repository;


import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    Optional<Interview> findByApplicationId(UUID applicationId);

    List<Interview> findByStatus(InterviewStatus status);

    List<Interview> findByScheduledStartUtcBetween(OffsetDateTime from, OffsetDateTime to);
}