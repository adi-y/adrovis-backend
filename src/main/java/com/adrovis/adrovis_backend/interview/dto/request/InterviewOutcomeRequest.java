package com.adrovis.adrovis_backend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Admin records what happened after the scheduled interview")
public record InterviewOutcomeRequest(
        @Pattern(regexp = "COMPLETED|NO_SHOW", message = "outcome must be COMPLETED or NO_SHOW")
        @Schema(example = "COMPLETED", allowableValues = {"COMPLETED", "NO_SHOW"})
        String outcome
) {}