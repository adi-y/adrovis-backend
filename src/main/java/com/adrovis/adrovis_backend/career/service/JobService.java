package com.adrovis.adrovis_backend.career.service;

import com.adrovis.adrovis_backend.career.dto.request.CreateJobRequest;
import com.adrovis.adrovis_backend.career.dto.request.UpdateJobRequest;
import com.adrovis.adrovis_backend.career.dto.response.JobResponse;
import com.adrovis.adrovis_backend.career.enums.JobStatus;

import java.util.List;
import java.util.UUID;

public interface JobService {

    JobResponse createJob(CreateJobRequest request);

    JobResponse updateJob(UUID jobId, UpdateJobRequest request);

    JobResponse getJobById(UUID jobId);

    List<JobResponse> getAllJobs();

    List<JobResponse> getJobsByStatus(JobStatus status);

    void deleteJob(UUID jobId);
}