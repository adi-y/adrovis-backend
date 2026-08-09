package com.adrovis.adrovis_backend.interview.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "A single candidate-proposed interview time window")
public record AvailabilitySlotRequest(

        @NotNull
        @Schema(example = "2026-08-12", description = "Date in the candidate's local timezone")
        LocalDate availableDate,

        @NotNull
        @Schema(example = "09:00", description = "Start time (local, 24h)")
        LocalTime startTime,

        @NotNull
        @Schema(example = "10:00", description = "End time (local, 24h) — must be after startTime")
        LocalTime endTime
) {}