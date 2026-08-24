package com.adrovis.adrovis_backend.interview.service;

import com.adrovis.adrovis_backend.interview.dto.response.InterviewQuestionsResponse;

public interface InterviewQuestionService {

    InterviewQuestionsResponse getQuestions(
            String applicationId
    );
}