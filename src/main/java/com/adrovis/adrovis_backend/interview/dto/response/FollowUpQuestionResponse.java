package com.adrovis.adrovis_backend.interview.dto.response;

public record FollowUpQuestionResponse(
        String question,
        String expectedAnswer
) {
}