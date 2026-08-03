package com.adrovis.adrovis_backend.career.dto.response;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;

import lombok.Builder;


public record ApplicationCreatedResponse(
        String applicationId,
        ApplicationStatus applicationStatus
) {}
