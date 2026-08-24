package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.interview.dto.ai.AiClosingPitch;
import com.adrovis.adrovis_backend.interview.dto.ai.AiInterviewPackage;
import com.adrovis.adrovis_backend.interview.dto.ai.AiInterviewQuestion;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewQuestion;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionGenerationStatus;
import com.adrovis.adrovis_backend.interview.repository.InterviewQuestionRepository;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionPersistenceService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void persist(
            Interview interview,
            AiInterviewPackage packageData
    ) {

        log.info(
                "Persisting AI interview package. interviewId={}, questionCount={}",
                interview.getId(),
                packageData.questions().size()
        );

        /*
         * Retry from FAILED must replace any stale/partial package.
         *
         * Because this method is transactional, deletion + insertion +
         * READY status are committed atomically.
         */
        interviewQuestionRepository.deleteByInterviewId(
                interview.getId()
        );

        for (AiInterviewQuestion aiQuestion :
                packageData.questions()) {

            InterviewQuestion question =
                    InterviewQuestion.builder()
                            .interviewId(interview.getId())
                            .questionNumber(
                                    aiQuestion.questionNumber()
                            )
                            .category(
                                    aiQuestion.category()
                            )
                            .difficulty(
                                    aiQuestion.difficulty()
                            )
                            .answerSource(
                                    aiQuestion.answerSource()
                            )
                            .question(
                                    aiQuestion.question().trim()
                            )
                            .expectedAnswer(
                                    aiQuestion.expectedAnswer().trim()
                            )
                            .keyPoints(
                                    writeJson(
                                            aiQuestion.keyPoints()
                                    )
                            )
                            .followUpQuestions(
                                    writeJson(
                                            aiQuestion.followUpQuestions()
                                    )
                            )
                            .resumeBasis(
                                    blankToNull(
                                            aiQuestion.resumeBasis()
                                    )
                            )
                            .interviewerGoal(
                                    aiQuestion.interviewerGoal().trim()
                            )
                            .build();

            interviewQuestionRepository.save(
                    question
            );
        }

        AiClosingPitch closingPitch =
                packageData.closingPitch();

        interview.setAiClosingPitch(
                writeJson(closingPitch)
        );

        interview.setQuestionGenerationStatus(
                InterviewQuestionGenerationStatus.READY
        );

        interview.setQuestionGenerationError(
                null
        );

        interview.setQuestionGenerationCompletedAt(
                OffsetDateTime.now()
        );

        interviewRepository.save(interview);

        log.info(
                "AI interview package persisted successfully. interviewId={}, questionCount=15, status=READY",
                interview.getId()
        );
    }

    private String writeJson(Object value) {

        try {

            return objectMapper.writeValueAsString(value);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "AI interview package could not be serialized.",
                    ex
            );
        }
    }

    private String blankToNull(String value) {

        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}