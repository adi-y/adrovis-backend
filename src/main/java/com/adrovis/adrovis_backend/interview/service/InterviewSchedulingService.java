package com.adrovis.adrovis_backend.interview.service;

import com.adrovis.adrovis_backend.interview.dto.request.*;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface InterviewSchedulingService {

    /**
     * Called from the existing application-status-update flow when an application
     * transitions to SHORTLISTED. Creates the interview row if it doesn't exist yet
     * and sends the availability-request email. Safe to call more than once.
     */
    void ensureInterviewAndRequestAvailability(String applicationId);

    InterviewResponse getInterview(String applicationId);

    InterviewResponse submitAvailability(String applicationId, AvailabilitySubmissionRequest request);

    List<InterviewSummaryResponse> listInterviews(String status, Boolean pendingAvailability,
                                                  Boolean today, Boolean upcoming,
                                                  LocalDate from, LocalDate to);

    InterviewResponse scheduleInterview(String applicationId, ScheduleInterviewRequest request);

    InterviewResponse rescheduleInterview(String applicationId, RescheduleInterviewRequest request);

    InterviewResponse cancelInterview(String applicationId, CancelInterviewRequest request);

    InterviewResponse recordOutcome(String applicationId, InterviewOutcomeRequest request);

    InterviewResponse requestAvailabilityAgain(String applicationId);
}