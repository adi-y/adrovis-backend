package com.adrovis.adrovis_backend.interview.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "Admin confirms an interview time — either one of the candidate's slots or a new one")
public record ScheduleInterviewRequest(

        @NotNull
        @Schema(example = "2026-08-12T14:00:00+05:30")
        OffsetDateTime scheduledStart,

        @NotNull
        @Schema(example = "2026-08-12T15:00:00+05:30")
        OffsetDateTime scheduledEnd,

        @NotBlank
        @Schema(example = "Rohan Mehta")
        String interviewerName,

        @Schema(example = "https://meet.google.com/abc-defg-hij")
        String meetingLink,

        @Schema(example = "Picked from candidate's second slot")
        String adminNote
) {}
