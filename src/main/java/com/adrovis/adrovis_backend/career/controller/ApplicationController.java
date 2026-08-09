package com.adrovis.adrovis_backend.career.controller;

import com.adrovis.adrovis_backend.career.dto.request.CreateApplicationRequest;
import com.adrovis.adrovis_backend.career.dto.request.UpdateApplicationStatusRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationResponse;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.service.ApplicationService;
import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(

            @Valid
            @RequestPart("application")
            CreateApplicationRequest request,

            @RequestPart("resume")
            MultipartFile resume
    ) {

        ApplicationResponse response =
                applicationService.createApplication(request, resume);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED,
                                "Application submitted successfully.",
                                response
                        )
                );
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable UUID applicationId
    ) {

        ApplicationResponse response =
                applicationService.getApplicationById(applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Application retrieved successfully.",
                        response
                )
        );
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsByJob(
            @PathVariable UUID jobId
    ) {

        List<ApplicationResponse> responses =
                applicationService.getApplicationsByJob(jobId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Applications retrieved successfully.",
                        responses
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicationsByStatus(
            @PathVariable ApplicationStatus status
    ) {

        List<ApplicationResponse> responses =
                applicationService.getApplicationsByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Applications retrieved successfully.",
                        responses
                )
        );
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(

            @PathVariable String applicationId,

            @Valid
            @RequestBody
            UpdateApplicationStatusRequest request
    ) {

        ApplicationResponse response =
                applicationService.updateApplicationStatus(
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Application status updated successfully.",
                        response
                )
        );
    }
}