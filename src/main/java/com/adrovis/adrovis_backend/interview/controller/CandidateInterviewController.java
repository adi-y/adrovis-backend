package com.adrovis.adrovis_backend.interview.controller;

import com.adrovis.adrovis_backend.interview.dto.request.AvailabilitySubmissionRequest;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewResponse;
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

@RestController
@RequestMapping("/api/v1/applications/{applicationId}")
@RequiredArgsConstructor
@Tag(name = "Interview Scheduling (Candidate)", description = "Public endpoints candidates use to view and submit interview availability")
public class CandidateInterviewController {

    private final InterviewSchedulingService interviewSchedulingService;

    @GetMapping("/interview")
    @Operation(summary = "Get current interview status",
            description = "PUBLIC — candidates use this to check whether availability is requested, submitted, or an interview is already scheduled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interview status returned"),
            @ApiResponse(responseCode = "404", description = "No interview record exists for this application")
    })
    public ResponseEntity<InterviewResponse> getInterview(
            @Parameter(example = "APP202600038") @PathVariable String applicationId) {
        return ResponseEntity.ok(interviewSchedulingService.getInterview(applicationId));
    }

    @PostMapping("/availability")
    @Operation(summary = "Submit (or replace) availability",
            description = "PUBLIC — candidate submits 1-10 time windows. Resubmitting replaces the previous submission. Rejected once an interview is scheduled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability saved"),
            @ApiResponse(responseCode = "400", description = "Invalid slot (bad time range, past date, duplicate)"),
            @ApiResponse(responseCode = "404", description = "No interview record exists yet"),
            @ApiResponse(responseCode = "409", description = "Interview already scheduled/cancelled/completed")
    })
    public ResponseEntity<InterviewResponse> submitAvailability(
            @Parameter(example = "APP202600038") @PathVariable String applicationId,
            @Valid @RequestBody AvailabilitySubmissionRequest request) {
        return ResponseEntity.ok(interviewSchedulingService.submitAvailability(applicationId, request));
    }
}
