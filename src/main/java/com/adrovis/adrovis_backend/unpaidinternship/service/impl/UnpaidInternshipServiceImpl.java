package com.adrovis.adrovis_backend.unpaidinternship.service.impl;

import com.adrovis.adrovis_backend.common.entity.ApplicationIdGenerator;
import com.adrovis.adrovis_backend.email.service.EmailService;
import com.adrovis.adrovis_backend.unpaidinternship.dto.request.UnpaidInternshipApplicationRequest;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipApplicationResponse;
import com.adrovis.adrovis_backend.unpaidinternship.dto.response.UnpaidInternshipDetailsResponse;
import com.adrovis.adrovis_backend.unpaidinternship.entity.UnpaidInternshipApplication;
import com.adrovis.adrovis_backend.unpaidinternship.enums.UnpaidInternshipApplicationStatus;
import com.adrovis.adrovis_backend.unpaidinternship.mapper.UnpaidInternshipApplicationMapper;
import com.adrovis.adrovis_backend.unpaidinternship.repository.UnpaidInternshipApplicationRepository;
import com.adrovis.adrovis_backend.unpaidinternship.service.UnpaidInternshipService;
import com.adrovis.adrovis_backend.unpaidinternship.service.UnpaidInternshipStorageService;
import com.adrovis.adrovis_backend.unpaidinternship.util.UnpaidInternshipApplicationIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnpaidInternshipServiceImpl
        implements UnpaidInternshipService {

    private static final String INTERNSHIP_TITLE =
            "Unpaid Software Developer Internship";

    private static final String APPLICATION_SOURCE =
            "UNPAID_INTERNSHIP";

    private final UnpaidInternshipApplicationRepository repository;

    private final UnpaidInternshipStorageService storageService;

    private final UnpaidInternshipApplicationMapper mapper;

    private final UnpaidInternshipApplicationIdGenerator applicationIdGenerator;

    private final EmailService emailService;

    @Override
    public UnpaidInternshipDetailsResponse getInternshipDetails() {

        return new UnpaidInternshipDetailsResponse(
                INTERNSHIP_TITLE,
                "Software Developer Internship Program",
                "Current Batch",
                true
        );
    }

    @Override
    @Transactional
    public UnpaidInternshipApplicationResponse submitApplication(
            UnpaidInternshipApplicationRequest request
    ) {

        if (repository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalStateException(
                    "An unpaid internship application already exists for this email."
            );
        }

        MultipartFile resume =
                request.getResume();

        UnpaidInternshipStorageService.StoredResume uploaded =
                storageService.upload(resume);

        String applicationId =
                applicationIdGenerator.next();

        UnpaidInternshipApplication application =
                new UnpaidInternshipApplication(
                        applicationId,
                        request.getFullName(),
                        request.getEmail(),
                        request.getPhone(),
                        request.getCollege(),
                        request.getGraduationYear(),
                        request.getBatch(),
                        INTERNSHIP_TITLE,
                        APPLICATION_SOURCE,
                        uploaded.storagePath(),
                        uploaded.originalName(),
                        uploaded.mimeType(),
                        uploaded.sizeBytes()
                );

        UnpaidInternshipApplication saved =
                repository.save(application);

        /*
         * Existing Resend infrastructure is reused.
         *
         * The adapter method is added outside this package so
         * existing email flow itself remains untouched.
         */
        emailService.sendUnpaidInternshipApplicationReceivedEmailAsync(
                saved
        );

        return mapper.toResponse(saved);
    }

    @Override
    public Page<UnpaidInternshipApplicationResponse> getApplications(
            UnpaidInternshipApplicationStatus status,
            Pageable pageable
    ) {

        Page<UnpaidInternshipApplication> applications;

        if (status == null) {

            applications =
                    repository.findAllByOrderByCreatedAtDesc(
                            pageable
                    );

        } else {

            applications =
                    repository.findAllByStatusOrderByCreatedAtDesc(
                            status,
                            pageable
                    );
        }

        return applications.map(mapper::toResponse);
    }

    @Override
    public UnpaidInternshipApplicationResponse getApplication(
            String applicationId
    ) {

        return mapper.toResponse(
                findApplication(applicationId)
        );
    }

    @Override
    @Transactional
    public UnpaidInternshipApplicationResponse updateStatus(
            String applicationId,
            UnpaidInternshipApplicationStatus status
    ) {

        UnpaidInternshipApplication application =
                findApplication(applicationId);

        application.changeStatus(status);

        return mapper.toResponse(application);
    }

    @Override
    public Resource downloadResume(
            String applicationId
    ) {

        UnpaidInternshipApplication application =
                findApplication(applicationId);

        return storageService.download(
                application.getResumeStoragePath()
        );
    }

    private UnpaidInternshipApplication findApplication(
            String applicationId
    ) {

        return repository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unpaid internship application not found: "
                                        + applicationId
                        )
                );
    }
}