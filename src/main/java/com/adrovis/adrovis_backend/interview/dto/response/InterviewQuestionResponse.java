package com.adrovis.adrovis_backend.interview.dto.response;

import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionAnswerSource;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionCategory;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionDifficulty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class InterviewQuestionResponse {

    private UUID id;

    private Integer questionNumber;

    private InterviewQuestionCategory category;

    private InterviewQuestionDifficulty difficulty;

    private InterviewQuestionAnswerSource answerSource;

    private String question;

    private String expectedAnswer;

    private List<String> keyPoints;

    private List<String> followUpQuestions;

    private String resumeBasis;

    private String interviewerGoal;
}