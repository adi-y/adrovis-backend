package com.adrovis.adrovis_backend.career.dto.response;

import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;

public record ProgramApplicationContinuationResponse(

        String applicationId,
        String applicantName,
        ApplicationStatus applicationStatus,
        boolean continuationAllowed
) {
}