package com.adrovis.adrovis_backend.career.dto.response;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(

        UUID id,

        UUID jobId,

        ApplicationType applicationType,

        ApplicationStatus applicationStatus,

        String applicantName,

        String applicantEmail,

        String applicantPhone,

        String resumeUrl,

        String coverLetter,

        Instant submittedAt,

        Instant createdAt,

        Instant updatedAt
) {
}