package com.adrovis.adrovis_backend.interview.dto.request;

import com.adrovis.adrovis_backend.interview.dto.response.InterviewCopilotResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterviewCopilotSaveRequest(

        @Min(1)
        @Max(15)
        Integer questionNumber,

        @Size(max = 40)
        String command,

        @Size(max = 2000)
        String userPrompt,

        @NotBlank
        @Size(max = 20000)
        String answer,

        List<InterviewCopilotResponse.Source> sources
) {
}