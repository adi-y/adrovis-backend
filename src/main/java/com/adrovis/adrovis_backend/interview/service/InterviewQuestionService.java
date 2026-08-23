package com.adrovis.adrovis_backend.interview.service;

import com.adrovis.adrovis_backend.interview.dto.response.InterviewQuestionsResponse;

import java.util.UUID;

public interface InterviewQuestionService {

    InterviewQuestionsResponse getQuestions(UUID interviewId);
}