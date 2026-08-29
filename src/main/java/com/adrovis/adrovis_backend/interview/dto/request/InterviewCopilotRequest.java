package com.adrovis.adrovis_backend.interview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record InterviewCopilotRequest(

        @Min(1)
        @Max(15)
        Integer questionNumber,

        @Size(max = 40)
        String command,

        @Size(max = 2000)
        String message,

        Boolean save
) {
}