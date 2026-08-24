package com.adrovis.adrovis_backend.interview.service;

import com.adrovis.adrovis_backend.interview.dto.ai.AiClosingPitch;
import com.adrovis.adrovis_backend.interview.dto.ai.AiFollowUpQuestion;
import com.adrovis.adrovis_backend.interview.dto.ai.AiInterviewPackage;
import com.adrovis.adrovis_backend.interview.dto.ai.AiInterviewQuestion;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InterviewQuestionGenerationValidator {

    private static final int MAX_QUESTIONS = 15;
    private static final int MIN_QUESTIONS = 10;

    public void validate(
            AiInterviewPackage packageData
    ) {

        if (packageData == null) {
            throw new IllegalStateException(
                    "AI interview response was empty."
            );
        }

        List<AiInterviewQuestion> questions =
                packageData.questions();

        if (questions == null
                || questions.size() < MIN_QUESTIONS
                || questions.size() > MAX_QUESTIONS) {

            throw new IllegalStateException(
                    "AI interview response must contain between "
                            + MIN_QUESTIONS
                            + " and "
                            + MAX_QUESTIONS
                            + " questions (got "
                            + (questions == null ? 0 : questions.size())
                            + ")."
            );
        }

        Set<Integer> numbers =
                new HashSet<>();

        for (int index = 0; index < questions.size(); index++) {

            AiInterviewQuestion question =
                    questions.get(index);

            if (question == null) {
                throw new IllegalStateException(
                        "AI interview response contains an empty question."
                );
            }

            Integer number =
                    question.questionNumber();

            if (number == null
                    || number < 1
                    || number > MAX_QUESTIONS
                    || !numbers.add(number)) {

                throw new IllegalStateException(
                        "AI interview question numbers are invalid or duplicated."
                );
            }

            if (isBlank(question.question())) {
                throw new IllegalStateException(
                        "AI interview question text is missing."
                );
            }

            if (isBlank(question.expectedAnswer())) {
                throw new IllegalStateException(
                        "AI interview expected answer is missing."
                );
            }

            if (question.category() == null
                    || question.difficulty() == null
                    || question.answerSource() == null) {

                throw new IllegalStateException(
                        "AI interview question classification is invalid."
                );
            }

            if (isBlank(question.interviewerGoal())) {
                throw new IllegalStateException(
                        "AI interview interviewer goal is missing."
                );
            }

            if (question.keyPoints() == null) {
                throw new IllegalStateException(
                        "AI interview key points are missing."
                );
            }

            List<AiFollowUpQuestion> followUps =
                    question.followUpQuestions();

            if (followUps == null) {
                throw new IllegalStateException(
                        "AI interview follow-up questions are missing."
                );
            }

            if (followUps.size() > 1) {
                throw new IllegalStateException(
                        "AI interview contains too many follow-up questions."
                );
            }

            for (AiFollowUpQuestion followUp : followUps) {

                if (followUp == null
                        || isBlank(followUp.question())
                        || isBlank(followUp.expectedAnswer())) {

                    throw new IllegalStateException(
                            "AI interview follow-up answer is missing."
                    );
                }
            }
        }

        validateClosingPitch(
                packageData.closingPitch()
        );
    }

    private void validateClosingPitch(
            AiClosingPitch pitch
    ) {

        if (pitch == null) {
            throw new IllegalStateException(
                    "AI interview closing pitch is missing."
            );
        }

        if (pitch.candidateStrengths() == null
                || pitch.candidateGaps() == null
                || pitch.programValuePoints() == null) {

            throw new IllegalStateException(
                    "AI interview closing pitch is incomplete."
            );
        }

        if (isBlank(pitch.bestValueAngle())
                || isBlank(pitch.transition())
                || isBlank(pitch.candidateSpecificPitch())
                || isBlank(pitch.feeExplanation())
                || isBlank(pitch.commitmentMessage())
                || isBlank(pitch.ppoMessage())
                || isBlank(pitch.closingQuestion())) {

            throw new IllegalStateException(
                    "AI interview closing pitch contains missing content."
            );
        }
    }

    private boolean isBlank(String value) {

        return value == null
                || value.isBlank();
    }
}