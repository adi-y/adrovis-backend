package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.adrovis.adrovis_backend.interview.service.InterviewQuestionGenerationService;
import com.adrovis.adrovis_backend.interview.service.ResumeTextExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionGenerationServiceImpl
        implements InterviewQuestionGenerationService {

    private final InterviewQuestionGenerationClaimService claimService;
    private final ResumeTextExtractorService resumeTextExtractorService;
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Async("taskExecutor")
    public void generateIfNeeded(UUID interviewId) {

        if (!claimService.claimGeneration(interviewId)) {
            return;
        }

        log.info(
                "AI interview question generation started for interviewId={}",
                interviewId
        );

        try {

            // 1. Load Interview
            Interview interview = interviewRepository
                    .findById(interviewId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Interview not found: " + interviewId
                            )
                    );

            // 2. Load Application using Interview.applicationId
            Application application = applicationRepository
                    .findById(interview.getApplicationId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Application not found: "
                                            + interview.getApplicationId()
                            )
                    );

            // 3. Get resume storage key
            String resumeStorageKey =
                    application.getResumeStorageKey();

            if (resumeStorageKey == null ||
                    resumeStorageKey.isBlank()) {

                throw new IllegalStateException(
                        "Application does not have a resume."
                );
            }

            log.info(
                    "Extracting resume text for interviewId={}, storageKey={}",
                    interviewId,
                    resumeStorageKey
            );

            // 4. Extract resume text using Tika
            String resumeText =
                    resumeTextExtractorService.extractText(
                            resumeStorageKey
                    );

            log.info(
                    "Resume text extracted successfully for interviewId={}, characters={}",
                    interviewId,
                    resumeText.length()
            );

            // TODO:
            // 5. Build Gemini prompt
            // 6. Call Gemini
            // 7. Validate exactly 15 questions
            // 8. Persist questions + closing pitch
            // 9. Mark generation READY

        } catch (Exception ex) {

            claimService.markGenerationFailed(
                    interviewId,
                    ex
            );

            log.error(
                    "AI interview question generation failed for interviewId={}",
                    interviewId,
                    ex
            );
        }
    }
}