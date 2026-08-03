package com.adrovis.adrovis_backend.career.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ProgramApplicationSubmitRequest(

        @NotNull(message = "acceptedTerms is required")
        @AssertTrue(message = "Consent is required to submit your application.")
        Boolean acceptedTerms

) {}