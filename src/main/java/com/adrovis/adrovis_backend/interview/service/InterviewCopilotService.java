package com.adrovis.adrovis_backend.interview.service;

import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.interview.dto.request.InterviewCopilotSaveRequest;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.interview.config.InterviewCopilotProperties;
import com.adrovis.adrovis_backend.interview.dto.request.InterviewCopilotRequest;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewCopilotResponse;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewQuestion;
import com.adrovis.adrovis_backend.interview.repository.InterviewQuestionRepository;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewCopilotService {

    private static final Pattern QUESTION_REFERENCE =
            Pattern.compile(
                    "\\bQ(?:Z)?\\s*(\\d{1,2})\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private final ApplicationRepository applicationRepository;

    private final InterviewRepository interviewRepository;

    private final InterviewQuestionRepository interviewQuestionRepository;

    private final InterviewCopilotProperties properties;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    public InterviewCopilotResponse ask(
            String applicationId,
            InterviewCopilotRequest request
    ) {

        Application application =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
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
                                new IllegalArgumentException(
                                        "Interview not found for application: "
                                                + applicationId
                                )
                        );

        List<InterviewQuestion> questions =
                interviewQuestionRepository
                        .findByInterviewIdOrderByQuestionNumberAsc(
                                interview.getId()
                        );

        String rawMessage =
                request.message() == null
                        ? ""
                        : request.message().trim();

        Integer questionNumber =
                request.questionNumber();

        if (questionNumber == null) {
            questionNumber =
                    extractQuestionNumber(rawMessage);
        }

        InterviewQuestion currentQuestion =
                findQuestion(
                        questions,
                        questionNumber
                );

        ParsedCommand parsedCommand =
                parseCommand(
                        request.command(),
                        rawMessage
                );

        if (questionNumber == null
                && parsedCommand.commandNeedsQuestionContext()) {

            throw new IllegalArgumentException(
                    "A question number is required for this command."
            );
        }

        String prompt =
                buildPrompt(
                        application,
                        currentQuestion,
                        questions,
                        parsedCommand,
                        rawMessage
                );


        GeminiResult result =
                generate(
                        prompt
                );

        UUID savedNoteId = null;

        if (Boolean.TRUE.equals(request.save())) {

            savedNoteId =
                    saveNote(
                            interview,
                            currentQuestion,
                            parsedCommand,
                            rawMessage,
                            result.answer(),
                            result.sources()
                    );
        }

        log.info(
                "Interview copilot completed. applicationId={}, interviewId={}, questionNumber={}, command={}, research={}, saved={}",
                applicationId,
                interview.getId(),
                questionNumber,
                parsedCommand.command(),
                savedNoteId != null
        );

        return new InterviewCopilotResponse(
                parsedCommand.command(),
                questionNumber,
                currentQuestion == null
                        ? null
                        : currentQuestion.getQuestion(),
                result.answer(),
                result.sources(),
                savedNoteId
        );
    }

    private InterviewQuestion findQuestion(
            List<InterviewQuestion> questions,
            Integer questionNumber
    ) {

        if (questionNumber == null) {
            return null;
        }

        return questions.stream()
                .filter(question ->
                        questionNumber.equals(
                                question.getQuestionNumber()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Question "
                                        + questionNumber
                                        + " was not found."
                        )
                );
    }

    private Integer extractQuestionNumber(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return null;
        }

        Matcher matcher =
                QUESTION_REFERENCE.matcher(
                        message
                );

        if (!matcher.find()) {
            return null;
        }

        return Integer.valueOf(
                matcher.group(1)
        );
    }

    private ParsedCommand parseCommand(
            String explicitCommand,
            String message
    ) {

        String command =
                normalizeCommand(
                        explicitCommand
                );

        String cleanedMessage =
                message == null
                        ? ""
                        : message.trim();

        if (command.isBlank()
                && cleanedMessage.startsWith("/")) {

            int space =
                    cleanedMessage.indexOf(' ');

            String token =
                    space > 0
                            ? cleanedMessage.substring(
                            0,
                            space
                    )
                            : cleanedMessage;

            command =
                    normalizeCommand(token);

            cleanedMessage =
                    space > 0
                            ? cleanedMessage.substring(
                            space + 1
                    ).trim()
                            : "";
        }

        return new ParsedCommand(
                command.isBlank()
                        ? "GENERAL"
                        : command,
                cleanedMessage
        );
    }

    private String normalizeCommand(
            String command
    ) {

        if (command == null
                || command.isBlank()) {

            return "";
        }

        String normalized =
                command
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (normalized.startsWith("/")) {
            normalized =
                    normalized.substring(1);
        }

        return switch (normalized) {

            case "SUM",
                 "SUMMARY" ->
                    "SUMMARY";

            case "ANS",
                 "ANSWER" ->
                    "ANSWER";

            case "RESEARCH",
                 "R" ->
                    "RESEARCH";

            case "FOLLOWUP",
                 "FOLLOWUPS",
                 "FU" ->
                    "FOLLOWUP";

            case "EXPLAIN",
                 "EXP" ->
                    "EXPLAIN";

            case "CODE" ->
                    "CODE";

            case "SQL" ->
                    "SQL";

            case "DSA" ->
                    "DSA";

            case "COMPARE" ->
                    "COMPARE";

            default ->
                    normalized;
        };
    }

    private String buildPrompt(
            Application application,
            InterviewQuestion currentQuestion,
            List<InterviewQuestion> questions,
            ParsedCommand parsedCommand,
            String rawMessage
    ) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                """
                You are the ADROVIS Interview Copilot.

                You are assisting a human interviewer during a live
                internship interview.

                Your job is to help the interviewer understand the current
                interview question, understand technical topics, prepare
                follow-ups, research unfamiliar subjects, and compare a
                candidate's answer with the prepared answer.

                The interviewer may have only basic-to-intermediate knowledge
                of a particular technical topic. Explain things clearly and
                professionally, but do not make the interviewer sound like
                a beginner.

                Keep responses highly readable and useful during a live call.

                Do NOT invent candidate facts.
                Do NOT invent resume facts.
                Do NOT claim the candidate did something unless it appears
                in the supplied context.

                ============================================================
                CANDIDATE CONTEXT
                ============================================================

                Candidate:
                """
        );

        prompt.append(
                safe(
                        application.getApplicantName()
                )
        );

        prompt.append(
                "\nApplication ID:\n"
        );

        prompt.append(
                safe(
                        application.getApplicationId()
                )
        );

        prompt.append(
                "\nJob / Internship:\n"
        );

        prompt.append(
                safe(
                        application.getJobTitleSnapshot()
                )
        );

        prompt.append(
                "\nCollege:\n"
        );

        prompt.append(
                safe(
                        application.getCollege()
                )
        );

        prompt.append(
                "\nGraduation year:\n"
        );

        prompt.append(
                application.getGraduationYear() == null
                        ? "Not provided"
                        : application
                        .getGraduationYear()
                        .toString()
        );

        prompt.append(
                """
                
                ============================================================
                CURRENT QUESTION
                ============================================================
                """
        );

        if (currentQuestion == null) {

            prompt.append(
                    "No specific interview question selected.\n"
            );

        } else {

            prompt.append(
                    "Q"
                            + currentQuestion.getQuestionNumber()
                            + "\n"
            );

            prompt.append(
                    "Category: "
                            + currentQuestion.getCategory()
                            + "\n"
            );

            prompt.append(
                    "Difficulty: "
                            + currentQuestion.getDifficulty()
                            + "\n"
            );

            prompt.append(
                    "Answer source: "
                            + currentQuestion.getAnswerSource()
                            + "\n"
            );

            prompt.append(
                    "Question: "
                            + safe(
                            currentQuestion.getQuestion()
                    )
                            + "\n"
            );

            prompt.append(
                    "Expected answer: "
                            + safe(
                            currentQuestion.getExpectedAnswer()
                    )
                            + "\n"
            );

            prompt.append(
                    "Interviewer goal: "
                            + safe(
                            currentQuestion.getInterviewerGoal()
                    )
                            + "\n"
            );

            prompt.append(
                    "Resume basis: "
                            + safe(
                            currentQuestion.getResumeBasis()
                    )
                            + "\n"
            );
        }

        prompt.append(
                """
                
                ============================================================
                FULL QUESTION SET
                ============================================================
                """
        );

        for (InterviewQuestion question :
                questions) {

            prompt.append(
                    "Q"
                            + question.getQuestionNumber()
                            + " ["
                            + question.getCategory()
                            + " / "
                            + question.getDifficulty()
                            + "] "
                            + safe(
                            question.getQuestion()
                    )
                            + "\n"
            );
        }

        prompt.append(
                """
                
                ============================================================
                COMMAND
                ============================================================
                """
        );

        prompt.append(
                parsedCommand.command()
        );

        prompt.append(
                "\n\nUSER MESSAGE\n"
        );

        String effectiveMessage =
                parsedCommand.message().isBlank()
                        ? rawMessage
                        : parsedCommand.message();

        prompt.append(
                truncate(
                        effectiveMessage,
                        4000
                )
        );

        prompt.append(
                """
                
                ============================================================
                COMMAND BEHAVIOUR
                ============================================================

                SUMMARY:
                Explain what the current question is asking, what topic it
                tests, and what a reasonable candidate answer should contain.

                ANSWER:
                Give a concise interview-ready expected answer and tell the
                interviewer the main things to listen for.

                RESEARCH:
                Research the topic using web sources. Give a concise
                explanation, the important points, and practical context
                useful for this interview. Prefer authoritative sources.

                EXPLAIN:
                Explain the topic in simple professional language.

                FOLLOWUP:
                Suggest up to 3 natural follow-up questions, with a short
                note about what a good answer should cover.

                CODE:
                Explain the supplied code, what it does, and any important
                issue an interviewer should understand.

                SQL:
                Explain the SQL/query, what it returns or changes, and any
                important issue or concept.

                DSA:
                Explain the DSA problem or concept and the expected approach
                at an interview level. Do not overcomplicate it.

                COMPARE:
                If the user supplied a candidate answer, compare it against
                the prepared expected answer and tell the interviewer:
                what was correct, what was missing, and one useful follow-up.

                GENERAL:
                Answer the user's question using the interview context.

                Keep normal answers concise.
                Prefer short paragraphs and bullets.
                Do not produce a giant essay.
                """
        );

        return truncate(
                prompt.toString(),
                properties.getMaxInputChars()
        );
    }

    private GeminiResult generate(
            String prompt) {

        if (properties.getApiKey() == null
                || properties.getApiKey().isBlank()) {

            throw new IllegalStateException(
                    "Interview copilot API key is not configured."
            );
        }

        String url =
                properties.getBaseUrl()
                        + "/v1beta/models/"
                        + properties.getModel()
                        + ":generateContent";

        Map<String, Object> requestBody =
                new LinkedHashMap<>();

        Map<String, Object> textPart =
                new LinkedHashMap<>();

        textPart.put(
                "text",
                prompt
        );

        Map<String, Object> content =
                new LinkedHashMap<>();

        content.put(
                "role",
                "user"
        );

        content.put(
                "parts",
                List.of(textPart)
        );

        requestBody.put(
                "contents",
                List.of(content)
        );

        Map<String, Object> generationConfig =
                new LinkedHashMap<>();

        generationConfig.put(
                "temperature",
                properties.getTemperature()
        );

        generationConfig.put(
                "max_output_tokens",
                properties.getMaxOutputTokens()
        );

        requestBody.put(
                "generationConfig",
                generationConfig
        );


        int attempts =
                Math.max(
                        1,
                        properties.getMaxRetries() + 1
                );

        for (int attempt = 1;
             attempt <= attempts;
             attempt++) {

            try {

                ResponseEntity<String> response =
                        restClient
                                .post()
                                .uri(
                                        URI.create(url)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .header(
                                        "x-goog-api-key",
                                        properties.getApiKey()
                                )
                                .body(requestBody)
                                .retrieve()
                                .toEntity(
                                        String.class
                                );

                String body =
                        response.getBody();

                if (body == null
                        || body.isBlank()) {

                    throw new IllegalStateException(
                            "Interview copilot returned an empty response."
                    );
                }

                return parseGeminiResponse(
                        body
                );

            } catch (
                    RestClientResponseException ex
            ) {

                log.warn(
                        "Interview copilot Gemini call failed. status={}, attempt={}/{}",
                        ex.getStatusCode().value(),
                        attempt,
                        attempts
                );

                if (attempt >= attempts) {

                    throw new IllegalStateException(
                            "Interview copilot is temporarily unavailable."
                    );
                }

                sleepBeforeRetry(
                        attempt
                );
            }
        }

        throw new IllegalStateException(
                "Interview copilot is temporarily unavailable."
        );
    }

    private GeminiResult parseGeminiResponse(
            String body
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(
                            body
                    );

            JsonNode candidates =
                    root.path(
                            "candidates"
                    );

            if (!candidates.isArray()
                    || candidates.isEmpty()) {

                throw new IllegalStateException(
                        "Interview copilot returned no answer."
                );
            }

            JsonNode candidate =
                    candidates.get(0);

            StringBuilder answer =
                    new StringBuilder();

            JsonNode parts =
                    candidate
                            .path("content")
                            .path("parts");

            if (parts.isArray()) {

                for (JsonNode part :
                        parts) {

                    String text =
                            part.path("text")
                                    .asText(
                                            ""
                                    );

                    if (!text.isBlank()) {

                        if (answer.length() > 0) {
                            answer.append(
                                    "\n"
                            );
                        }

                        answer.append(
                                text
                        );
                    }
                }
            }

            if (answer.isEmpty()) {

                throw new IllegalStateException(
                        "Interview copilot returned an empty answer."
                );
            }

            List<InterviewCopilotResponse.Source>
                    sources =
                    parseSources(
                            candidate
                    );

            return new GeminiResult(
                    answer.toString().trim(),
                    sources
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to parse interview copilot response.",
                    ex
            );

            throw new IllegalStateException(
                    "Interview copilot returned an unreadable response."
            );
        }
    }

    private List<InterviewCopilotResponse.Source>
    parseSources(
            JsonNode candidate
    ) {

        JsonNode chunks =
                candidate
                        .path("groundingMetadata")
                        .path("groundingChunks");

        if (!chunks.isArray()) {
            return Collections.emptyList();
        }

        List<InterviewCopilotResponse.Source>
                sources =
                new ArrayList<>();

        for (JsonNode chunk :
                chunks) {

            JsonNode web =
                    chunk.path(
                            "web"
                    );

            if (web.isMissingNode()) {
                continue;
            }

            String title =
                    web.path("title")
                            .asText(
                                    "Source"
                            );

            String url =
                    web.path("uri")
                            .asText(
                                    ""
                            );

            if (!url.isBlank()) {

                sources.add(
                        new InterviewCopilotResponse.Source(
                                title,
                                url
                        )
                );
            }

            if (sources.size() >= 5) {
                break;
            }
        }

        return sources;
    }

    private UUID saveNote(
            Interview interview,
            InterviewQuestion currentQuestion,
            ParsedCommand command,
            String rawMessage,
            String answer,
            List<InterviewCopilotResponse.Source> sources
    ) {

        Interview current =
                interviewRepository
                        .findById(
                                interview.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Interview no longer exists."
                                )
                        );

        List<InterviewCopilotResponse.SavedNote>
                notes =
                parseSavedNotes(
                        current.getAiCopilotNotes()
                );

        UUID noteId =
                UUID.randomUUID();

        InterviewCopilotResponse.SavedNote note =
                new InterviewCopilotResponse.SavedNote(
                        noteId,
                        currentQuestion == null
                                ? null
                                : currentQuestion
                                .getQuestionNumber(),
                        currentQuestion == null
                                ? null
                                : truncate(
                                currentQuestion
                                        .getQuestion(),
                                4000
                        ),
                        truncate(
                                rawMessage,
                                2000
                        ),
                        truncate(
                                answer,
                                10000
                        ),
                        command.command(),
                        sources == null
                                ? Collections.emptyList()
                                : sources,
                        OffsetDateTime.now().toString()
                );

        List<InterviewCopilotResponse.SavedNote>
                updatedNotes =
                new ArrayList<>(
                        notes
                );

        updatedNotes.add(
                note
        );

        int maxNotes =
                Math.max(
                        1,
                        properties.getMaxNotes()
                );

        while (
                updatedNotes.size()
                        > maxNotes
        ) {

            updatedNotes.remove(
                    0
            );
        }

        try {

            current.setAiCopilotNotes(
                    objectMapper.writeValueAsString(
                            updatedNotes
                    )
            );

            interviewRepository.save(
                    current
            );

            log.info(
                    "Interview copilot note saved. interviewId={}, noteId={}, questionNumber={}",
                    current.getId(),
                    noteId,
                    note.questionNumber()
            );

            return noteId;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Interview copilot answer could not be saved.",
                    ex
            );
        }
    }

    private List<InterviewCopilotResponse.SavedNote>
    parseSavedNotes(
            String json
    ) {

        if (json == null
                || json.isBlank()) {

            return new ArrayList<>();
        }

        try {

            return objectMapper.readValue(
                    json,
                    new TypeReference<
                            List<InterviewCopilotResponse.SavedNote>
                            >() {
                    }
            );

        } catch (Exception ex) {

            log.warn(
                    "Existing interview copilot notes could not be parsed. Starting with an empty note list."
            );

            return new ArrayList<>();
        }
    }

    private void sleepBeforeRetry(
            int attempt
    ) {

        try {

            Thread.sleep(
                    Math.min(
                            properties.getRetryBaseDelayMs()
                                    * attempt,
                            2000L
                    )
            );

        } catch (InterruptedException ex) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "Interview copilot retry interrupted."
            );
        }
    }

    private String truncate(
            String value,
            int max
    ) {

        if (value == null) {
            return "";
        }

        if (value.length() <= max) {
            return value;
        }

        return value.substring(
                0,
                max
        );
    }

    private String safe(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return "Not provided";
        }

        return value;
    }

    private record ParsedCommand(
            String command,
            String message
    ) {

        private boolean commandNeedsQuestionContext() {

            return switch (command) {

                case "SUMMARY",
                     "ANSWER",
                     "RESEARCH",
                     "FOLLOWUP",
                     "EXPLAIN",
                     "COMPARE" ->
                        true;

                default ->
                        false;
            };
        }
    }

    private record GeminiResult(
            String answer,
            List<InterviewCopilotResponse.Source> sources
    ) {
    }
    public InterviewCopilotResponse.SavedNote save(
            String applicationId,
            InterviewCopilotSaveRequest request
    ) {

        Application application =
                applicationRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
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
                                new IllegalArgumentException(
                                        "Interview not found for application: "
                                                + applicationId
                                )
                        );

        InterviewQuestion currentQuestion =
                request.questionNumber() == null
                        ? null
                        : interviewQuestionRepository
                        .findByInterviewIdOrderByQuestionNumberAsc(
                                interview.getId()
                        )
                        .stream()
                        .filter(question ->
                                request.questionNumber()
                                        .equals(
                                                question.getQuestionNumber()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        List<InterviewCopilotResponse.SavedNote> notes =
                parseSavedNotes(
                        interview.getAiCopilotNotes()
                );

        UUID noteId = UUID.randomUUID();

        InterviewCopilotResponse.SavedNote note =
                new InterviewCopilotResponse.SavedNote(
                        noteId,
                        request.questionNumber(),
                        currentQuestion == null
                                ? null
                                : truncate(
                                currentQuestion.getQuestion(),
                                4000
                        ),
                        truncate(
                                request.userPrompt(),
                                2000
                        ),
                        truncate(
                                request.answer(),
                                10000
                        ),
                        normalizeCommand(
                                request.command()
                        ),
                        request.sources() == null
                                ? Collections.emptyList()
                                : request.sources(),
                        OffsetDateTime.now().toString()
                );

        notes.add(note);

        int maxNotes =
                Math.max(
                        1,
                        properties.getMaxNotes()
                );

        while (notes.size() > maxNotes) {
            notes.remove(0);
        }

        try {

            interview.setAiCopilotNotes(
                    objectMapper.writeValueAsString(
                            notes
                    )
            );

            interviewRepository.save(
                    interview
            );

            log.info(
                    "Interview copilot note saved. interviewId={}, noteId={}, questionNumber={}",
                    interview.getId(),
                    noteId,
                    request.questionNumber()
            );

            return note;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Interview copilot answer could not be saved.",
                    ex
            );
        }
    }
}