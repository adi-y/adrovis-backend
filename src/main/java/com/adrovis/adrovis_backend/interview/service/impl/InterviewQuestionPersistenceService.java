package com.adrovis.adrovis_backend.interview.service.impl;

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
import java.util.Map;
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionPersistenceService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final ObjectMapper objectMapper;

private static final String CLOSING_PITCH = """
Before we wrap up, I just want to tell you what ADROVIS is actually about, because I don't want you to look at this as just another internship where you pay a fee and get a certificate.

Actually, I started this because I personally went through this when I was around your age. When I was a fresher, I came across a lot of internships and programs asking for money. I even paid for one myself, and at the end, there was barely any real experience — mostly just a certificate.

And honestly, that's still happening with a lot of freshers today. You can have the skills, build projects and finish college, but when you sit for an interview, companies ask you about real experience — how you worked with a team, handled tasks, Git, reviews, bugs, deadlines and everything that actually happens inside a software company.

So when I started building ADROVIS, I wanted to approach it differently.

ADROVIS is a software company first. We're working on projects for businesses, and we're also building our own product. So the idea is to bring interns into that actual environment instead of putting them in a classroom.

Over these three months, you'll work with a team, get actual tasks, work on projects, use the tools and workflow we use internally, go through reviews, fix bugs, communicate with your team and gradually take more ownership.

And we're not expecting you to know everything on day one. Our expectation is that you learn, improve and become much more confident working like a software engineer by the end of those three months.

And if you perform really well, take ownership and we feel you're someone we want to continue with, we can also consider you for opportunities with ADROVIS after the internship. That's performance-based, so it's never something we guarantee upfront.

There's a ₹999 internship fee for the structured experience, mentorship, project work and onboarding. But I don't want you to think you're paying ₹999 for a certificate. The certificate is just documentation of what you complete — the real value is the experience you take away.

So my question to you is simple — if you had the opportunity to get the kind of practical experience you wish you had as a fresher, would that be valuable for you right now?
""";

    @Transactional
    public void persist(
            Interview interview,
            AiInterviewPackage packageData
    ) throws JsonProcessingException {

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

        interview.setAiClosingPitch(
                objectMapper.writeValueAsString(
                        Map.of("script", CLOSING_PITCH)
                )
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