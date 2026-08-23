package com.adrovis.adrovis_backend.interview.service;

import java.util.UUID;

public interface InterviewQuestionGenerationService {

    void generateIfNeeded(UUID interviewId);
}