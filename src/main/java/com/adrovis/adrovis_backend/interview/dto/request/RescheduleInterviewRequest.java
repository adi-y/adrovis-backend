package com.adrovis.adrovis_backend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "Admin changes the confirmed time of an already-scheduled interview")
public record RescheduleInterviewRequest(

        @NotNull
        OffsetDateTime scheduledStart,

        @NotNull
        OffsetDateTime scheduledEnd,

        @Schema(description = "Optional — leave unset to keep the current interviewer")
        String interviewerName,

        @Schema(description = "Optional — leave unset to keep the current link")
        String meetingLink,

        String adminNote
) {}