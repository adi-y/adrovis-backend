package com.adrovis.adrovis_backend.interview.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.interview.config.GeminiInterviewProperties;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewPromptBuilder {

    private final GeminiInterviewProperties properties;

    public String build(
            Application application,
            Interview interview,
            String resumeText
    ) {

        String normalizedResume =
                normalizeAndLimit(resumeText);

        return """
                You are the ADROVIS Interview Preparation Engine.

                Generate a production-quality interview preparation package
                for a human interviewer.

                IMPORTANT:
                The backend parses your response directly into Java DTOs.
                Therefore the JSON structure and enum values below are
                mandatory. Do not invent field names or change the structure.

                ============================================================
                CANDIDATE CONTEXT
                ============================================================

                Candidate name:
                %s

                Application ID:
                %s

                Application type:
                %s

                Job / Program:
                %s

                College:
                %s

                Graduation year:
                %s

                Candidate note:
                %s

                ============================================================
                RESUME
                ============================================================

                %s

                ============================================================
                INTERVIEW QUESTION RULES
                ============================================================

                Generate EXACTLY 15 interview questions.

                The question numbers MUST be:
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15

                Do not skip numbers.
                Do not duplicate numbers.
                Do not use numbers outside 1-15.

                Preferred question distribution:

                - 4 PROJECT questions
                - 3 SKILL questions
                - 4 TECHNICAL questions
                - 2 PRACTICAL questions
                - 2 BEHAVIOURAL questions

                The distribution may flex if the resume does not provide
                enough evidence, but there MUST still be exactly 15 questions.

                ============================================================
                ENUM VALUES — ABSOLUTELY STRICT
                ============================================================

                category MUST be exactly one of:

                "PROJECT"
                "SKILL"
                "TECHNICAL"
                "PRACTICAL"
                "BEHAVIOURAL"

                difficulty MUST be exactly one of:

                "EASY"
                "MEDIUM"
                "TOUGH"

                answerSource MUST be exactly one of:

                "RESUME"
                "KNOWLEDGE"
                "BEHAVIOURAL"

                These values are case-sensitive.

                NEVER output:
                "Project"
                "Skill"
                "Technical"
                "Practical"
                "Behavioural"
                "Resume"
                "Knowledge"
                "Easy"
                "Medium"
                "Tough"

                Always use the uppercase values shown above.

                ============================================================
                ANSWER SOURCE RULES
                ============================================================

                Use answerSource = "RESUME" when the question is directly
                based on information explicitly present in the supplied resume.

                Use answerSource = "KNOWLEDGE" when the question tests
                technical or professional knowledge related to the
                candidate's demonstrated skills.

                Use answerSource = "BEHAVIOURAL" for behavioural questions.

                NEVER invent resume information.

                If a technology, project, employer, responsibility,
                certification, achievement, or experience is not present
                in the supplied resume, do not claim that it is present.

                ============================================================
                DIFFICULTY RULES
                ============================================================

                EASY:
                Fundamental understanding or straightforward application.

                MEDIUM:
                Practical understanding and normal interview-level depth.

                TOUGH:
                Deeper reasoning, trade-offs, debugging, architecture,
                optimization, or advanced understanding.

                TOUGH does not mean obscure trivia or trick questions.

                ============================================================
                QUESTION CONTENT RULES
                ============================================================

                Every question MUST be useful to a real interviewer.

                PROJECT questions should be grounded in actual resume
                projects whenever possible.

                SKILL questions should relate to skills demonstrated
                by the candidate.

                TECHNICAL questions should test genuine technical
                understanding relevant to the role.

                PRACTICAL questions should test how the candidate would
                apply knowledge in realistic situations.

                BEHAVIOURAL questions should evaluate behaviour,
                communication, ownership, adaptability, teamwork,
                problem solving, or similar professional qualities.

                Do not fabricate what the candidate actually did.

                ============================================================
                EXACT QUESTION OBJECT STRUCTURE
                ============================================================

                Every item inside "questions" MUST contain EXACTLY these
                fields:

                {
                  "questionNumber": 1,
                  "category": "PROJECT",
                  "difficulty": "MEDIUM",
                  "answerSource": "RESUME",
                  "question": "Question text",
                  "expectedAnswer": "Expected answer guidance",
                  "keyPoints": [
                    "Key point one",
                    "Key point two"
                  ],
                  "followUpQuestions": [
                    {
                      "question": "Follow-up question",
                      "expectedAnswer": "Expected follow-up answer"
                    }
                  ],
                  "resumeBasis": "Relevant resume evidence",
                  "interviewerGoal": "What the interviewer should evaluate"
                }

                IMPORTANT:

                "followUpQuestions" MUST ALWAYS be a JSON ARRAY.

                A follow-up item MUST ALWAYS be a JSON OBJECT containing
                exactly these two fields:

                {
                  "question": "...",
                  "expectedAnswer": "..."
                }

                NEVER do this:

                "followUpQuestions": [
                  "some question"
                ]

                NEVER do this:

                "followUpQuestions": "some question"

                NEVER do this:

                "followUpQuestions": null

                If no follow-up is useful, use:

                "followUpQuestions": []

                ============================================================
                FIELD RULES
                ============================================================

                questionNumber:
                Integer from 1 through 15.

                category:
                One of the exact uppercase enum values defined above.

                difficulty:
                One of the exact uppercase enum values defined above.

                answerSource:
                One of the exact uppercase enum values defined above.

                question:
                One sentence.
                Under 30 words.

                expectedAnswer:
                1-2 concise sentences.
                Under 45 words.
                This is interviewer evaluation guidance, not a fabricated
                candidate response.

                keyPoints:
                JSON array of strings.
                Maximum 3 items.
                Each item under 8 words.
                Never use null.

                followUpQuestions:
                JSON array.
                Maximum 1 object.
                Empty array is allowed.

                follow-up question:
                Under 20 words.

                follow-up expectedAnswer:
                Under 30 words.

                resumeBasis:
                If answerSource is "RESUME", provide concise evidence
                directly supported by the supplied resume.
                Under 15 words.

                If answerSource is "KNOWLEDGE" or "BEHAVIOURAL",
                use an empty string:

                "resumeBasis": ""

                Do not invent resume evidence.

                interviewerGoal:
                One sentence.
                Under 20 words.

                ============================================================
                CLOSING PITCH
                ============================================================

                The root object MUST also contain a "closingPitch" object.

                "closingPitch" MUST contain EXACTLY these fields:

                {
                  "candidateStrengths": [
                    "Strength one"
                  ],
                  "candidateGaps": [
                    "Gap one"
                  ],
                  "bestValueAngle": "One sentence",
                  "transition": "One sentence",
                  "candidateSpecificPitch": "Two or three concise sentences",
                  "programValuePoints": [
                    "Value point one"
                  ],
                  "feeExplanation": "One or two transparent sentences",
                  "commitmentMessage": "One sentence",
                  "ppoMessage": "One sentence",
                  "closingQuestion": "One sentence"
                }

                candidateStrengths:
                Maximum 3 short items.

                candidateGaps:
                Maximum 2 short items.

                programValuePoints:
                Maximum 3 short items.

                bestValueAngle:
                One sentence.

                transition:
                One sentence.

                candidateSpecificPitch:
                Maximum 3 concise sentences.

                feeExplanation:
                1-2 sentences.
                Transparently explain the Rs.999 program/interview-preparation
                fee.
                Do not make misleading claims.

                commitmentMessage:
                One sentence.

                ppoMessage:
                One sentence.
                PPO must NEVER be presented as guaranteed.

                closingQuestion:
                One sentence.

                Ground the closing pitch in the candidate information
                and resume actually provided.

                Do not fabricate achievements.

                ============================================================
                COMPLETE JSON SHAPE
                ============================================================

                The final response MUST have exactly this root structure:

                {
                  "questions": [
                    {
                      "questionNumber": 1,
                      "category": "PROJECT",
                      "difficulty": "MEDIUM",
                      "answerSource": "RESUME",
                      "question": "...",
                      "expectedAnswer": "...",
                      "keyPoints": [
                        "...",
                        "..."
                      ],
                      "followUpQuestions": [
                        {
                          "question": "...",
                          "expectedAnswer": "..."
                        }
                      ],
                      "resumeBasis": "...",
                      "interviewerGoal": "..."
                    }
                  ],
                  "closingPitch": {
                    "candidateStrengths": [
                      "..."
                    ],
                    "candidateGaps": [
                      "..."
                    ],
                    "bestValueAngle": "...",
                    "transition": "...",
                    "candidateSpecificPitch": "...",
                    "programValuePoints": [
                      "..."
                    ],
                    "feeExplanation": "...",
                    "commitmentMessage": "...",
                    "ppoMessage": "...",
                    "closingQuestion": "..."
                  }
                }

                The "questions" array MUST contain exactly 15 objects.

                The example above shows the required STRUCTURE only.
                Generate the actual 15 questions from the candidate
                context and resume.

                ============================================================
                FINAL VALIDATION BEFORE RESPONDING
                ============================================================

                Before returning the response, internally verify:

                1. Root object contains "questions".
                2. Root object contains "closingPitch".
                3. questions contains exactly 15 objects.
                4. questionNumber values are exactly 1 through 15.
                5. Every question has all 10 required fields.
                6. category uses only valid uppercase enum values.
                7. difficulty uses only valid uppercase enum values.
                8. answerSource uses only valid uppercase enum values.
                9. keyPoints is always an array.
                10. followUpQuestions is always an array.
                11. Every follow-up is an object with question and
                    expectedAnswer.
                12. resumeBasis is a string, including "" when not applicable.
                13. interviewerGoal is present and non-empty.
                14. closingPitch contains all 10 required fields.
                15. No markdown.
                16. No ```json fences.
                17. No explanation before or after the JSON.
                18. Return valid JSON only.

                ============================================================
                ABSOLUTE OUTPUT RULE
                ============================================================

                Return ONLY one valid JSON object.

                Do not return markdown.
                Do not return code fences.
                Do not return commentary.
                Do not return explanations.
                Do not return multiple JSON objects.

                The response will be parsed directly by the backend.
                Invalid JSON or invalid enum values will cause processing
                to fail.

                Generate the complete JSON now.
                """.formatted(
                safe(application.getApplicantName()),
                safe(application.getApplicationId()),
                application.getApplicationType() == null
                        ? "UNKNOWN"
                        : application.getApplicationType().name(),
                safe(application.getJobTitleSnapshot()),
                safe(application.getCollege()),
                application.getGraduationYear() == null
                        ? "Not provided"
                        : application.getGraduationYear().toString(),
                safe(application.getNote()),
                normalizedResume
        );
    }

    private String normalizeAndLimit(
            String resumeText
    ) {

        if (resumeText == null
                || resumeText.isBlank()) {

            throw new IllegalArgumentException(
                    "Resume text is empty."
            );
        }

        String normalized =
                resumeText
                        .replaceAll("\\r\\n?", "\n")
                        .replaceAll("[ \\t]+", " ")
                        .replaceAll("\\n{3,}", "\n\n")
                        .trim();

        if (normalized.length()
                > properties.getMaxResumeChars()) {

            normalized =
                    normalized.substring(
                            0,
                            properties.getMaxResumeChars()
                    );
        }

        return normalized;
    }

    private String safe(String value) {

        if (value == null
                || value.isBlank()) {

            return "Not provided";
        }

        return value;
    }
}