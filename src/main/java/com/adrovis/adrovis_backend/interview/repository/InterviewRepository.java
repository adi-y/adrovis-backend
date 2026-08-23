package com.adrovis.adrovis_backend.interview.repository;


import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewStatus;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionGenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Collection;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    Optional<Interview> findByApplicationId(UUID applicationId);

    List<Interview> findByStatus(InterviewStatus status);

    List<Interview> findByScheduledStartUtcBetween(OffsetDateTime from, OffsetDateTime to);

    @Modifying
    @Query("""
    UPDATE Interview i
       SET i.questionGenerationStatus = :processing,
           i.questionGenerationStartedAt = :startedAt,
           i.questionGenerationError = null,
           i.questionGenerationAttempts = i.questionGenerationAttempts + 1
     WHERE i.id = :interviewId
       AND i.questionGenerationStatus IN :claimableStatuses
""")
    int claimQuestionGeneration(
            @Param("interviewId") UUID interviewId,
            @Param("processing") InterviewQuestionGenerationStatus processing,
            @Param("startedAt") OffsetDateTime startedAt,
            @Param("claimableStatuses")
            Collection<InterviewQuestionGenerationStatus> claimableStatuses
    );
}