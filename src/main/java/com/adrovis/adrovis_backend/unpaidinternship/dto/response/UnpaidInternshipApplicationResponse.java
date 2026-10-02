package com.adrovis.adrovis_backend.unpaidinternship.dto.response;

import com.adrovis.adrovis_backend.unpaidinternship.enums.UnpaidInternshipApplicationStatus;

import java.time.Instant;

public record UnpaidInternshipApplicationResponse(

        String applicationId,

        String fullName,

        String email,

        String phone,

        String college,

        Integer graduationYear,

        String batch,

        String internshipTitle,

        UnpaidInternshipApplicationStatus status,

        String resumeOriginalName,

        String resumeMimeType,

        Long resumeSizeBytes,

        Instant submittedAt,

        Instant createdAt,

        Instant updatedAt

) {
}