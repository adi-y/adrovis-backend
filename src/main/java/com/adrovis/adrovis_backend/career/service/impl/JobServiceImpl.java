package com.adrovis.adrovis_backend.career.service.impl;

import com.adrovis.adrovis_backend.career.dto.request.CreateJobRequest;
import com.adrovis.adrovis_backend.career.dto.request.UpdateJobRequest;
import com.adrovis.adrovis_backend.career.dto.response.JobResponse;
import com.adrovis.adrovis_backend.career.entity.Job;
import com.adrovis.adrovis_backend.career.enums.JobStatus;
import com.adrovis.adrovis_backend.career.mapper.JobMapper;
import com.adrovis.adrovis_backend.career.repository.JobRepository;
import com.adrovis.adrovis_backend.career.service.JobService;
import com.adrovis.adrovis_backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponse createJob(CreateJobRequest request) {

        Job job = jobMapper.toEntity(request);

        Job savedJob = jobRepository.save(job);

        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID jobId, UpdateJobRequest request) {

        Job job = findJobById(jobId);

        jobMapper.updateEntity(request, job);

        return jobMapper.toResponse(job);
    }

    @Override
    public JobResponse getJobById(UUID jobId) {
        return jobMapper.toResponse(findJobById(jobId));
    }

    @Override
    public List<JobResponse> getAllJobs() {

        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    public List<JobResponse> getJobsByStatus(JobStatus status) {

        return jobRepository.findAllByStatus(status)
                .stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId) {

        Job job = findJobById(jobId);

        jobRepository.delete(job);
    }

    private Job findJobById(UUID jobId) {

        return jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job not found with id: " + jobId
                        )
                );
    }
}