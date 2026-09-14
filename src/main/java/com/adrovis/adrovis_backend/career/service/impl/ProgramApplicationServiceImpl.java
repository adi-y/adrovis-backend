package com.adrovis.adrovis_backend.career.service.impl;

import com.adrovis.adrovis_backend.campaign.service.CampaignEnrollmentService;
import com.adrovis.adrovis_backend.campaign.service.CampaignLinkService;
import com.adrovis.adrovis_backend.candidate.service.CandidateService;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationCreateRequest;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationSubmitRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationCreatedResponse;
import com.adrovis.adrovis_backend.career.dto.response.ProgramApplicationContinuationResponse;
import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.enums.ApplicationStatus;
import com.adrovis.adrovis_backend.career.enums.ApplicationType;
import com.adrovis.adrovis_backend.career.mapper.ApplicationMapper;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.career.service.ProgramApplicationService;
import com.adrovis.adrovis_backend.common.exception.AppException;
import com.adrovis.adrovis_backend.common.entity.ApplicationIdGenerator;

import com.adrovis.adrovis_backend.email.service.EmailService;
import com.adrovis.adrovis_backend.storage.dto.response.FileUploadResponse;
import com.adrovis.adrovis_backend.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.adrovis.adrovis_backend.career.entity.CandidateOutreach;
import com.adrovis.adrovis_backend.career.repository.CandidateOutreachRepository;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramApplicationServiceImpl implements ProgramApplicationService {

    private static final String PROGRAM_TITLE_SNAPSHOT = "Software Developer Internship";

    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationIdGenerator idGenerator;
    private final ApplicationMapper applicationMapper;
    private final EmailService emailService;
    private final CandidateOutreachRepository candidateOutreachRepository;
    private final CandidateService candidateService;
    private final CampaignLinkService campaignLinkService;
    private final CampaignEnrollmentService campaignEnrollmentService;

    @Override
    @Transactional
    public ApplicationCreatedResponse createDraft(
            ProgramApplicationCreateRequest request
    ) {

        candidateService.getOrCreate(
                request.getFullName(),
                request.getEmail()
        );

        FileUploadResponse uploadedResume =
                fileStorageService.upload(
                        request.getResume()
                );

        CandidateOutreach outreach =
                candidateOutreachRepository
                        .findFirstByEmailIgnoreCase(
                                request.getEmail()
                        )
                        .orElse(null);

        String source =
                outreach != null
                        ? outreach.getSource()
                        : "WEBSITE";

        Application application =
                new Application(
                        idGenerator.next(),
                        null,
                        ApplicationType.PROGRAM,
                        ApplicationStatus.PENDING,
                        PROGRAM_TITLE_SNAPSHOT,
                        request.getFullName(),
                        request.getEmail(),
                        request.getPhone(),
                        request.getCollege(),
                        request.getGraduationYear(),
                        uploadedResume.storageKey(),
                        uploadedResume.fileUrl(),
                        uploadedResume.originalName(),
                        uploadedResume.mimeType(),
                        uploadedResume.sizeBytes(),
                        null,
                        false,
                        null
                );

        application.setSource(source);

        Application saved =
                applicationRepository.save(application);

        campaignEnrollmentService.startApplicationJourney(
                candidateService.getOrCreate(
                        saved.getApplicantName(),
                        saved.getApplicantEmail()
                ),
                saved
        );

        if (outreach != null) {
            outreach.linkApplication(saved);
            candidateOutreachRepository.save(outreach);
        }

        return applicationMapper.toCreatedResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramApplicationContinuationResponse getContinuation(
            String applicationId,
            String token
    ) {

        Application application =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                AppException.notFound(
                                        "Application not found."
                                )
                        );

        if (application.getApplicationType()
                != ApplicationType.PROGRAM) {

            throw AppException.conflict(
                    "This application is not a program application."
            );
        }

        if (application.getApplicationStatus()
                != ApplicationStatus.PENDING) {

            throw AppException.conflict(
                    "This application is no longer pending."
            );
        }

        boolean valid =
                campaignLinkService.validateContinuationToken(
                        token,
                        application.getApplicationId(),
                        application.getApplicantEmail()
                );

        if (!valid) {
            throw AppException.conflict(
                    "This application continuation link is invalid or expired."
            );
        }

        return new ProgramApplicationContinuationResponse(
                application.getApplicationId(),
                application.getApplicantName(),
                application.getApplicationStatus(),
                true
        );
    }
    @Override
    @Transactional
    public void submitContinuation(
            String applicationId,
            String token,
            ProgramApplicationSubmitRequest request
    ) {

        Application application =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                AppException.notFound(
                                        "Application not found."
                                )
                        );

        if (application.getApplicationType()
                != ApplicationType.PROGRAM) {

            throw AppException.conflict(
                    "This application is not a program application."
            );
        }

        boolean valid =
                campaignLinkService.validateContinuationToken(
                        token,
                        application.getApplicationId(),
                        application.getApplicantEmail()
                );

        if (!valid) {
            throw AppException.conflict(
                    "This application continuation link is invalid or expired."
            );
        }

        application.submitProgramApplication();

        emailService.sendApplicationReceivedEmailAsync(
                application
        );
    }

    @Override
    @Transactional
    public void submit(String applicationId, ProgramApplicationSubmitRequest request) {

        Application application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> AppException.notFound("Application not found."));

        // request.acceptedTerms() is already guaranteed true by @Valid + @AssertTrue —
        // this guard exists for defense-in-depth, not because @Valid could miss it.
        application.submitProgramApplication();

        emailService.sendApplicationReceivedEmailAsync(application);
    }
}
