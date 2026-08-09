package com.adrovis.adrovis_backend.career.dto.response;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(

        UUID id,
        String applicationId,

        UUID jobId,

        ApplicationType applicationType,

        ApplicationStatus applicationStatus,

        String applicantName,

        String applicantEmail,

        String applicantPhone,

        String resumeUrl,

        String note,

        Instant submittedAt,

        Instant createdAt,

        Instant updatedAt
) {
}
