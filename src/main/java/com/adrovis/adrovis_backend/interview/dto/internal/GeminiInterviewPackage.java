package com.adrovis.adrovis_backend.interview.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiInterviewPackage(

        @JsonProperty("questions")
        List<GeminiInterviewQuestion> questions,

        @JsonProperty("closingPitch")
        GeminiClosingPitch closingPitch
) {
}