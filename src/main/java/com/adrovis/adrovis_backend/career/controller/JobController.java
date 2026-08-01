package com.adrovis.adrovis_backend.career.controller;

import com.adrovis.adrovis_backend.career.dto.response.JobResponse;
import com.adrovis.adrovis_backend.career.enums.JobStatus;
import com.adrovis.adrovis_backend.career.service.JobService;
import com.adrovis.adrovis_backend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> getAllJobs() {

        List<JobResponse> jobs = jobService.getAllJobs();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Jobs retrieved successfully.",
                        jobs
                )
        );
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(
            @PathVariable UUID jobId
    ) {

        JobResponse response = jobService.getJobById(jobId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Job retrieved successfully.",
                        response
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobsByStatus(
            @PathVariable JobStatus status
    ) {

        List<JobResponse> jobs = jobService.getJobsByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        "Jobs retrieved successfully.",
                        jobs
                )
        );
    }
}