package com.adrovis.adrovis_backend.interview.dto.response;

import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionGenerationStatus;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class InterviewQuestionsResponse {

    private String applicationId;

    private UUID interviewId;

    private InterviewQuestionGenerationStatus generationStatus;

    private Integer totalQuestions;

    private List<InterviewQuestionResponse> questions;

    private String closingPitch;

    private List<InterviewCopilotResponse.SavedNote> copilotNotes;

    private String generationError;
}