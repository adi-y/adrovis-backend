package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.interview.dto.response.ClosingPitchResponse;
import com.adrovis.adrovis_backend.interview.dto.response.FollowUpQuestionResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewQuestionResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewQuestionsResponse;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewQuestion;
import com.adrovis.adrovis_backend.interview.repository.InterviewQuestionRepository;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.adrovis.adrovis_backend.interview.service.InterviewQuestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionServiceImpl
        implements InterviewQuestionService {

    private final ApplicationRepository applicationRepository;

    private final InterviewRepository interviewRepository;

    private final InterviewQuestionRepository interviewQuestionRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public InterviewQuestionsResponse getQuestions(
            String applicationId
    ) {

        log.debug(
                "Fetching AI interview questions. applicationId={}",
                applicationId
        );

        Application application =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Application not found: "
                                                + applicationId
                                )
                        );

        Interview interview =
                interviewRepository
                        .findByApplicationId(
                                application.getId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Interview not found for application: "
                                                + applicationId
                                )
                        );

        List<InterviewQuestion> entities =
                interviewQuestionRepository
                        .findByInterviewIdOrderByQuestionNumberAsc(
                                interview.getId()
                        );

        List<InterviewQuestionResponse> questions =
                entities.stream()
                        .map(this::toResponse)
                        .toList();

        ClosingPitchResponse closingPitch =
                parseClosingPitch(
                        interview.getAiClosingPitch()
                );

        return InterviewQuestionsResponse.builder()
                .applicationId(
                        application.getApplicationId()
                )
                .interviewId(
                        interview.getId()
                )
                .generationStatus(
                        interview.getQuestionGenerationStatus()
                )
                .totalQuestions(
                        questions.size()
                )
                .questions(
                        questions
                )
                .closingPitch(
                        closingPitch
                )
                .generationError(
                        interview.getQuestionGenerationError()
                )
                .build();
    }

    private InterviewQuestionResponse toResponse(
            InterviewQuestion question
    ) {

        return InterviewQuestionResponse.builder()
                .id(question.getId())
                .questionNumber(
                        question.getQuestionNumber()
                )
                .category(
                        question.getCategory()
                )
                .difficulty(
                        question.getDifficulty()
                )
                .answerSource(
                        question.getAnswerSource()
                )
                .question(
                        question.getQuestion()
                )
                .expectedAnswer(
                        question.getExpectedAnswer()
                )
                .keyPoints(
                        parseStringArray(
                                question.getKeyPoints()
                        )
                )
                .followUpQuestions(
                        parseFollowUps(
                                question.getFollowUpQuestions()
                        )
                )
                .resumeBasis(
                        question.getResumeBasis()
                )
                .interviewerGoal(
                        question.getInterviewerGoal()
                )
                .build();
    }

    private List<String> parseStringArray(
            String json
    ) {

        if (json == null
                || json.isBlank()) {

            return Collections.emptyList();
        }

        try {

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {
                    }
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to parse interview question key points JSON.",
                    ex
            );

            return Collections.emptyList();
        }
    }

    private List<FollowUpQuestionResponse> parseFollowUps(
            String json
    ) {

        if (json == null
                || json.isBlank()) {

            return Collections.emptyList();
        }

        try {

            return objectMapper.readValue(
                    json,
                    new TypeReference<
                            List<FollowUpQuestionResponse>
                            >() {
                    }
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to parse interview follow-up JSON.",
                    ex
            );

            return Collections.emptyList();
        }
    }

    private ClosingPitchResponse parseClosingPitch(
            String json
    ) {

        if (json == null
                || json.isBlank()) {

            return null;
        }

        try {

            JsonNode node =
                    objectMapper.readTree(json);

            return objectMapper.treeToValue(
                    node,
                    ClosingPitchResponse.class
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to parse AI closing pitch JSON.",
                    ex
            );

            return null;
        }
    }
}