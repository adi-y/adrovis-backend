package com.adrovis.adrovis_backend.career.dto.request;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(

        @NotNull(message = "Application status is required.")
        ApplicationStatus applicationStatus
) {
}