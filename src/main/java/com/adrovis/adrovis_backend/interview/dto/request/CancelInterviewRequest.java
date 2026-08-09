package com.adrovis.adrovis_backend.interview.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin cancels an interview")
public record CancelInterviewRequest(
        @Schema(example = "Position put on hold")
        String reason
) {}