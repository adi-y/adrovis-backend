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
                WHO THIS IS FOR — READ THIS BEFORE ANYTHING ELSE
                ============================================================

                This interview package is for an ADROVIS INTERNSHIP
                interview, not a senior-engineer interview.

                The candidate is a college student, recent graduate, or
                early-career applicant.

                The interviewer conducting the call may ALSO be a recent
                graduate or someone with basic-to-intermediate technical
                knowledge. They will read your questions and expected
                answers LIVE, on a call, in real time. They will NOT have
                time to look anything up.

                Every question and every expected answer must work for that
                interviewer, in that moment:
                1. They read the question and understand it instantly.
                2. They ask it naturally, in their own voice.
                3. They glance at the expected answer and immediately know
                   what a reasonable response sounds like.
                4. They judge the candidate's actual answer against it
                   without needing outside knowledge.

                If an expected answer would require the interviewer to
                already know the topic well to understand your explanation
                of it, you have failed this task, regardless of whether the
                JSON is valid.

                ============================================================
                READABILITY — BEFORE AND AFTER EXAMPLES
                ============================================================

                These are not style suggestions. They are the bar every
                question and expected answer must clear.

                Example 1 — question phrasing
                BAD:  "Describe your implementation of cryptographically
                       signed authorization tokens within the Spring
                       Security filter chain."
                GOOD: "How did you handle authentication in your project?"

                Example 2 — question phrasing
                BAD:  "Explain the architectural differences between
                       stateless token-based authentication and traditional
                       session-based authentication."
                GOOD: "Why did you choose JWT authentication for this
                       project?"

                Example 3 — expected answer
                BAD:  "Candidate should explain configuring Spring Security
                       filters to intercept requests, validate JWT
                       signatures, and map user roles to specific endpoint
                       permissions."
                GOOD: "They should explain that JWT is used to identify
                       logged-in users, and that different roles get access
                       to different parts of the app."

                Example 4 — expected answer
                BAD:  "Candidate should discuss using Bucket4j library or a
                       Redis token-bucket algorithm inside a Spring Security
                       filter to track client request frequencies."
                GOOD: "They should suggest limiting how many requests one
                       user can make in a short time, and returning an error
                       once that limit is hit."

                A good expected answer tells the interviewer "what should I
                roughly expect this candidate to say" — it is not a mini
                technical spec. If you catch yourself naming a specific
                library, algorithm, or internal mechanism the candidate
                never mentioned, rewrite it in plain terms instead.

                ============================================================
                STAYING INSIDE THE CANDIDATE'S ACTUAL LEVEL
                ============================================================

                Do not introduce a technology, pattern, or concept the
                candidate has not demonstrated, even as "deeper" follow-up
                material. Concretely, do NOT reach for any of the following
                unless the resume or skills list explicitly shows the
                candidate has used it:

                - Redis, caching layers, or any distributed-systems concept
                - message queues, Kafka, event-driven architecture
                - Docker/Kubernetes orchestration, CI/CD pipeline internals
                - rate-limiting algorithms (token bucket, sliding window)
                - advanced Spring internals (bean lifecycle exceptions, AOP
                  proxies, circular dependency resolution)
                - enterprise design patterns, microservices decomposition,
                  system design at scale
                - database performance tuning beyond "why is this slow /
                  how would you speed it up" at a conceptual level

                A TOUGH question should push the candidate to think harder
                about something connected to their own background — a
                trade-off they made, a bug they had to chase, a decision
                they'd defend — not introduce a concept a working
                professional would need a year of experience to have
                opinions about.

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

                Use answerSource = "RESUME" only when the question is
                directly based on something explicitly present in the
                supplied resume.

                Use answerSource = "KNOWLEDGE" only when the question tests
                the SAME technology or concept the candidate has already
                named in their resume or skills list — not a
                professionally-adjacent concept they haven't mentioned. If
                the candidate lists "React.js", a KNOWLEDGE question can go
                deeper on React concepts; it should not jump to a
                state-management library they never mentioned.

                Use answerSource = "BEHAVIOURAL" for behavioural questions.

                NEVER invent resume information.

                If a technology, project, employer, responsibility,
                certification, achievement, or experience is not present
                in the supplied resume, do not claim that it is present.

                ============================================================
                DIFFICULTY RULES
                ============================================================

                EASY:
                Tests basic understanding of something the candidate claims
                to know. A confident candidate should answer this without
                hesitation.

                MEDIUM:
                Tests whether the candidate actually understands or can
                explain something they have worked with — not just recall
                a definition.

                TOUGH:
                Makes the candidate think a bit harder about a decision,
                trade-off, debugging situation, or practical scenario
                reasonably connected to their own background. A strong
                college student or recent graduate should still be able to
                answer it.

                TOUGH must NOT mean: senior-level system design,
                distributed systems, obscure framework internals,
                infrastructure the candidate hasn't demonstrated, or
                knowledge that normally requires professional work
                experience. See "STAYING INSIDE THE CANDIDATE'S ACTUAL
                LEVEL" above — those restrictions apply most strongly here.

                ============================================================
                QUESTION CONTENT RULES
                ============================================================

                Every question MUST be useful to a real interviewer and
                phrased the way a real person would ask it on a call —
                natural and conversational, not textbook wording. Re-read
                the READABILITY examples above before writing each
                question.

                PROJECT questions should be grounded in actual resume
                projects whenever possible, and should make clear why the
                question is being asked — test whether the candidate
                understands what they built, not just ask them to repeat
                resume bullet points.

                SKILL questions should relate to skills demonstrated
                by the candidate.

                TECHNICAL questions should test genuine technical
                understanding relevant to the role, staying within the
                level restrictions above.

                PRACTICAL questions should test how the candidate would
                apply knowledge in a realistic, intern-appropriate
                situation.

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

                If no follow-up is genuinely useful, use:

                "followUpQuestions": []

                Only generate a follow-up when it naturally deepens the
                discussion — not because the schema allows one. A follow-up
                must be at the SAME difficulty ceiling as its parent
                question, never harder, and must be just as plain-language
                as the examples above.

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
                Phrased the way a person would actually say it out loud on
                a call.

                expectedAnswer:
                1-2 concise sentences.
                Under 45 words.
                Plain, everyday language — no library names, algorithm
                names, or internal mechanisms unless the candidate would
                naturally use that exact word themselves. This is a quick
                "what to expect" cue for a non-expert interviewer, not a
                technical specification. If you would need to explain a
                term in your own expected answer, that term does not belong
                in it.

                keyPoints:
                JSON array of strings.
                Maximum 3 items.
                Each item under 8 words.
                Plain phrases an interviewer can scan in one glance — not
                jargon or implementation detail.
                Never use null.

                followUpQuestions:
                JSON array.
                Maximum 1 object.
                Empty array is allowed and often correct.

                follow-up question:
                Under 20 words.

                follow-up expectedAnswer:
                Under 30 words, same plain-language standard as
                expectedAnswer above.

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
                What the interviewer is actually trying to find out by
                asking this.


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
                10. followUpQuestions is always an array, and every
                    follow-up stays at or below its parent's difficulty
                    and jargon level.
                11. Every follow-up is an object with question and
                    expectedAnswer.
                12. resumeBasis is a string, including "" when not applicable.
                13. interviewerGoal is present and non-empty.
                14. closingPitch contains all 10 required fields.
                15. No question, expectedAnswer, or keyPoint names a
                    technology, library, or internal mechanism the
                    candidate never mentioned, unless answerSource is
                    KNOWLEDGE and it is the same technology already named
                    in the resume.
                16. Every expectedAnswer would make sense to an interviewer
                    who has never studied this topic in depth.
                17. No markdown.
                18. No ```json fences.
                19. No explanation before or after the JSON.
                20. Return valid JSON only.

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