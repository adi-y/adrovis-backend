package com.adrovis.adrovis_backend.interview.dto.response;

import java.util.List;

public record ClosingPitchResponse(
        List<String> candidateStrengths,
        List<String> candidateGaps,
        String bestValueAngle,
        String transition,
        String candidateSpecificPitch,
        List<String> programValuePoints,
        String feeExplanation,
        String commitmentMessage,
        String ppoMessage,
        String closingQuestion
) {
}