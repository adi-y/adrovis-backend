package com.adrovis.adrovis_backend.interview.repository;

import com.adrovis.adrovis_backend.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewQuestionRepository
        extends JpaRepository<InterviewQuestion, UUID> {

    List<InterviewQuestion> findByInterviewIdOrderByQuestionNumberAsc(UUID interviewId);

    void deleteByInterviewId(UUID interviewId);
}