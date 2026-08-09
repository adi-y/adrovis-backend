package com.adrovis.adrovis_backend.interview.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record InterviewResponse(
        String applicationId,
        String status,
        String candidateNote,
        String adminNote,
        List<AvailabilitySlotResponse> slots,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        String displayTimezone,
        String meetingLink,
        String interviewerName
) {}