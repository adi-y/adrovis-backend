package com.adrovis.adrovis_backend.interview.dto.ai;

import java.util.List;

public record AiClosingPitch(
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