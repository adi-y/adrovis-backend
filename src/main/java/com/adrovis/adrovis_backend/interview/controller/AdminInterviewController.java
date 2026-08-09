package com.adrovis.adrovis_backend.interview.controller;

import com.adrovis.adrovis_backend.interview.dto.request.*;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewSummaryResponse;
import com.adrovis.adrovis_backend.interview.service.InterviewSchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview Scheduling (Admin)", description = "ADMIN/RECRUITER-only endpoints for reviewing availability and managing interviews. Not authenticated yet — secure /api/v1/admin/** when auth is added.")
public class AdminInterviewController {

    private final InterviewSchedulingService interviewSchedulingService;

    @GetMapping
    @Operation(summary = "List/filter interviews for the dashboard",
            description = "ADMIN — filter by status, pendingAvailability, today, upcoming, or a from/to date range.")
    public ResponseEntity<List<InterviewSummaryResponse>> listInterviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean pendingAvailability,
            @RequestParam(required = false) Boolean today,
            @RequestParam(required = false) Boolean upcoming,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(interviewSchedulingService.listInterviews(
                status, pendingAvailability, today, upcoming, from, to));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get full interview detail", description = "ADMIN — includes admin note and all submitted slots.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview detail returned"),
            @ApiResponse(responseCode = "404", description = "No interview record found")
    })
    public ResponseEntity<InterviewResponse> getInterview(
            @Parameter(example = "APP202600038") @PathVariable String applicationId) {
        return ResponseEntity.ok(interviewSchedulingService.getInterview(applicationId));
    }

    @PostMapping("/{applicationId}/schedule")
    @Operation(summary = "Confirm an interview time",
            description = "ADMIN — pick one of the candidate's submitted slots or a custom time. Fails with 409 if already scheduled (use reschedule) or not yet awaiting/submitted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview scheduled, candidate notified by email"),
            @ApiResponse(responseCode = "400", description = "Invalid time range"),
            @ApiResponse(responseCode = "404", description = "No interview record found"),
            @ApiResponse(responseCode = "409", description = "Interview not schedulable in its current status")
    })
    public ResponseEntity<InterviewResponse> schedule(
            @Parameter(example = "APP202600038") @PathVariable String applicationId,
            @Valid @RequestBody ScheduleInterviewRequest request) {
        return ResponseEntity.ok(interviewSchedulingService.scheduleInterview(applicationId, request));
    }

    @PatchMapping("/{applicationId}/reschedule")
    @Operation(summary = "Change the confirmed time of a scheduled interview",
            description = "ADMIN — only valid while status is SCHEDULED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview rescheduled, candidate notified by email"),
            @ApiResponse(responseCode = "409", description = "Interview is not currently SCHEDULED")
    })
    public ResponseEntity<InterviewResponse> reschedule(
            @Parameter(example = "APP202600038") @PathVariable String applicationId,
            @Valid @RequestBody RescheduleInterviewRequest request) {
        return ResponseEntity.ok(interviewSchedulingService.rescheduleInterview(applicationId, request));
    }

    @PatchMapping("/{applicationId}/cancel")
    @Operation(summary = "Cancel an interview", description = "ADMIN — valid from any non-terminal status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview cancelled, candidate notified by email"),
            @ApiResponse(responseCode = "409", description = "Interview already cancelled or completed")
    })
    public ResponseEntity<InterviewResponse> cancel(
            @Parameter(example = "APP202600038") @PathVariable String applicationId,
            @RequestBody(required = false) CancelInterviewRequest request) {
        return ResponseEntity.ok(interviewSchedulingService.cancelInterview(
                applicationId, request != null ? request : new CancelInterviewRequest(null)));
    }

    @PatchMapping("/{applicationId}/outcome")
    @Operation(summary = "Record COMPLETED or NO_SHOW after the interview date", description = "ADMIN — internal record-keeping, no email sent.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Outcome recorded"),
            @ApiResponse(responseCode = "409", description = "Interview is not currently SCHEDULED")
    })
    public ResponseEntity<InterviewResponse> recordOutcome(
            @Parameter(example = "APP202600038") @PathVariable String applicationId,
            @Valid @RequestBody InterviewOutcomeRequest request) {
        return ResponseEntity.ok(interviewSchedulingService.recordOutcome(applicationId, request));
    }

    @PostMapping("/{applicationId}/request-availability")
    @Operation(summary = "Reset to AWAITING_AVAILABILITY and resend the request email",
            description = "ADMIN — use when none of the candidate's slots work and you want fresh input instead of picking a time yourself.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability re-requested, email resent"),
            @ApiResponse(responseCode = "409", description = "Interview is cancelled or completed")
    })
    public ResponseEntity<InterviewResponse> requestAvailabilityAgain(
            @Parameter(example = "APP202600038") @PathVariable String applicationId) {
        return ResponseEntity.ok(interviewSchedulingService.requestAvailabilityAgain(applicationId));
    }
}
