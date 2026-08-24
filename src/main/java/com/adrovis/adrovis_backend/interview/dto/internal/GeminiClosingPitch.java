package com.adrovis.adrovis_backend.interview.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiClosingPitch(

        @JsonProperty("candidateStrengths")
        List<String> candidateStrengths,

        @JsonProperty("candidateGaps")
        List<String> candidateGaps,

        @JsonProperty("bestValueAngle")
        String bestValueAngle,

        @JsonProperty("transition")
        String transition,

        @JsonProperty("candidateSpecificPitch")
        String candidateSpecificPitch,

        @JsonProperty("programValuePoints")
        List<String> programValuePoints,

        @JsonProperty("feeExplanation")
        String feeExplanation,

        @JsonProperty("commitmentMessage")
        String commitmentMessage,

        @JsonProperty("ppoMessage")
        String ppoMessage,

        @JsonProperty("closingQuestion")
        String closingQuestion
) {
}