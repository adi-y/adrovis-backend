package com.adrovis.adrovis_backend.career.dto.request;

import com.adrovis.adrovis_backend.career.enums.EmploymentType;
import com.adrovis.adrovis_backend.career.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(

        @NotBlank(message = "Title is required.")
        @Size(max = 255, message = "Title must not exceed 255 characters.")
        String title,

        @NotBlank(message = "Description is required.")
        String description,

        @NotBlank(message = "Location is required.")
        @Size(max = 255, message = "Location must not exceed 255 characters.")
        String location,

        @NotNull(message = "Employment type is required.")
        EmploymentType employmentType,

        @NotNull(message = "Job status is required.")
        JobStatus status
) {
}