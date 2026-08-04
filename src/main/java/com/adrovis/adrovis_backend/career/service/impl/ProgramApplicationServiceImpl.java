package com.adrovis.adrovis_backend.career.service.impl;

import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationCreateRequest;
import com.adrovis.adrovis_backend.career.dto.request.ProgramApplicationSubmitRequest;
import com.adrovis.adrovis_backend.career.dto.response.ApplicationCreatedResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramApplicationServiceImpl implements ProgramApplicationService {

    private static final String PROGRAM_TITLE_SNAPSHOT = "Associate Software Engineer Program";

    private final ApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationIdGenerator idGenerator;
    private final ApplicationMapper applicationMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApplicationCreatedResponse createDraft(ProgramApplicationCreateRequest request) {

        FileUploadResponse uploadedResume = fileStorageService.upload(request.getResume());

        Application application = new Application(
                idGenerator.next(),
                null,                              // no Job for PROGRAM type
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
                null,                               // note — not applicable to PROGRAM
                false,                               // isConsent — not yet
                null                                  // submittedAt — not yet
        );

        Application saved = applicationRepository.save(application);

        return applicationMapper.toCreatedResponse(saved);
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