package com.adrovis.adrovis_backend.career.dto.response;

import com.adrovis.adrovis_backend.career.enums.EmploymentType;
import com.adrovis.adrovis_backend.career.enums.JobStatus;

import java.time.Instant;
import java.util.UUID;

public record JobResponse(

        UUID id,

        String title,

        String description,

        String location,

        EmploymentType employmentType,

        JobStatus status,

        Instant createdAt,

        Instant updatedAt
) {
}