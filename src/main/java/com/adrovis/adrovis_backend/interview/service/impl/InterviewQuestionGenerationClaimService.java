package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionGenerationStatus;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionGenerationClaimService {

    private final InterviewRepository interviewRepository;

    @Transactional
    public boolean claimGeneration(UUID interviewId) {

        int updated = interviewRepository.claimQuestionGeneration(
                interviewId,
                InterviewQuestionGenerationStatus.PROCESSING,
                OffsetDateTime.now(),
                java.util.List.of(
                        InterviewQuestionGenerationStatus.NOT_STARTED,
                        InterviewQuestionGenerationStatus.FAILED
                )
        );

        if (updated == 1) {
            log.info(
                    "AI question generation claimed for interviewId={}",
                    interviewId
            );
            return true;
        }

        log.info(
                "Skipping AI generation for interviewId={} because it is already claimed or unavailable",
                interviewId
        );

        return false;
    }

    @Transactional
    public void markGenerationFailed(
            UUID interviewId,
            Exception exception
    ) {

        interviewRepository.findById(interviewId)
                .ifPresent(interview -> {

                    interview.setQuestionGenerationStatus(
                            InterviewQuestionGenerationStatus.FAILED
                    );

                    interview.setQuestionGenerationError(
                            safeErrorMessage(exception)
                    );

                    interview.setQuestionGenerationCompletedAt(
                            OffsetDateTime.now()
                    );

                    interviewRepository.save(interview);
                });
    }

    private String safeErrorMessage(Exception exception) {

        if (exception.getMessage() == null ||
                exception.getMessage().isBlank()) {

            return "AI interview preparation failed.";
        }

        return exception.getMessage();
    }
}