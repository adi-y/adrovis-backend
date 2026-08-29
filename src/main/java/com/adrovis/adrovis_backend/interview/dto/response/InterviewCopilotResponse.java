package com.adrovis.adrovis_backend.interview.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record InterviewCopilotResponse(

        String command,

        Integer questionNumber,

        String question,

        String answer,

        List<Source> sources,

        UUID savedNoteId
) {

    public record Source(
            String title,
            String url
    ) {
    }

    public record SavedNote(
            UUID id,
            Integer questionNumber,
            String question,
            String userPrompt,
            String answer,
            String type,
            List<Source> sources,
            String savedAt
    ) {
    }
}