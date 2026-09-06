package com.adrovis.adrovis_backend.career.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CandidateOutreachRequest(

        @NotBlank
        String source,

        UUID jobId,

        @NotEmpty
        List<@Valid Candidate> candidates

) {

    public record Candidate(

            @NotBlank
            String name,

            @NotBlank
            @Email
            String email

    ) {
    }
}