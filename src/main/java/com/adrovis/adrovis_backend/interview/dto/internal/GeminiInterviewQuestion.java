package com.adrovis.adrovis_backend.interview.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiInterviewQuestion(

        @JsonProperty("questionNumber")
        Integer questionNumber,

        @JsonProperty("category")
        String category,

        @JsonProperty("difficulty")
        String difficulty,

        @JsonProperty("answerSource")
        String answerSource,

        @JsonProperty("question")
        String question,

        @JsonProperty("expectedAnswer")
        String expectedAnswer,

        @JsonProperty("keyPoints")
        List<String> keyPoints,

        @JsonProperty("followUpQuestions")
        List<String> followUpQuestions,

        @JsonProperty("resumeBasis")
        String resumeBasis,

        @JsonProperty("interviewerGoal")
        String interviewerGoal
) {
}