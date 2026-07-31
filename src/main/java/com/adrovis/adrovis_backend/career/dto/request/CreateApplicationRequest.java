package com.adrovis.adrovis_backend.career.dto.request;

import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.career.validation.ValidApplicationRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@ValidApplicationRequest
public record CreateApplicationRequest(

        UUID jobId,

        @NotNull(message = "Application type is required.")
        ApplicationType applicationType,

        @NotBlank(message = "Applicant name is required.")
        @Size(max = 255)
        String applicantName,

        @NotBlank(message = "Applicant email is required.")
        @Email(message = "Invalid email format.")
        @Size(max = 255)
        String applicantEmail,

        @Size(max = 20)
        String applicantPhone,

        String coverLetter
) {
}