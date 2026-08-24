package com.adrovis.adrovis_backend.interview.dto.ai;

import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionAnswerSource;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionCategory;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionDifficulty;

import java.util.List;

public record AiInterviewQuestion(
        Integer questionNumber,
        InterviewQuestionCategory category,
        InterviewQuestionDifficulty difficulty,
        InterviewQuestionAnswerSource answerSource,
        String question,
        String expectedAnswer,
        List<String> keyPoints,
        List<AiFollowUpQuestion> followUpQuestions,
        String resumeBasis,
        String interviewerGoal
) {
}