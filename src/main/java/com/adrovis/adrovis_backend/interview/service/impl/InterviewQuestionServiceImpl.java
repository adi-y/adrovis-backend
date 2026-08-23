package com.adrovis.adrovis_backend.interview.service.impl;

import com.adrovis.adrovis_backend.interview.dto.response.InterviewQuestionResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewQuestionsResponse;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewQuestion;
import com.adrovis.adrovis_backend.interview.repository.InterviewQuestionRepository;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.adrovis.adrovis_backend.interview.service.InterviewQuestionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    @Override
    @Transactional(readOnly = true)
    public InterviewQuestionsResponse getQuestions(UUID interviewId) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Interview not found: " + interviewId
                        ));

        List<InterviewQuestionResponse> questions =
                interviewQuestionRepository
                        .findByInterviewIdOrderByQuestionNumberAsc(interviewId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return InterviewQuestionsResponse.builder()
                .interviewId(interview.getId())
                .generationStatus(interview.getQuestionGenerationStatus())
                .generationError(interview.getQuestionGenerationError())
                .questions(questions)
                .build();
    }

    private InterviewQuestionResponse toResponse(InterviewQuestion question) {

        return InterviewQuestionResponse.builder()
                .id(question.getId())
                .questionNumber(question.getQuestionNumber())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .answerSource(question.getAnswerSource())
                .question(question.getQuestion())
                .expectedAnswer(question.getExpectedAnswer())
                .keyPoints(parseJsonArray(question.getKeyPoints()))
                .followUpQuestions(parseJsonArray(question.getFollowUpQuestions()))
                .resumeBasis(question.getResumeBasis())
                .interviewerGoal(question.getInterviewerGoal())
                .build();
    }

    private List<String> parseJsonArray(String json) {
        // Temporary implementation.
        // We'll replace this with proper JSON mapping when we wire
        // the JSONB fields into the application.
        return List.of();
    }
}