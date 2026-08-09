package com.adrovis.adrovis_backend.interview.dto.response;

import java.time.OffsetDateTime;

public record InterviewSummaryResponse(
        String applicationId,
        String applicantName,
        String jobTitle,
        String status,
        int slotCount,
        OffsetDateTime scheduledStart
) {}
