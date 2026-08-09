package com.adrovis.adrovis_backend.interview.repository;


import com.adrovis.adrovis_backend.interview.entity.InterviewAvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface InterviewAvailabilitySlotRepository extends JpaRepository<InterviewAvailabilitySlot, UUID> {

    List<InterviewAvailabilitySlot> findByInterviewIdOrderByAvailableDateAscStartTimeAsc(UUID interviewId);

    @Transactional
    void deleteByInterviewId(UUID interviewId);
}
