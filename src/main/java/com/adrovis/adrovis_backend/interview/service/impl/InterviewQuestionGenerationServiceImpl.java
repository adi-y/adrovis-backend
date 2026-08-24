package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.interview.client.GeminiInterviewClient;
import com.adrovis.adrovis_backend.interview.dto.ai.AiInterviewPackage;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.adrovis.adrovis_backend.interview.service.InterviewPromptBuilder;
import com.adrovis.adrovis_backend.interview.service.InterviewQuestionGenerationService;
import com.adrovis.adrovis_backend.interview.service.InterviewQuestionGenerationValidator;
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

    private final InterviewPromptBuilder promptBuilder;

    private final GeminiInterviewClient geminiInterviewClient;

    private final InterviewQuestionGenerationValidator validator;

    private final InterviewQuestionPersistenceService persistenceService;

    @Override
    @Async("taskExecutor")
    public void generateIfNeeded(UUID interviewId) {

        if (!claimService.claimGeneration(interviewId)) {

            log.debug(
                    "AI generation skipped because generation is already claimed. interviewId={}",
                    interviewId
            );

            return;
        }

        long startedAt =
                System.currentTimeMillis();

        log.info(
                "AI interview question generation started. interviewId={}",
                interviewId
        );

        try {

            Interview interview =
                    interviewRepository
                            .findById(interviewId)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Interview not found."
                                    )
                            );

            Application application =
                    applicationRepository
                            .findById(
                                    interview.getApplicationId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Application not found."
                                    )
                            );

            String resumeStorageKey =
                    application.getResumeStorageKey();

            if (resumeStorageKey == null
                    || resumeStorageKey.isBlank()) {

                throw new IllegalStateException(
                        "Application does not have a resume."
                );
            }

            log.info(
                    "Extracting resume for AI interview generation. interviewId={}",
                    interviewId
            );

            String resumeText =
                    resumeTextExtractorService.extractText(
                            resumeStorageKey
                    );

            log.info(
                    "Resume extraction completed for AI generation. interviewId={}, characters={}",
                    interviewId,
                    resumeText.length()
            );

            /*
             * IMPORTANT:
             * Resume text is never logged.
             */

            String prompt =
                    promptBuilder.build(
                            application,
                            interview,
                            resumeText
                    );

            log.debug(
                    "Gemini prompt prepared for interviewId={}, promptCharacters={}",
                    interviewId,
                    prompt.length()
            );

            long geminiStartedAt =
                    System.currentTimeMillis();

            AiInterviewPackage aiPackage =
                    geminiInterviewClient.generate(
                            prompt
                    );

            log.info(
                    "Gemini generation completed for interviewId={}, durationMs={}",
                    interviewId,
                    System.currentTimeMillis()
                            - geminiStartedAt
            );

            validator.validate(
                    aiPackage
            );

            log.info(
                    "AI interview response validated successfully. interviewId={}, questionCount={}",
                    interviewId,
                    aiPackage.questions().size()
            );

            persistenceService.persist(
                    interview,
                    aiPackage
            );

            log.info(
                    "AI interview question generation completed. interviewId={}, durationMs={}, questionCount={}",
                    interviewId,
                    System.currentTimeMillis()
                            - startedAt,
                    aiPackage.questions().size()
            );

        } catch (Exception ex) {

            claimService.markGenerationFailed(
                    interviewId,
                    ex
            );

            log.error(
                    "AI interview question generation failed. interviewId={}, durationMs={}, reason={}",
                    interviewId,
                    System.currentTimeMillis()
                            - startedAt,
                    ex.getMessage(),
                    ex
            );
        }
    }
}