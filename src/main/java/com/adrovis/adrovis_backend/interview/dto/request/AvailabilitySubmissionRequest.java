package com.adrovis.adrovis_backend.interview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Candidate availability submission — replaces any prior submission")
public record AvailabilitySubmissionRequest(

        @NotBlank
        @Schema(example = "Asia/Kolkata", description = "IANA timezone the slots below are expressed in")
        String timezone,

        @Schema(example = "I have a client call until 9am on the 13th", description = "Optional note to the recruiter")
        String candidateNote,

        @NotEmpty(message = "At least one availability slot is required")
        @Size(max = 10, message = "Please submit at most 10 time windows")
        @Valid
        List<AvailabilitySlotRequest> slots
) {}
