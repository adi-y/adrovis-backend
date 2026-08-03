package com.adrovis.adrovis_backend.career.service.impl;

import com.adrovis.adrovis_backend.career.dto.request.CreateApplicationRequest;
import com.adrovis.adrovis_backend.career.dto.request.UpdateApplicationStatusRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationResponse;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.entity.Job;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.career.mapper.ApplicationMapper;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.career.repository.JobRepository;
import com.adrovis.adrovis_backend.career.service.ApplicationService;
import com.adrovis.adrovis_backend.common.entity.ApplicationIdGenerator;
import com.adrovis.adrovis_backend.common.exception.DuplicateResourceException;
import com.adrovis.adrovis_backend.common.exception.ResourceNotFoundException;
import com.adrovis.adrovis_backend.storage.dto.response.FileUploadResponse;
import com.adrovis.adrovis_backend.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ApplicationMapper applicationMapper;
    private final FileStorageService fileStorageService;
    private final ApplicationIdGenerator idGenerator;

    @Override
    @Transactional
    public ApplicationResponse createApplication(
            CreateApplicationRequest request,
            MultipartFile resume
    ) {

        Job job = null;

        if (request.applicationType() == ApplicationType.JOB) {
            job = findJob(request.jobId());
            validateDuplicateApplication(request.applicantEmail(), job);
        }

        FileUploadResponse uploadedResume =
                fileStorageService.upload(resume);

        Application application = new Application(
                idGenerator.next(),                       // applicationId
                job,
                ApplicationType.JOB,
                ApplicationStatus.SUBMITTED,
                job.getTitle(),                           // snapshot

                request.applicantName(),
                request.applicantEmail(),
                request.applicantPhone(),

                null,                                    // college
                null,                                    // graduationYear

                uploadedResume.storageKey(),
                uploadedResume.fileUrl(),
                uploadedResume.originalName(),
                uploadedResume.mimeType(),
                uploadedResume.sizeBytes(),

                request.coverLetter(),                   // note

                true,                                    // consent
                Instant.now()
        );

        Application savedApplication =
                applicationRepository.save(application);

        return applicationMapper.toResponse(savedApplication);
    }

    @Override
    public ApplicationResponse getApplicationById(UUID applicationId) {
        return applicationMapper.toResponse(findApplication(applicationId));
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJob(UUID jobId) {

        Job job = findJob(jobId);

        return applicationRepository.findAllByJob(job)
                .stream()
                .map(applicationMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsByStatus(
            ApplicationStatus status
    ) {

        return applicationRepository.findAllByApplicationStatus(status)
                .stream()
                .map(applicationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(
            UUID applicationId,
            UpdateApplicationStatusRequest request
    ) {

        Application application = findApplication(applicationId);

        application.changeStatus(request.applicationStatus());

        return applicationMapper.toResponse(application);
    }

    private Job findJob(UUID jobId) {

        return jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job not found with id: " + jobId
                        )
                );
    }

    private Application findApplication(UUID applicationId) {

        return applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Application not found with id: " + applicationId
                        )
                );
    }

    private void validateDuplicateApplication(
            String applicantEmail,
            Job job
    ) {

        if (applicationRepository.existsByApplicantEmailIgnoreCaseAndJob(
                applicantEmail,
                job
        )) {

            throw new DuplicateResourceException(
                    "Application already exists.",
                    Map.of(
                            "applicantEmail",
                            "An application has already been submitted for this job."
                    )
            );
        }
    }
}